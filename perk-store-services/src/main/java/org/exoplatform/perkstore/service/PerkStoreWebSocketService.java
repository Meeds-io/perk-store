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
package org.exoplatform.perkstore.service;

import static org.exoplatform.perkstore.service.utils.Utils.COMETD_CHANNEL;
import static org.exoplatform.perkstore.service.utils.Utils.transformToString;

import java.util.Collection;

import org.mortbay.cometd.continuation.EXoContinuationBayeux;
import org.picocontainer.Startable;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.perkstore.model.WebSocketMessage;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

public class PerkStoreWebSocketService implements Startable {

  private static final Log      LOG = ExoLogger.getLogger(PerkStoreWebSocketService.class);

  private ContinuationService   continuationService;

  private EXoContinuationBayeux continuationBayeux;

  private String                cometdContextName;

  public PerkStoreWebSocketService() {
    // Services looked up on first use
  }

  PerkStoreWebSocketService(EXoContinuationBayeux continuationBayeux) {
    this.continuationBayeux = continuationBayeux;
  }

  @Override
  public void start() {
    EXoContinuationBayeux bayeux = getContinuationBayeux();
    if (bayeux != null) {
      // A message sent to all reaches the users of the other nodes of a cluster
      bayeux.observeChannel(COMETD_CHANNEL);
    }
  }

  @Override
  public void stop() {
    // Nothing to stop
  }

  public void sendMessage(String eventId, Collection<String> recipientUsers, boolean sendToAll, Object... objects) {
    getContinuationService();

    WebSocketMessage messageObject = new WebSocketMessage(eventId, objects);
    String message = transformToString(messageObject);

    if (sendToAll) {
      continuationService.sendBroadcastMessage(COMETD_CHANNEL, message);
    } else {
      for (String recipientUser : recipientUsers) {
        if (continuationService.isPresent(recipientUser)) {
          continuationService.sendMessage(recipientUser, COMETD_CHANNEL, message);
        }
      }
    }
  }

  protected String getCometdContextName() {
    if (cometdContextName == null) {
      getContinuationBayeux();
      cometdContextName = (continuationBayeux == null ? "cometd" : continuationBayeux.getCometdContextName());
    }
    return cometdContextName;
  }

  protected String getUserToken(String username) {
    try {
      return getContinuationService().getUserToken(username);
    } catch (Exception e) {
      LOG.warn("Could not retrieve continuation token for user " + username, e);
      return "";
    }
  }

  private EXoContinuationBayeux getContinuationBayeux() {
    if (continuationBayeux == null) {
      continuationBayeux = CommonsUtils.getService(EXoContinuationBayeux.class);
    }
    return continuationBayeux;
  }

  private ContinuationService getContinuationService() {
    if (continuationService == null) {
      continuationService = CommonsUtils.getService(ContinuationService.class);
    }
    return continuationService;
  }
}
