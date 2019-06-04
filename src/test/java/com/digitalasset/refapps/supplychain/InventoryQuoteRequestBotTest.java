/**
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 */
package com.digitalasset.refapps.supplychain;

import static org.junit.Assert.*;

import com.daml.ledger.javaapi.data.Command;
import com.daml.ledger.javaapi.data.ExerciseCommand;
import com.daml.ledger.javaapi.data.Template;
import com.daml.ledger.rxjava.components.LedgerViewFlowable;
import com.daml.ledger.rxjava.components.helpers.CommandsAndPendingSet;
import com.digitalasset.refapps.supplychain.util.CommandsAndPendingSetBuilder;
import da.refapps.supplychain.inventory.InventoryItem;
import da.refapps.supplychain.quoterequest.InventoryQuoteRequestBotTrigger;
import da.refapps.supplychain.types.OrderedProduct;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;
import org.pcollections.HashTreePMap;

public class InventoryQuoteRequestBotTest {
  private static final String PARTY = "TESTPARTY";

  CommandsAndPendingSetBuilder.Factory commandBuilder =
      CommandsAndPendingSetBuilder.factory("appId", Clock::systemUTC, Duration.ofSeconds(5));
  private LedgerViewFlowable.LedgerTestView<Template> ledgerView;
  private InventoryQuoteRequestBot bot = new InventoryQuoteRequestBot(commandBuilder, PARTY);

  @Test
  public void calculateCommands() throws InvocationTargetException, IllegalAccessException {
    ledgerView =
        new LedgerViewFlowable.LedgerTestView(
            HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty());

    // Delivery date: between LocalDate MIN and MAX
    OrderedProduct product = new OrderedProduct("Product1", 100L, LocalDate.MIN, LocalDate.MAX);
    InventoryQuoteRequestBotTrigger trigger1 =
        new InventoryQuoteRequestBotTrigger("workflow1", "warehouse1", "supplier", product);
    InventoryItem inventoryItem =
        new InventoryItem("warehouse1", "supplier", "Product1", 100L, BigDecimal.ONE);

    ledgerView =
        ledgerView
            .addActiveContract(InventoryQuoteRequestBotTrigger.TEMPLATE_ID, "cid-01", trigger1)
            .addActiveContract(InventoryItem.TEMPLATE_ID, "cid-02", inventoryItem);

    CommandsAndPendingSet cmds = bot.calculateCommands(ledgerView).blockingFirst();

    @NonNull List<Command> actualCommands = cmds.getSubmitCommandsRequest().getCommands();
    assertEquals(1, actualCommands.size());
    actualCommands.forEach(
        cmd -> {
          Optional<ExerciseCommand> exerciseCommand = cmd.asExerciseCommand();
          assertTrue(exerciseCommand.isPresent());
          assertEquals("InventoryQuoteRequestBotTrigger_Accept", exerciseCommand.get().getChoice());
        });
  }

  @Test(expected = IllegalStateException.class)
  public void calculateCommandsFailureWrongProduct()
      throws InvocationTargetException, IllegalAccessException {
    ledgerView =
        new LedgerViewFlowable.LedgerTestView(
            HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty());

    // Delivery date: between LocalDate MIN and MAX
    OrderedProduct product = new OrderedProduct("Product1", 100L, LocalDate.MIN, LocalDate.MAX);
    InventoryQuoteRequestBotTrigger trigger1 =
        new InventoryQuoteRequestBotTrigger("workflow1", "warehouse1", "supplier", product);
    InventoryItem inventoryItem =
        new InventoryItem("warehouse1", "supplier", "Product2", 100L, BigDecimal.ONE);

    ledgerView =
        ledgerView
            .addActiveContract(InventoryQuoteRequestBotTrigger.TEMPLATE_ID, "cid-01", trigger1)
            .addActiveContract(InventoryItem.TEMPLATE_ID, "cid-02", inventoryItem);

    CommandsAndPendingSet cmds = bot.calculateCommands(ledgerView).blockingFirst();
  }

  @Test(expected = IllegalStateException.class)
  public void calculateCommandsFailureWrongWarehouse()
      throws InvocationTargetException, IllegalAccessException {
    ledgerView =
        new LedgerViewFlowable.LedgerTestView(
            HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty(), HashTreePMap.empty());

    // Delivery date: between LocalDate MIN and MAX
    OrderedProduct product = new OrderedProduct("Product1", 100L, LocalDate.MIN, LocalDate.MAX);
    InventoryQuoteRequestBotTrigger trigger1 =
        new InventoryQuoteRequestBotTrigger("workflow1", "warehouse1", "supplier", product);
    InventoryItem inventoryItem =
        new InventoryItem("warehouse2", "supplier", "Product1", 100L, BigDecimal.ONE);

    ledgerView =
        ledgerView
            .addActiveContract(InventoryQuoteRequestBotTrigger.TEMPLATE_ID, "cid-01", trigger1)
            .addActiveContract(InventoryItem.TEMPLATE_ID, "cid-02", inventoryItem);

    CommandsAndPendingSet cmds = bot.calculateCommands(ledgerView).blockingFirst();
  }
}