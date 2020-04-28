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
package org.exoplatform.perkstore.plugin;

import java.util.List;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.api.settings.FeaturePlugin;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.perkstore.model.GlobalSettings;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.perkstore.service.utils.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.*;

/**
 * A plugin added to {@link ExoFeatureService} that determines if 'perkstore' is
 * enabled for a user or not
 */
public class PerkStoreFeaturePlugin extends FeaturePlugin {
  private static final Log     LOG                     = ExoLogger.getLogger(PerkStoreService.class);

  private static final String  PERK_STORE_FEATURE_NAME = "perkstore";

  private static final String  PERKSTORE_ENABLED_FLAG  = "perkstore.enabled";

  private ConversationRegistry conversationRegistry;

  private PerkStoreService     perkStoreService;

  @Override
  public String getName() {
    return PERK_STORE_FEATURE_NAME;
  }

  @Override
  public boolean isFeatureActiveForUser(String featureName, String username) {
    List<StateKey> stateKeys = getConversationRegistry().getStateKeys(username);
    for (StateKey stateKey : stateKeys) {
      ConversationState state = getConversationRegistry().getState(stateKey);
      Boolean perkStoreEnabled = (Boolean) state.getAttribute(PERKSTORE_ENABLED_FLAG);
      if (perkStoreEnabled != null) {
        return perkStoreEnabled;
      }
    }
    boolean perkStoreEnabled = false;
    try {
      GlobalSettings settings = getPerkStoreService().getGlobalSettings();
      if (settings == null) {
        return false;
      }
      perkStoreEnabled = Utils.hasPermission(username, settings.getAccessPermissions());
      for (StateKey stateKey : stateKeys) {
        ConversationState state = getConversationRegistry().getState(stateKey);
        state.setAttribute(PERKSTORE_ENABLED_FLAG, perkStoreEnabled);
      }
    } catch (Exception e) {
      LOG.error("Error while checking user ACL on Perk Store Feature", e);
    }
    return perkStoreEnabled;
  }

  /**
   * The Service can't be injected by constructor to avoid cyclic dependency
   * 
   * @return instance of {@link ConversationRegistry} injected in current
   *         container
   */
  private ConversationRegistry getConversationRegistry() {
    if (conversationRegistry == null) {
      conversationRegistry = CommonsUtils.getService(ConversationRegistry.class);
    }
    return conversationRegistry;
  }

  /**
   * The Service can't be injected by constructor to avoid cyclic dependency
   * 
   * @return instance of {@link PerkStoreService} injected in current container
   */
  private PerkStoreService getPerkStoreService() {
    if (perkStoreService == null) {
      perkStoreService = CommonsUtils.getService(PerkStoreService.class);
    }
    return perkStoreService;
  }

}
