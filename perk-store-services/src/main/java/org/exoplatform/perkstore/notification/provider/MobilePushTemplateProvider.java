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
package org.exoplatform.perkstore.notification.provider;

import static org.exoplatform.perkstore.service.utils.NotificationUtils.*;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.perkstore.notification.builder.PerkStoreTemplateBuilder;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = PERKSTORE_PRODUCT_ADDED_NOTIFICATION_PLUGIN, template = "war:/conf/perk-store/templates/notification/push/ProductPushPlugin.gtmpl"),
    @TemplateConfig(pluginId = PERKSTORE_PRODUCT_MODIFIED_NOTIFICATION_PLUGIN, template = "war:/conf/perk-store/templates/notification/push/ProductPushPlugin.gtmpl"),
    @TemplateConfig(pluginId = PERKSTORE_ORDER_ADDED_NOTIFICATION_PLUGIN, template = "war:/conf/perk-store/templates/notification/push/OrderPushPlugin.gtmpl"),
    @TemplateConfig(pluginId = PERKSTORE_ORDER_MODIFIED_NOTIFICATION_PLUGIN, template = "war:/conf/perk-store/templates/notification/push/OrderPushPlugin.gtmpl"),
})
public class MobilePushTemplateProvider extends TemplateProvider {

  public MobilePushTemplateProvider(ExoContainer container, InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PRODUCT_ADDED_KEY,
                              new PerkStoreTemplateBuilder(this, container, PRODUCT_ADDED_KEY, true));
    this.templateBuilders.put(PRODUCT_MODIFIED_KEY,
                              new PerkStoreTemplateBuilder(this,
                                                           container,
                                                           PRODUCT_MODIFIED_KEY,
                                                           true));
    this.templateBuilders.put(ORDER_ADDED_KEY,
                              new PerkStoreTemplateBuilder(this,
                                                           container,
                                                           ORDER_ADDED_KEY,
                                                           true,
                                                           true));
    this.templateBuilders.put(ORDER_MODIFIED_KEY,
                              new PerkStoreTemplateBuilder(this,
                                                           container,
                                                           ORDER_MODIFIED_KEY,
                                                           true,
                                                           true));
  }

}
