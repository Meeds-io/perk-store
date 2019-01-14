/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.addon.perkstore.notification.plugin;

import static org.exoplatform.addon.perkstore.service.utils.NotificationUtils.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
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
    if (mandatoryOrder && getOrderParameter(ctx) == null) {
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
    ProductOrder order = getOrderParameter(ctx);

    NotificationInfo notification = NotificationInfo.instance();
    notification.key(getId());

    setNotificationRecipients(notification, globalSettings, product, order, newProduct, newOrder);
    if (!notification.isSendAll() && (notification.getSendToUserIds() == null || notification.getSendToUserIds().isEmpty())) {
      if (LOG.isDebugEnabled()) {
        LOG.warn("Notification type '{}' doesn't have a recipient", getId());
      }
      return null;
    } else {
      storeSettingsParameters(globalSettings, notification);
      storeProductParameters(notification, product, newProduct);
      if (order != null) {
        storeOrderParameters(notification, order, newOrder);
      }
      return notification.end();
    }
  }

}
