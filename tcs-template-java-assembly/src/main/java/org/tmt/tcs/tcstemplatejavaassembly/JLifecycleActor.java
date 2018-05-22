package org.tmt.tcs.tcstemplatejavaassembly;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import csw.services.logging.javadsl.ILogger;
import csw.services.logging.javadsl.JLoggerFactory;
//import akka.actor.typed.javadsl.MutableBehavior;

public class JLifecycleActor extends Behaviors.MutableBehavior<JLifecycleActor.LifecycleMessage> {


    // add messages here
    static interface LifecycleMessage {}

    public static final class InitializeMessage implements LifecycleMessage { }
    public static final class ShutdownMessage implements LifecycleMessage { }


    private ActorContext<LifecycleMessage> actorContext;
    private JLoggerFactory loggerFactory;
    private ILogger log;


    private JLifecycleActor(ActorContext<LifecycleMessage> actorContext, JLoggerFactory loggerFactory) {
        this.actorContext = actorContext;
        this.loggerFactory = loggerFactory;
        this.log = loggerFactory.getLogger(actorContext, getClass());

    }

    public static <LifecycleMessage> Behavior<LifecycleMessage> behavior(JLoggerFactory loggerFactory) {
        return Behaviors.setup(ctx -> {
            return (Behaviors.MutableBehavior<LifecycleMessage>) new JLifecycleActor((ActorContext<JLifecycleActor.LifecycleMessage>) ctx, loggerFactory);
        });
    }


    @Override
    public Behaviors.Receive<LifecycleMessage> createReceive() {

        ReceiveBuilder<LifecycleMessage> builder = receiveBuilder()
                .onMessage(InitializeMessage.class,
                        command -> {
                            log.info("InitializeMessage Received");
                            onInitialize(command);
                            return Behaviors.same();
                        })
                .onMessage(ShutdownMessage.class,
                        command -> {
                        log.info("ShutdownMessage Received");
                        onShutdown(command);
                        return Behaviors.same();
                });
        return builder.build();
    }

    private void onInitialize(InitializeMessage message) {

        log.info("Initialize Message Received ");

        // TODO: example of working with Config
       // val bazValue: Int = assemblyConfig.getInt("foo.bar.baz")
       // log.debug(s"foo.bar.baz config element value is: $bazValue")

    }

    private void onShutdown(ShutdownMessage message) {

        log.info("Shutdown Message Received ");
    }


}
