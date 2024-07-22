/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.perkstore.plugin;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.services.security.Identity;

import io.meeds.portal.permlink.model.PermanentLinkObject;
import io.meeds.portal.permlink.plugin.PermanentLinkPlugin;

/**
 * Perk Store Permanent link generation
 */
public class PerkStorePermanentLinkPlugin implements PermanentLinkPlugin {

  public static final String      OBJECT_TYPE        = "perkstore";

  public static final String      PRODUCT_URL_FORMAT = "/portal/%s/perkstore?productId=%s";

  public static final String      ORDER_URL_FORMAT   = "/portal/%s/perkstore?productId=%s&orderId=%s";

  private UserPortalConfigService portalConfigService;

  public PerkStorePermanentLinkPlugin(UserPortalConfigService portalConfigService) {
    this.portalConfigService = portalConfigService;
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean canAccess(PermanentLinkObject object, Identity identity) throws ObjectNotFoundException {
    return true;
  }

  @Override
  public String getDirectAccessUrl(PermanentLinkObject object) throws ObjectNotFoundException {
    String productId = object.getObjectId();
    if (object.getParameters() != null
        && object.getParameters().containsKey("orderId")) {
      return String.format(ORDER_URL_FORMAT,
                           portalConfigService.getMetaPortal(),
                           productId,
                           object.getParameters().get("orderId"));
    } else {
      return String.format(PRODUCT_URL_FORMAT, portalConfigService.getMetaPortal(), productId);
    }
  }

}
