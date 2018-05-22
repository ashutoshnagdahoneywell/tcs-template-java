package org.tmt.tcs.tcstemplatejavaassembly;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import csw.framework.javadsl.JComponentHandlers;
import csw.framework.scaladsl.CurrentStatePublisher;
import csw.messages.commands.CommandResponse;
import csw.messages.commands.ControlCommand;
import csw.messages.framework.ComponentInfo;
import csw.messages.location.*;
import csw.messages.scaladsl.TopLevelActorMessage;
import csw.services.command.javadsl.JCommandService;
import csw.services.command.scaladsl.CommandResponseManager;
import csw.services.command.scaladsl.CommandService;
import csw.services.location.javadsl.ILocationService;
import csw.services.logging.javadsl.ILogger;
import csw.services.logging.javadsl.JLoggerFactory;
import scala.None;
import scala.Option;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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

    private ActorRef<JCommandHandlerActor.CommandMessage> commandHandlerActor;
    private ActorRef<JEventHandlerActor.EventMessage> eventHandlerActor;
    private ActorRef<JLifecycleActor.LifecycleMessage> lifecycleActor;
    private ActorRef<JMonitorActor.MonitorMessage> monitorActor;

    // reference to the template HCD
    private Optional<JCommandService> templateHcd = Optional.empty();  // NOTE the use of Optional

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





        commandHandlerActor = ctx.spawnAnonymous(JCommandHandlerActor.behavior(commandResponseManager, templateHcd, Boolean.TRUE, loggerFactory));

        eventHandlerActor = ctx.spawnAnonymous(JEventHandlerActor.behavior(loggerFactory));

        lifecycleActor = ctx.spawnAnonymous(JLifecycleActor.behavior(loggerFactory));

        monitorActor = ctx.spawnAnonymous(JMonitorActor.behavior(JMonitorActor.AssemblyState.Ready, JMonitorActor.AssemblyMotionState.Idle, loggerFactory));
    }





    @Override
    public CompletableFuture<Void> jInitialize() {
        return CompletableFuture.runAsync(() -> {
            log.debug("in initialize()");

            commandHandlerActor.tell(new JCommandHandlerActor.GoOnlineMessage());


        });
    }

    @Override
    public CompletableFuture<Void> jOnShutdown() {
        return CompletableFuture.runAsync(() -> {
            log.debug("in onShutdown()");
        });
    }

    @Override
    public void onLocationTrackingEvent(TrackingEvent trackingEvent) {

        log.debug("in onLocationTrackingEvent()");

        if (trackingEvent instanceof LocationUpdated) {
            // do something for the tracked location when it is updated

            AkkaLocation hcdLocation = (AkkaLocation)((LocationUpdated) trackingEvent).location();

            templateHcd = Optional.of(new JCommandService(hcdLocation, actorContext.getSystem()));


        } else if (trackingEvent instanceof LocationRemoved) {
            // do something for the tracked location when it is no longer available
        }


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
    }

    @Override
    public void onGoOnline() {
        log.debug("in onGoOnline()");
    }
}
