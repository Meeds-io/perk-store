/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2026 Meeds Association
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.MockedStatic;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.perkstore.model.GlobalSettings;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.SimpleExoCache;
import org.exoplatform.services.listener.ListenerService;

/**
 * The global settings a node loaded stay on that node; a change made on any
 * node evicts them on every node.
 */
public class PerkStoreServiceCacheTest {

  @Test
  public void testTheLoadedSettingsStayOnThisNodeUntilEvicted() {
    SettingService settingService = mock(SettingService.class);
    ExoCache<String, GlobalSettings> cache = spy(new SimpleExoCache<>());
    CacheService cacheService = mock(CacheService.class);
    when(cacheService.<String, GlobalSettings> getCacheInstance(PerkStoreService.GLOBAL_SETTINGS_CACHE_NAME)).thenReturn(cache);
    PerkStoreService service = new PerkStoreService(null, null, settingService, null, cacheService);

    service.getGlobalSettings();
    service.getGlobalSettings();
    // As the eviction a change on another node sends
    cache.clearCache();
    service.getGlobalSettings();

    verify(settingService, times(2)).get(any(), any(), any());
    verify(cache, times(2)).putLocal(any(), any());
    verify(cache, never()).put(any(), any());
  }

  @Test
  public void testAChangeOfTheSettingsEvictsThemOnEveryNode() throws Exception {
    SettingService settingService = mock(SettingService.class);
    ExoCache<String, GlobalSettings> cache = spy(new SimpleExoCache<>());
    CacheService cacheService = mock(CacheService.class);
    when(cacheService.<String, GlobalSettings> getCacheInstance(PerkStoreService.GLOBAL_SETTINGS_CACHE_NAME)).thenReturn(cache);
    PerkStoreService service = spy(new PerkStoreService(null, null, settingService, null, cacheService));
    doReturn(true).when(service).isPerkStoreManager("manager");
    service.getGlobalSettings();

    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class)) {
      commonsUtils.when(() -> CommonsUtils.getService(ListenerService.class)).thenReturn(mock(ListenerService.class));
      service.saveGlobalSettings(new GlobalSettings(), "manager");
    }

    verify(settingService).set(any(), any(), any(), any());
    // A cluster-wide removal, not this node's only
    verify(cache).remove(any());
    verify(cache, never()).removeLocal(any());
  }
}
