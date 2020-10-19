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
package org.exoplatform.perkstore.notification.plugin;

import static org.exoplatform.perkstore.service.utils.NotificationUtils.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.perkstore.model.*;
import org.exoplatform.perkstore.model.constant.ProductOrderModificationType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class PerkStoreNotificationPlugin extends BaseNotificationPlugin {
  private static final Log LOG = ExoLogger.getLogger(PerkStoreNotificationPlugin.class);

  private String           notificationId;

  private boolean          newProduct;

  private boolean          newOrder;

  private boolean          mandatoryOrder;

  public PerkStoreNotificationPlugin(InitParams initParams) {
    super(initParams);
    ValueParam notificationIdParam = initParams.getValueParam("notification.id");
    if (notificationIdParam == null || StringUtils.isBlank(notificationIdParam.getValue())) {
      throw new IllegalStateException("'notification.id' parameter is mandatory");
    }
    this.notificationId = notificationIdParam.getValue();

    ValueParam newProductParam = initParams.getValueParam("product.new");
    if (newProductParam != null && StringUtils.isNotBlank(newProductParam.getValue())) {
      this.newProduct = Boolean.parseBoolean(newProductParam.getValue());
    }
    ValueParam mandatoryOrderParam = initParams.getValueParam("order.mandatory");
    if (mandatoryOrderParam != null && StringUtils.isNotBlank(mandatoryOrderParam.getValue())) {
      this.mandatoryOrder = Boolean.parseBoolean(mandatoryOrderParam.getValue());
    }
    ValueParam newOrderParam = initParams.getValueParam("order.new");
    if (newOrderParam != null && StringUtils.isNotBlank(newOrderParam.getValue())) {
      this.newOrder = Boolean.parseBoolean(newOrderParam.getValue());
    }
    if (newProduct && newOrder) {
      throw new IllegalStateException("The notification couldn't be of type newly added product and about a order that was paid");
    }
    if (newProduct && mandatoryOrder) {
      throw new IllegalStateException("The notification couldn't be of type newly added product and with a mandatory order");
    }
    if (newOrder && !mandatoryOrder) {
      throw new IllegalStateException("The notification couldn't be of type order payment without a mandatory order");
    }
  }

  @Override
  public String getId() {
    return this.notificationId;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    if (getProductParameter(ctx) == null) {
      LOG.warn("Notification type '{}' isn't valid because the product wasn't found", getId());
      return false;
    }
    if (newProduct != isNewProductParameter(ctx)) {
      LOG.warn("Notification type '{}' isn't valid because the product should be ",
               getId(),
               (newProduct ? "'a new product'" : "'an existing product'"));
      return false;
    }
    if (mandatoryOrder && getUpdatedOrderParameter(ctx) == null) {
      LOG.warn("Notification type '{}' isn't valid because the order wasn't found ", getId());
      return false;
    }
    if (newOrder != isNewOrderParameter(ctx)) {
      LOG.warn("Notification type '{}' isn't valid because the order should be ",
               getId(),
               (newOrder ? "'a new order'" : "'an existing order'"));

      return false;
    }
    return true;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    GlobalSettings globalSettings = getSettingsParameter(ctx);
    Product product = getProductParameter(ctx);
    ProductOrder updatedOrder = getUpdatedOrderParameter(ctx);
    ProductOrder oldOrder = getOldOrderParameter(ctx);
    Profile modifier = getModifierParameter(ctx);
    ProductOrderModificationType orderModificationType = getOrderModificationTypeParameter(ctx);

    NotificationInfo notification = NotificationInfo.instance();
    notification.key(getId());

    setNotificationRecipients(notification, globalSettings, product, updatedOrder, newProduct, newOrder, modifier);
    if (!notification.isSendAll() && (notification.getSendToUserIds() == null || notification.getSendToUserIds().isEmpty())) {
      LOG.debug("Notification type '{}' doesn't have a recipient", getId());
      return null;
    } else {
      storeSettingsParameters(globalSettings, notification);

      // This is made to avoid a special case: a product is created and never
      // modified and it receives orders
      boolean isNew = newProduct || (updatedOrder != null && product.getLastModifier() == null);
      storeProductParameters(notification, product, isNew);
      if (updatedOrder != null) {
        storeOrderParameters(notification, oldOrder, updatedOrder, orderModificationType, newOrder, modifier);
      }
      return notification.end();
    }
  }

}
