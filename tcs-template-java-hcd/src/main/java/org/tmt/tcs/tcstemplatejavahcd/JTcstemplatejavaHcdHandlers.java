package org.tmt.tcs.tcstemplatejavahcd;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import csw.framework.javadsl.JComponentHandlers;
import csw.framework.scaladsl.CurrentStatePublisher;
import csw.messages.commands.CommandResponse;
import csw.messages.commands.ControlCommand;
import csw.messages.framework.ComponentInfo;
import csw.messages.location.TrackingEvent;
import csw.messages.scaladsl.TopLevelActorMessage;
import csw.services.command.scaladsl.CommandResponseManager;
import csw.services.location.javadsl.ILocationService;
import csw.services.logging.javadsl.ILogger;
import csw.services.logging.javadsl.JLoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to TcstemplatejavaHcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw-prod/framework.html
 */
public class JTcstemplatejavaHcdHandlers extends JComponentHandlers {

    private ILogger log;
    private CommandResponseManager commandResponseManager;
    private CurrentStatePublisher currentStatePublisher;
    private ActorContext<TopLevelActorMessage> actorContext;
    private ILocationService locationService;
    private ComponentInfo componentInfo;
    ActorRef<JStatePublisherActor.StatePublisherMessage> statePublisherActor;

    JTcstemplatejavaHcdHandlers(
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

        // create the assembly's components
        statePublisherActor =
                ctx.spawnAnonymous(JStatePublisherActor.behavior(currentStatePublisher, loggerFactory));

    }

    @Override
    public CompletableFuture<Void> jInitialize() {
        return CompletableFuture.runAsync(() -> {

            JStatePublisherActor.StartMessage message = new JStatePublisherActor.StartMessage();

            statePublisherActor.tell(message);

        });
    }

    @Override
    public CompletableFuture<Void> jOnShutdown() {
        return CompletableFuture.runAsync(() -> {

        });
    }

    @Override
    public void onLocationTrackingEvent(TrackingEvent trackingEvent) {

    }

    @Override
    public CommandResponse validateCommand(ControlCommand controlCommand) {

        return new CommandResponse.Accepted(controlCommand.runId());
    }

    @Override
    public void onSubmit(ControlCommand controlCommand) {

        switch (controlCommand.commandName().name()) {

            case "point":
                log.debug("handling point command: " + controlCommand);

                try { Thread.sleep(500); } catch (InterruptedException e) {};

                commandResponseManager.addOrUpdateCommand(controlCommand.runId(), new CommandResponse.Completed(controlCommand.runId()));

                break;

            case "pointDemand":
                log.debug("handling pointDemand command: " + controlCommand);

                try { Thread.sleep(1000); } catch (InterruptedException e) {};

                commandResponseManager.addOrUpdateCommand(controlCommand.runId(), new CommandResponse.Completed(controlCommand.runId()));

                break;

            default:
                log.error("unhandled message in Monitor Actor onMessage: " + controlCommand);
                // maintain actor state

        }

    }

    @Override
    public void onOneway(ControlCommand controlCommand) {

    }

    @Override
    public void onGoOffline() {

    }

    @Override
    public void onGoOnline() {

    }
}
