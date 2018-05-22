package org.tmt.tcs.tcstemplatejavaclient;


import akka.actor.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.util.Timeout;
import csw.messages.commands.CommandName;
import csw.messages.commands.CommandResponse;
import csw.messages.commands.Setup;
import csw.messages.location.AkkaLocation;
import csw.messages.location.ComponentId;
import csw.messages.location.Connection;
import csw.messages.params.generics.Key;
import csw.messages.params.generics.JKeyTypes;
import csw.messages.params.models.Id;
import csw.messages.params.models.ObsId;
import csw.messages.params.models.Prefix;
import csw.services.command.javadsl.JCommandService;
import csw.services.location.javadsl.*;
import scala.concurrent.duration.FiniteDuration;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static csw.services.location.javadsl.JComponentType.Assembly;

public class TcsTemplateJavaClient {

    Prefix source;
    ActorSystem system;
    ILocationService locationService;

    public TcsTemplateJavaClient(Prefix source, ActorSystem system, ILocationService locationService) throws Exception {

        this.source = source;
        this.system = system;
        this.locationService = locationService;

        commandServiceOptional = getAssemblyBlocking();
    }

    Optional<JCommandService> commandServiceOptional = Optional.empty();

    private Connection.AkkaConnection assemblyConnection = new Connection.AkkaConnection(new ComponentId("TcstemplatejavaAssembly", Assembly));


    private Key<String> targetTypeKey = JKeyTypes.StringKey().make("targetType");
    private Key<Double> wavelengthKey = JKeyTypes.DoubleKey().make("wavelength");
    private Key<String> axesKey = JKeyTypes.StringKey().make("axes");
    private Key<Double> azKey = JKeyTypes.DoubleKey().make("az");
    private Key<Double> elKey = JKeyTypes.DoubleKey().make("el");



    /**
     * Gets a reference to the running assembly from the location service, if found.
     */

    private Optional<JCommandService> getAssemblyBlocking() throws Exception {

        FiniteDuration waitForResolveLimit = new FiniteDuration(30, TimeUnit.SECONDS);

        Optional<AkkaLocation> resolveResult = locationService.resolve(assemblyConnection, waitForResolveLimit).get();

        if (resolveResult.isPresent()) {

            AkkaLocation akkaLocation = resolveResult.get();

            return Optional.of(new JCommandService(akkaLocation, Adapter.toTyped(system)));

        } else {
            return Optional.empty();
        }
    }




    /**
     * Sends a setTargetWavelength message to the Assembly and returns the response
     */
    public CompletableFuture<CommandResponse> setTargetWavelength(Optional<ObsId> obsId, String targetType, Double wavelength) throws Exception {

        //Optional<JCommandService> commandServiceOptional = getAssemblyBlocking();

        if (commandServiceOptional.isPresent()) {

            JCommandService commandService = commandServiceOptional.get();

            Setup setup = new Setup(source, new CommandName("setTargetWavelength"), obsId)
                    .add(targetTypeKey.set(targetType))
                    .add(wavelengthKey.set(wavelength));

            return commandService.submitAndSubscribe(setup, Timeout.durationToTimeout(FiniteDuration.apply(5, TimeUnit.SECONDS)));

        } else {

            return CompletableFuture.completedFuture(new CommandResponse.Error(new Id(""), "Can't locate Assembly"));
        }


    }


    /**
     * Sends a datum message to the Assembly and returns the response
     */
    public CompletableFuture<CommandResponse> datum(Optional<ObsId> obsId) throws Exception {

        //Optional<JCommandService> commandServiceOptional = getAssemblyBlocking();

        if (commandServiceOptional.isPresent()) {

            JCommandService commandService = commandServiceOptional.get();

            Setup setup = new Setup(source, new CommandName("datum"), obsId);

            return commandService.submitAndSubscribe(setup, Timeout.durationToTimeout(FiniteDuration.apply(20, TimeUnit.SECONDS)));

        } else {

            return CompletableFuture.completedFuture(new CommandResponse.Error(new Id(""), "Can't locate Assembly"));
        }


    }

}






