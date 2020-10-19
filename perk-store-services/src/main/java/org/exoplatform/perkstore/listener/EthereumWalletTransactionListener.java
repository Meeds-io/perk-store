/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.perkstore.listener;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EthereumWalletTransactionListener extends Listener<Object, Map<String, Object>> {

  private static final Log LOG = ExoLogger.getLogger(EthereumWalletTransactionListener.class);

  private ExoContainer     container;

  private PerkStoreService perkStoreService;

  public EthereumWalletTransactionListener(PerkStoreService perkStoreService, ExoContainer container) {
    this.perkStoreService = perkStoreService;
    this.container = container;
  }

  @Override
  public void onEvent(Event<Object, Map<String, Object>> event) throws Exception {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    String hash = null;
    try {
      Map<String, Object> transactionDetails = event.getData();
      if (transactionDetails == null) {
        throw new IllegalStateException("Transaction details is mandatory");
      }
      hash = (String) transactionDetails.get("hash");
      if (StringUtils.isBlank(hash)) {
        throw new IllegalStateException("Transaction hash is mandatory");
      }
      String oldHash = (String) transactionDetails.get("oldHash");
      if (StringUtils.isNotBlank(oldHash)) {
        this.perkStoreService.replaceTransactions(oldHash, hash);
      }
      this.perkStoreService.saveOrderTransactionStatus(transactionDetails);
    } catch (Exception e) {
      LOG.error("Error saving order details after transaction {} gets mined", hash, e);
    } finally {
      RequestLifeCycle.end();
    }
  }

}
