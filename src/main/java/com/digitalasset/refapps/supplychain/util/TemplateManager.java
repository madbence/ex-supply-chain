/**
 * Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 */
package com.digitalasset.refapps.supplychain.util;

import com.daml.ledger.javaapi.data.Template;
import java.util.HashMap;
import java.util.Map;
import org.pcollections.PMap;

public class TemplateManager {

  /** Collects all template instances for a specific type. Need this method because of PMap... */
  public static <C extends Template> Map<String, C> filterTemplates(
      Class<C> type, PMap<String, ?> contracts) {
    HashMap<String, C> m = new HashMap<>();
    contracts.forEach(
        (s, c) -> {
          if (type.isInstance(c)) m.put(s, type.cast(c));
        });
    return m;
  }
}