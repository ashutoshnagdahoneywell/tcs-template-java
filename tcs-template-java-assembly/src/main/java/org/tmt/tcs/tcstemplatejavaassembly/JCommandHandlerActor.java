package org.tmt.tcs.tcstemplatejavaassembly;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ActorContext;
//import akka.actor.typed.javadsl.MutableBehavior;
import akka.actor.typed.javadsl.ReceiveBuilder;
import csw.messages.commands.ControlCommand;
import csw.services.command.scaladsl.CommandService;
import org.tmt.tcs.tcstemplatejavaassembly.JCommandHandlerActor.CommandMessage;
import csw.services.logging.javadsl.ILogger;
import csw.services.logging.javadsl.JLoggerFactory;

public class JCommandHandlerActor extends Behaviors.MutableBehavior<CommandMessage> {


    // add messages here
    static interface CommandMessage {}

    public static final class SubmitCommandMessage implements CommandMessage {

        public final ControlCommand controlCommand;


        public SubmitCommandMessage(ControlCommand controlCommand) {
            this.controlCommand = controlCommand;
        }
    }

    public static final class GoOnlineMessage implements CommandMessage { }
    public static final class GoOfflineMessage implements CommandMessage { }

    public static final class UpdateTemplateHcdMessage implements CommandMessage {

        // TODO: how does Java implement Option<>
        public final CommandService commandService;

        public UpdateTemplateHcdMessage(CommandService commandService) {
            this.commandService = commandService;
        }
    }

    private ActorContext<CommandMessage> actorContext;
    private ILogger log;

    private JCommandHandlerActor(ActorContext<CommandMessage> actorContext, JLoggerFactory loggerFactory) {
        this.actorContext = actorContext;
        this.log = loggerFactory.getLogger(actorContext, getClass());
    }

    public static <CommandMessage> Behavior<CommandMessage> behavior(JLoggerFactory loggerFactory) {
        return Behaviors.setup(ctx -> {
            return (Behaviors.MutableBehavior<CommandMessage>) new JCommandHandlerActor((ActorContext<JCommandHandlerActor.CommandMessage>) ctx, loggerFactory);
        });
    }


    @Override
    public Behaviors.Receive<CommandMessage> createReceive() {

        ReceiveBuilder<CommandMessage> builder = receiveBuilder()
                .onMessage(SubmitCommandMessage.class,
                        command -> {
                            handleSubmitCommand(command);
                            return Behaviors.same();
                        })
                .onMessage(GoOnlineMessage.class,
                        command -> {
                            log.info("GO ONLINE REACHED");
                            // TODO: change the behavior to online
                            return Behavior.same();
                        });
        return builder.build();
    }

    private void handleSubmitCommand(SubmitCommandMessage message) {
        log.info("HANLDE SUBMIT REACHED");
    }


}
