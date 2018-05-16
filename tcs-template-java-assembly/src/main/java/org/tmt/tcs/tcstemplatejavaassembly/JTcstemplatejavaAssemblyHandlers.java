package org.tmt.tcs.tcstemplatejavaassembly;

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
public class JTcstemplatejavaAssemblyHandlers extends JComponentHandlers {

    private ILogger log;
    private CommandResponseManager commandResponseManager;
    private CurrentStatePublisher currentStatePublisher;
    private ActorContext<TopLevelActorMessage> actorContext;
    private ILocationService locationService;
    private ComponentInfo componentInfo;

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
    }

    @Override
    public CompletableFuture<Void> jInitialize() {
        return CompletableFuture.runAsync(() -> {
            log.debug("in initialize()");
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
    }

    @Override
    public CommandResponse validateCommand(ControlCommand controlCommand) {
        log.debug("in validateCommand()");
        return null;
    }

    @Override
    public void onSubmit(ControlCommand controlCommand) {
        log.debug("in onSubmit()");
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
