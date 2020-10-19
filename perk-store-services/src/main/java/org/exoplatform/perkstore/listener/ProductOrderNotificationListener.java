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

import static org.exoplatform.perkstore.service.utils.NotificationUtils.*;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.perkstore.model.Product;
import org.exoplatform.perkstore.model.ProductOrderModification;
import org.exoplatform.perkstore.model.constant.ProductOrderModificationType;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.services.listener.*;

@Asynchronous
public class ProductOrderNotificationListener extends Listener<Product, ProductOrderModification> {

  private ExoContainer     container;

  private PerkStoreService perkStoreService;

  public ProductOrderNotificationListener(ExoContainer container) {
    this.container = container;
  }

  @Override
  public void onEvent(Event<Product, ProductOrderModification> event) throws Exception {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      ProductOrderModification orderModification = event.getData();
      ProductOrderModificationType modificationType = orderModification.getModificationType();
      boolean isNew = modificationType == ProductOrderModificationType.NEW;

      NotificationContext ctx = NotificationContextImpl.cloneInstance();

      ctx.append(SETTINGS_PARAMETER, getPerkStoreService().getGlobalSettings());
      ctx.append(PRODUCT_PARAMETER, event.getSource());
      ctx.append(NEW_ORDER_PARAMETER, orderModification.getNewValue());
      if (orderModification.getOldValue() != null) {
        ctx.append(OLD_ORDER_PARAMETER, orderModification.getOldValue());
      }
      if (orderModification.getLastModifier() != null) {
        ctx.append(MODIFIER_PARAMETER, orderModification.getLastModifier());
      }
      ctx.append(ORDER_MODIFICATION_TYPE_PARAMETER, modificationType);
      ctx.append(ORDER_IS_NEW_PARAMETER, isNew);

      String pluginId = isNew ? PERKSTORE_ORDER_ADDED_NOTIFICATION_PLUGIN : PERKSTORE_ORDER_MODIFIED_NOTIFICATION_PLUGIN;

      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(pluginId))).execute(ctx);
    } finally {
      RequestLifeCycle.end();
    }
  }

  private PerkStoreService getPerkStoreService() {
    if (perkStoreService == null) {
      perkStoreService = container.getComponentInstanceOfType(PerkStoreService.class);
    }
    return perkStoreService;
  }

}
