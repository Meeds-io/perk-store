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
package org.exoplatform.perkstore.notification.provider;

import static org.exoplatform.perkstore.service.utils.NotificationUtils.*;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.perkstore.notification.builder.PerkStoreTemplateBuilder;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = PERKSTORE_PRODUCT_ADDED_NOTIFICATION_PLUGIN, template = "war:/conf/perk-store/templates/notification/mail/ProductMailPlugin.gtmpl"),
    @TemplateConfig(pluginId = PERKSTORE_PRODUCT_MODIFIED_NOTIFICATION_PLUGIN, template = "war:/conf/perk-store/templates/notification/mail/ProductMailPlugin.gtmpl"),
    @TemplateConfig(pluginId = PERKSTORE_ORDER_ADDED_NOTIFICATION_PLUGIN, template = "war:/conf/perk-store/templates/notification/mail/OrderMailPlugin.gtmpl"),
    @TemplateConfig(pluginId = PERKSTORE_ORDER_MODIFIED_NOTIFICATION_PLUGIN, template = "war:/conf/perk-store/templates/notification/mail/OrderMailPlugin.gtmpl"),
})
public class MailTemplateProvider extends TemplateProvider {

  public MailTemplateProvider(ExoContainer container, InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PRODUCT_ADDED_KEY,
                              new PerkStoreTemplateBuilder(this, container, PRODUCT_ADDED_KEY, false));
    this.templateBuilders.put(PRODUCT_MODIFIED_KEY,
                              new PerkStoreTemplateBuilder(this, container, PRODUCT_MODIFIED_KEY, false));
    this.templateBuilders.put(ORDER_ADDED_KEY,
                              new PerkStoreTemplateBuilder(this, container, ORDER_ADDED_KEY, true, false));
    this.templateBuilders.put(ORDER_MODIFIED_KEY,
                              new PerkStoreTemplateBuilder(this, container, ORDER_MODIFIED_KEY, true, false));

  }
}
