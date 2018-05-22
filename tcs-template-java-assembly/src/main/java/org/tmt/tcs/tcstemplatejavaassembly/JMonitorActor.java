package org.tmt.tcs.tcstemplatejavaassembly;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import akka.japi.Option;
import csw.services.command.scaladsl.CommandService;
import csw.services.logging.javadsl.ILogger;
import csw.services.logging.javadsl.JLoggerFactory;

//import akka.actor.typed.javadsl.MutableBehavior;

public class JMonitorActor extends Behaviors.MutableBehavior<JMonitorActor.MonitorMessage> {


    public enum AssemblyState {
        Ready, Degraded, Disconnected, Faulted
    }
    public enum AssemblyMotionState {
        Idle, Slewing, Tracking, InPosition, Halted
    }

    // add messages here
    static interface MonitorMessage {}

    public static final class AssemblyStateChangeMessage implements MonitorMessage {

        public final AssemblyState assemblyState;

        public AssemblyStateChangeMessage(AssemblyState assemblyState) {
            this.assemblyState = assemblyState;
        }
    }
    public static final class AssemblyMotionStateChangeMessage implements MonitorMessage {

        public final AssemblyMotionState assemblyMotionState;

        public AssemblyMotionStateChangeMessage(AssemblyMotionState assemblyMotionState) {
            this.assemblyMotionState = assemblyMotionState;
        }
    }

    public static final class LocationEventMessage implements MonitorMessage {

        public final Option<CommandService> templateHcd;

        public LocationEventMessage(Option<CommandService> templateHcd) {
            this.templateHcd = templateHcd;
        }
    }


    private ActorContext<MonitorMessage> actorContext;
    private JLoggerFactory loggerFactory;
    private ILogger log;
    private AssemblyState assemblyState;
    private AssemblyMotionState assemblyMotionState;

    private JMonitorActor(ActorContext<MonitorMessage> actorContext, AssemblyState assemblyState, AssemblyMotionState assemblyMotionState, JLoggerFactory loggerFactory) {
        this.actorContext = actorContext;
        this.loggerFactory = loggerFactory;
        this.log = loggerFactory.getLogger(actorContext, getClass());
        this.assemblyState = assemblyState;
        this.assemblyMotionState = assemblyMotionState;
    }

    public static <MonitorMessage> Behavior<MonitorMessage> behavior(AssemblyState assemblyState, AssemblyMotionState assemblyMotionState, JLoggerFactory loggerFactory) {
        return Behaviors.setup(ctx -> {
            return (Behaviors.MutableBehavior<MonitorMessage>) new JMonitorActor((ActorContext<JMonitorActor.MonitorMessage>) ctx, assemblyState, assemblyMotionState, loggerFactory);
        });
    }


    @Override
    public Behaviors.Receive<MonitorMessage> createReceive() {

        ReceiveBuilder<MonitorMessage> builder = receiveBuilder()
                .onMessage(AssemblyStateChangeMessage.class,
                        message -> {
                            log.info("AssemblyStateChangeMessage Received");
                            // change the behavior state
                            return behavior(message.assemblyState, assemblyMotionState, loggerFactory);

                        })
                .onMessage(AssemblyMotionStateChangeMessage.class,
                        message -> {
                            log.info("AssemblyMotionStateChangeMessage Received");
                            // change the behavior state
                            return behavior(assemblyState, message.assemblyMotionState, loggerFactory);
                        })
                .onMessage(LocationEventMessage.class,
                        message -> {
                            log.info("LocationEventMessage Received");
                            return onLocationEventMessage(message);
                        });
        return builder.build();
    }

    private Behavior<MonitorMessage> onLocationEventMessage(LocationEventMessage message) {

        if (message.templateHcd.isEmpty() ) {
            // if templateHcd is null, then change state to disconnected
            return JMonitorActor.behavior(AssemblyState.Disconnected, assemblyMotionState, loggerFactory);
        } else {
            if (assemblyState == AssemblyState.Disconnected) {
                // TODO: this logic is oversimplified: just because the state is no longer disconnected, does not mean it is Ready
                return JMonitorActor.behavior(AssemblyState.Ready, assemblyMotionState, loggerFactory);
            } else {
                return this;
            }
        }
    }


}
