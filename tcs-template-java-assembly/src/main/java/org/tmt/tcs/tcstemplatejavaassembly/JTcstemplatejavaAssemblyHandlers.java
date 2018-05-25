package org.tmt.tcs.tcstemplatejavaassembly;

import akka.actor.ActorRefFactory;
import akka.actor.typed.ActorRef;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Adapter;

import akka.stream.Materializer;
import com.typesafe.config.Config;
import csw.framework.exceptions.FailureStop;
import csw.framework.javadsl.JComponentHandlers;
import csw.framework.scaladsl.CurrentStatePublisher;
import csw.messages.commands.CommandResponse;
import csw.messages.commands.ControlCommand;
import csw.messages.framework.ComponentInfo;
import csw.messages.location.*;
import csw.messages.scaladsl.TopLevelActorMessage;
import csw.services.command.javadsl.JCommandService;
import csw.services.command.scaladsl.CommandResponseManager;

import csw.services.command.scaladsl.CurrentStateSubscription;
import csw.services.config.api.javadsl.IConfigClientService;
import csw.services.config.api.models.ConfigData;
import csw.services.config.client.internal.ActorRuntime;
import csw.services.config.client.javadsl.JConfigClientFactory;

import csw.services.location.javadsl.ILocationService;
import csw.services.logging.javadsl.ILogger;
import csw.services.logging.javadsl.JLoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to TcstemplatejavaHcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw-prod/framework.html
 */
public class JTcstemplatejavaAssemblyHandlers extends JComponentHandlers {

    private ILogger log;
    private CommandResponseManager commandResponseManager;
    private CurrentStatePublisher currentStatePublisher;
    private ActorContext<TopLevelActorMessage> actorContext;
    private ILocationService locationService;
    private ComponentInfo componentInfo;
    private IConfigClientService clientApi;

    private ActorRef<JCommandHandlerActor.CommandMessage> commandHandlerActor;
    private ActorRef<JEventHandlerActor.EventMessage> eventHandlerActor;
    private ActorRef<JLifecycleActor.LifecycleMessage> lifecycleActor;
    private ActorRef<JMonitorActor.MonitorMessage> monitorActor;

    // reference to the template HCD
    private Optional<JCommandService> templateHcd = Optional.empty();  // NOTE the use of Optional

    private Optional<CurrentStateSubscription> subscription = Optional.empty();

    JTcstemplatejavaAssemblyHandlers(
            ActorContext<TopLevelActorMessage> ctx,
            ComponentInfo componentInfo,
            CommandResponseManager commandResponseManager,
            CurrentStatePublisher currentStatePublisher,
            ILocationService locationService,
            JLoggerFactory loggerFactory
    ) {
        super(ctx, componentInfo, commandResponseManager, currentStatePublisher, locationService, loggerFactory);
        this.currentStatePublisher = currentStatePublisher;
        this.log = loggerFactory.getLogger(getClass());
        this.commandResponseManager = commandResponseManager;
        this.actorContext = ctx;
        this.locationService = locationService;
        this.componentInfo = componentInfo;


        // Handle to the config client service
        clientApi = JConfigClientFactory.clientApi(Adapter.toUntyped(actorContext.getSystem()), locationService);


        // Load the configuration from the configuration service
        Config assemblyConfig = getAssemblyConfig();

        commandHandlerActor = ctx.spawnAnonymous(JCommandHandlerActor.behavior(commandResponseManager, templateHcd, Boolean.TRUE, loggerFactory));

        eventHandlerActor = ctx.spawnAnonymous(JEventHandlerActor.behavior(loggerFactory));

        lifecycleActor = ctx.spawnAnonymous(JLifecycleActor.behavior(assemblyConfig, loggerFactory));

        monitorActor = ctx.spawnAnonymous(JMonitorActor.behavior(JMonitorActor.AssemblyState.Ready, JMonitorActor.AssemblyMotionState.Idle, loggerFactory));

    }





    @Override
    public CompletableFuture<Void> jInitialize() {
        return CompletableFuture.runAsync(() -> {
            log.debug("in initialize()");

             lifecycleActor.tell(new JLifecycleActor.InitializeMessage());

        });
    }

    @Override
    public CompletableFuture<Void> jOnShutdown() {
        return CompletableFuture.runAsync(() -> {
            log.debug("in onShutdown()");

            lifecycleActor.tell(new JLifecycleActor.ShutdownMessage());
        });
    }

    @Override
    public void onLocationTrackingEvent(TrackingEvent trackingEvent) {

        log.debug("in onLocationTrackingEvent()");

        if (trackingEvent instanceof LocationUpdated) {
            // do something for the tracked location when it is updated

            AkkaLocation hcdLocation = (AkkaLocation)((LocationUpdated) trackingEvent).location();

            templateHcd = Optional.of(new JCommandService(hcdLocation, actorContext.getSystem()));

            // set up Hcd CurrentState subscription to be handled by the monitor actor
            subscription = Optional.of(templateHcd.get().subscribeCurrentState(currentState ->
                    monitorActor.tell(new JMonitorActor.CurrentStateEventMessage(currentState))));

        } else if (trackingEvent instanceof LocationRemoved) {
            // do something for the tracked location when it is no longer available
            templateHcd = Optional.empty();
            // FIXME: not sure if this is necessary
            subscription.get().unsubscribe();

        }

        // send messages to command handler and monitor actors
        commandHandlerActor.tell(new JCommandHandlerActor.UpdateTemplateHcdMessage(templateHcd));

        monitorActor.tell(new JMonitorActor.LocationEventMessage(templateHcd));

    }

    @Override
    public CommandResponse validateCommand(ControlCommand controlCommand) {
        log.debug("in validateCommand()");
        return new CommandResponse.Accepted(controlCommand.runId());
    }

    @Override
    public void onSubmit(ControlCommand controlCommand) {

        log.debug("in onSubmit()");

        commandHandlerActor.tell(new JCommandHandlerActor.SubmitCommandMessage(controlCommand));
    }

    @Override
    public void onOneway(ControlCommand controlCommand) {
        log.debug("in onOneway()");
    }

    @Override
    public void onGoOffline() {
        log.debug("in onGoOffline()");

        commandHandlerActor.tell(new JCommandHandlerActor.GoOfflineMessage());


    }

    @Override
    public void onGoOnline() {
        log.debug("in onGoOnline()");

        commandHandlerActor.tell(new JCommandHandlerActor.GoOnlineMessage());


    }

    public class ConfigNotAvailableException extends FailureStop {

        public ConfigNotAvailableException() {
            super("Configuration not available. Initialization failure.");
        }
    }

    private Config getAssemblyConfig() {

        try {
            ActorRefFactory actorRefFactory = Adapter.toUntyped(actorContext.getSystem());

            ActorRuntime actorRuntime = new ActorRuntime(Adapter.toUntyped(actorContext.getSystem()));

            Materializer mat = actorRuntime.mat();

            ConfigData configData = getAssemblyConfigData();

            return configData.toJConfigObject(mat).get();

        } catch (Exception e) {
            throw new ConfigNotAvailableException();
        }

    }

    private ConfigData getAssemblyConfigData() throws ExecutionException, InterruptedException {

        log.info("loading assembly configuration");

        // construct the path
        Path filePath = Paths.get("/org/tmt/tcs/tcs_test.conf");

        ConfigData activeFile = clientApi.getActive(filePath).get().get();

        return activeFile;
    }


}
