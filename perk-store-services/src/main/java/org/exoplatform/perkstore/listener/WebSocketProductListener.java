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

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.perkstore.model.GlobalSettings;
import org.exoplatform.perkstore.model.Product;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.perkstore.service.PerkStoreWebSocketService;
import org.exoplatform.services.listener.*;

@Asynchronous
public class WebSocketProductListener extends Listener<Product, Boolean> {

  private PerkStoreWebSocketService webSocketService;

  private PerkStoreService          perkStoreService;

  @Override
  public void onEvent(Event<Product, Boolean> event) throws Exception {
    Product product = event.getSource();

    if (!product.isEnabled()) {
      return;
    }
    GlobalSettings globalSettings = getPerkStoreService().getGlobalSettings();

    Set<String> recipientUsers = new HashSet<>();
    boolean sendToAll = getProductAccessUsersList(recipientUsers, product, globalSettings);
    if (!sendToAll && product.getLastModifier() != null) {
      recipientUsers.add(product.getLastModifier().getId());
    }
    getWebSocketService().sendMessage(event.getEventName(), recipientUsers, sendToAll, product);
  }

  private PerkStoreWebSocketService getWebSocketService() {
    if (webSocketService == null) {
      webSocketService = CommonsUtils.getService(PerkStoreWebSocketService.class);
    }
    return webSocketService;
  }

  public PerkStoreService getPerkStoreService() {
    if (perkStoreService == null) {
      perkStoreService = CommonsUtils.getService(PerkStoreService.class);
    }
    return perkStoreService;
  }
}
