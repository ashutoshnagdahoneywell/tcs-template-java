package org.tmt.tcs.tcstemplatejavaassembly;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import csw.messages.commands.CommandResponse;
import csw.messages.commands.ControlCommand;
import csw.services.command.scaladsl.CommandResponseManager;
import csw.services.logging.javadsl.ILogger;
import csw.services.logging.javadsl.JLoggerFactory;
//import akka.actor.typed.javadsl.MutableBehavior;

public class DatumCmdActor extends Behaviors.MutableBehavior<ControlCommand> {


    // Add messages here
    // No sealed trait/interface or messages for this actor.  Always accepts the Submit command message.


    private ActorContext<ControlCommand> actorContext;
    private JLoggerFactory loggerFactory;
    private ILogger log;
    private CommandResponseManager commandResponseManager;


    private DatumCmdActor(ActorContext<ControlCommand> actorContext, CommandResponseManager commandResponseManager, JLoggerFactory loggerFactory) {
        this.actorContext = actorContext;
        this.loggerFactory = loggerFactory;
        this.log = loggerFactory.getLogger(actorContext, getClass());
        this.commandResponseManager = commandResponseManager;

    }

    public static <ControlCommand> Behavior<ControlCommand> behavior(CommandResponseManager commandResponseManager, JLoggerFactory loggerFactory) {
        return Behaviors.setup(ctx -> {
            return (Behaviors.MutableBehavior<ControlCommand>) new DatumCmdActor((ActorContext<csw.messages.commands.ControlCommand>) ctx, commandResponseManager,
                    loggerFactory);
        });
    }


    @Override
    public Behaviors.Receive<ControlCommand> createReceive() {

        ReceiveBuilder<ControlCommand> builder = receiveBuilder()
                .onMessage(ControlCommand.class,
                        command -> {
                            log.info("DatumCmd Received");
                            handleSubmitCommand(command);
                            return Behaviors.same();
                        });
        return builder.build();
    }

    private void handleSubmitCommand(ControlCommand message) {

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            log.info("interrupted exception");
        }

        commandResponseManager.addOrUpdateCommand(message.runId(), new CommandResponse.Completed(message.runId()));

        log.info("command message handled");
    }


}
