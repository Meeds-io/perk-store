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

import static org.exoplatform.perkstore.service.utils.Utils.getProductAccessUsersList;
import static org.exoplatform.perkstore.service.utils.Utils.getProductManagersUsersList;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.perkstore.model.*;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.perkstore.service.PerkStoreWebSocketService;
import org.exoplatform.services.listener.*;

@Asynchronous
public class WebSocketOrderListener extends Listener<Product, ProductOrderModification> {

  private PerkStoreService          perkStoreService;

  private PerkStoreWebSocketService webSocketService;

  @Override
  public void onEvent(Event<Product, ProductOrderModification> event) throws Exception {
    Product product = event.getSource();
    ProductOrderModification orderModification = event.getData();
    GlobalSettings globalSettings = getPerkStoreService().getGlobalSettings();

    Set<String> readOnlyUsers = new HashSet<>();
    boolean sendToAll = getProductAccessUsersList(readOnlyUsers, product, globalSettings);

    Set<String> orderDetailViewerUsers = new HashSet<>();
    getProductManagersUsersList(orderDetailViewerUsers, product, globalSettings);
    // Notify sender about the order changes
    ProductOrder updatedOrder = orderModification.getNewValue();
    orderDetailViewerUsers.add(updatedOrder.getSender().getId());

    readOnlyUsers.removeAll(orderDetailViewerUsers);
    // Send to simple users only product attributes modification
    getWebSocketService().sendMessage(event.getEventName(), readOnlyUsers, sendToAll, product);

    // Send to managers and order sender the order modifications
    getWebSocketService().sendMessage(event.getEventName(), orderDetailViewerUsers, false, product, updatedOrder);
  }

  public PerkStoreService getPerkStoreService() {
    if (perkStoreService == null) {
      perkStoreService = CommonsUtils.getService(PerkStoreService.class);
    }
    return perkStoreService;
  }

  private PerkStoreWebSocketService getWebSocketService() {
    if (webSocketService == null) {
      webSocketService = CommonsUtils.getService(PerkStoreWebSocketService.class);
    }
    return webSocketService;
  }
}
