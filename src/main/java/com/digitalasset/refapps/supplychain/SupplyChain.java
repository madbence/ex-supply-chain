/**
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 */
package com.digitalasset.refapps.supplychain;

import com.daml.ledger.rxjava.DamlLedgerClient;
import com.daml.ledger.rxjava.components.Bot;
import com.digitalasset.refapps.supplychain.util.CliOptions;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.digitalasset.refapps.supplychain.util.CommandsAndPendingSetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupplyChain {
  public static final String APPLICATION_ID = "direct-asset-control";
  private static final Logger logger = LoggerFactory.getLogger(SupplyChain.class);

  private static final String SELLER_PARTY = "Seller";
  private static final String SUPPLIER_PARTY = "Supplier";
  private static final String TRANSPORT_PARTY1 = "TransportCompany1";
  private static final String TRANSPORT_PARTY2 = "TransportCompany2";

  private static final AtomicReference<Clock> clock =
        new AtomicReference<>(Clock.fixed(Instant.ofEpochSecond(0), ZoneId.systemDefault()));

  public static void main(String[] args) {
    CliOptions options = CliOptions.parseArgs(args);
    //    MulticastProcessor<Instant> time = MulticastProcessor.create();

    DamlLedgerClient client =
        DamlLedgerClient.forHostWithLedgerIdDiscovery(
            options.getSandboxHost(), options.getSandboxPort(), Optional.empty());

    waitForSandbox(options, client);

    logger.info("Sandbox is started on port: {}", options.getSandboxPort());

    //    Flowable<Instant> clockFlowable =
    //        client
    //            .getTimeClient()
    //            .getTime()
    //            .doOnNext(ts -> logger.info("Received time change {}", ts))
    //            .doOnNext(ts -> clock.set(Clock.fixed(ts, ZoneId.systemDefault())));

    Duration mrt = Duration.ofSeconds(10);
    CommandsAndPendingSetBuilder.Factory commandBuilderFactory =
        CommandsAndPendingSetBuilder.factory(APPLICATION_ID, clock::get, mrt);

    AggregatedQuoteBot aggregatedQuoteBot = new AggregatedQuoteBot(commandBuilderFactory, SELLER_PARTY);
    Bot.wire(APPLICATION_ID,
             client,
             aggregatedQuoteBot.transactionFilter,
             aggregatedQuoteBot::calculateCommands,
             aggregatedQuoteBot::getContractInfo);

    ChooseTransportBot chooseTransportBot = new ChooseTransportBot(commandBuilderFactory, SUPPLIER_PARTY);
    Bot.wire(APPLICATION_ID,
            client,
            chooseTransportBot.transactionFilter,
            chooseTransportBot::calculateCommands,
            chooseTransportBot::getContractInfo);

    DeliveryCompleteBot transportCapacityReleaseBot = new DeliveryCompleteBot(commandBuilderFactory, SELLER_PARTY);
    Bot.wire(APPLICATION_ID,
            client,
            transportCapacityReleaseBot.transactionFilter,
            transportCapacityReleaseBot::calculateCommands,
            transportCapacityReleaseBot::getContractInfo);
  }

  public static void waitForSandbox(CliOptions options, DamlLedgerClient client) {
    waitForSandbox(options.getSandboxHost(), options.getSandboxPort(), client);
  }

  public static void waitForSandbox(String host, int port, DamlLedgerClient client) {
    boolean connected = false;
    while (!connected) {
      try {
        client.connect();
        connected = true;
      } catch (Exception _ignored) {
        logger.info(String.format("Connecting to sandbox at %s:%s", host, port));
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
      }
    }
  }
}
