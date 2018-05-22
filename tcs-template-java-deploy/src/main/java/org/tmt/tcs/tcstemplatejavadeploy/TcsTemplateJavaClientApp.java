package org.tmt.tcs.tcstemplatejavadeploy;

import akka.actor.ActorSystem;
import akka.util.Timeout;
import csw.messages.commands.CommandResponse;
import csw.messages.params.models.ObsId;
import csw.messages.params.models.Prefix;
import csw.services.location.commons.ClusterAwareSettings;
import csw.services.location.javadsl.ILocationService;
import csw.services.location.javadsl.JLocationServiceFactory;

import csw.services.logging.javadsl.JLoggingSystemFactory;
import scala.concurrent.Await;
import org.tmt.tcs.tcstemplatejavaclient.*;
import scala.concurrent.duration.FiniteDuration;

import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TcsTemplateJavaClientApp {

    public static void main(String[] args) throws Exception {

        ActorSystem system = ClusterAwareSettings.system();
        ILocationService locationService = JLocationServiceFactory.make();

        TcsTemplateJavaClient tcsTemplateJavaClient   = new TcsTemplateJavaClient(new Prefix("tcs.tcs-template"), system, locationService);
        Optional<ObsId> maybeObsId          = Optional.empty();
        String hostName                = InetAddress.getLocalHost().getHostName();
        JLoggingSystemFactory.start("TcsTemplateClientApp", "0.1", hostName, system);


        CompletableFuture<CommandResponse> cf1 = tcsTemplateJavaClient.setTargetWavelength(maybeObsId, "GUIDESTAR", 867.4);
        CommandResponse resp1 = cf1.get();
        System.out.println("setTargetWavelength: " + resp1);

        CompletableFuture<CommandResponse> cf2 = tcsTemplateJavaClient.datum(maybeObsId);
        CommandResponse resp2 = cf2.get();
        System.out.println("datum: " + resp2);

    }
}
