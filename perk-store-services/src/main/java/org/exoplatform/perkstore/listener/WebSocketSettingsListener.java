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

import static org.exoplatform.perkstore.service.utils.Utils.getApplicationAccessUsersList;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.perkstore.model.GlobalSettings;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.perkstore.service.PerkStoreWebSocketService;
import org.exoplatform.services.listener.*;

@Asynchronous
public class WebSocketSettingsListener extends Listener<PerkStoreService, GlobalSettings> {

  private PerkStoreWebSocketService webSocketService;

  @Override
  public void onEvent(Event<PerkStoreService, GlobalSettings> event) throws Exception {
    GlobalSettings globalSettings = event.getData();

    Set<String> recipientUsers = new HashSet<>();
    boolean sendToAll = getApplicationAccessUsersList(recipientUsers, globalSettings);

    getWebSocketService().sendMessage(event.getEventName(), recipientUsers, sendToAll, globalSettings);
  }

  private PerkStoreWebSocketService getWebSocketService() {
    if (webSocketService == null) {
      webSocketService = CommonsUtils.getService(PerkStoreWebSocketService.class);
    }
    return webSocketService;
  }

}
