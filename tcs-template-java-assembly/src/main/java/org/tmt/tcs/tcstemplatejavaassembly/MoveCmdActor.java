package org.tmt.tcs.tcstemplatejavaassembly;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import akka.util.Timeout;
import csw.messages.commands.CommandName;
import csw.messages.commands.CommandResponse;
import csw.messages.commands.ControlCommand;
import csw.messages.commands.Setup;
import csw.messages.params.generics.Parameter;
import csw.messages.params.models.ObsId;
import csw.messages.params.models.Prefix;
import csw.messages.params.models.Id;
import csw.services.command.javadsl.JCommandService;
import csw.services.command.scaladsl.CommandResponseManager;
import csw.services.logging.javadsl.ILogger;
import csw.services.logging.javadsl.JLoggerFactory;
import scala.Option;
import scala.concurrent.Await;
import scala.concurrent.duration.FiniteDuration;

import java.util.*;
import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
//import akka.actor.typed.javadsl.MutableBehavior;

public class MoveCmdActor extends Behaviors.MutableBehavior<ControlCommand> {


    // Add messages here
    // No sealed trait/interface or messages for this actor.  Always accepts the Submit command message.


    private ActorContext<ControlCommand> actorContext;
    private JLoggerFactory loggerFactory;
    private ILogger log;
    private CommandResponseManager commandResponseManager;
    private Optional<JCommandService> templateHcd;


    private MoveCmdActor(ActorContext<ControlCommand> actorContext, CommandResponseManager commandResponseManager, Optional<JCommandService> templateHcd, JLoggerFactory loggerFactory) {
        this.actorContext = actorContext;
        this.loggerFactory = loggerFactory;
        this.log = loggerFactory.getLogger(actorContext, getClass());
        this.commandResponseManager = commandResponseManager;
        this.templateHcd = templateHcd;

    }

    public static <ControlCommand> Behavior<ControlCommand> behavior(CommandResponseManager commandResponseManager, Optional<JCommandService> templateHcd, JLoggerFactory loggerFactory) {
        return Behaviors.setup(ctx -> {
            return (Behaviors.MutableBehavior<ControlCommand>) new MoveCmdActor((ActorContext<csw.messages.commands.ControlCommand>) ctx, commandResponseManager, templateHcd,
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

        // NOTE: we use get instead of getOrElse because we assume the command has been validated
        Parameter axesParam = message.paramSet().find(x -> x.keyName().equals("axes")).get();
        Parameter azParam = message.paramSet().find(x -> x.keyName().equals("az")).get();
        Parameter elParam = message.paramSet().find(x -> x.keyName().equals("el")).get();

        // create Point and PointDemand messages and send to HCD

        CompletableFuture<CommandResponse> moveFuture = move(message.maybeObsId(), axesParam, azParam, elParam);

        moveFuture.thenAccept((response) -> {

            log.debug("response = " + response);
            log.debug("runId = " + message.runId());

            commandResponseManager.addSubCommand(message.runId(), response.runId());

            commandResponseManager.updateSubCommand(response.runId(), response);

            log.info("move command message handled");


        });


    }

    private Prefix templateHcdPrefix = new Prefix("tcs.tcs-templatehcd");

    //implicit val timeout: Timeout = Timeout(30.seconds)

    //CompletableFuture<CommandResponse> move() {
    //    return move(, , , );
    //}

    CompletableFuture<CommandResponse> move(Option<ObsId> obsId,
                                            Parameter axesParam,
                                            Parameter azParam,
                                            Parameter elParam) {

        if (templateHcd.isPresent()) {

            Setup setupHcd1 = new Setup(templateHcdPrefix, new CommandName("point"), Optional.empty()).add(axesParam);
            Setup setupHcd2 = new Setup(templateHcdPrefix, new CommandName("pointDemand"), Optional.empty()).add(azParam).add(elParam);

            HashMap<JCommandService, Set<ControlCommand>> componentsToCommands = new HashMap<JCommandService, Set<ControlCommand>>() {
                {
                    put(templateHcd.get(), new HashSet<ControlCommand>(Arrays.asList(setupHcd1, setupHcd2)));
                }
            };

            CompletableFuture<CommandResponse> commandResponse = templateHcd.get()
                    .submitAllAndGetFinalResponse(
                            new HashSet<ControlCommand>(Arrays.asList(setupHcd1, setupHcd2)),
                            Timeout.durationToTimeout(FiniteDuration.apply(5, TimeUnit.SECONDS))
                    );

            return commandResponse;

        } else {

            return CompletableFuture.completedFuture(new CommandResponse.Error(new Id(""), "Can't locate TcsTemplateHcd"));
        }

    }

}
