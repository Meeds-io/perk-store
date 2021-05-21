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
package org.exoplatform.perkstore.model;

import java.io.Serializable;

import org.exoplatform.perkstore.service.utils.Utils;

import groovy.transform.ToString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class UserSettings extends PerkStoreCloneable implements Serializable {

  private static final long serialVersionUID = 4866936020505632226L;

  private String            cometdChannel    = Utils.COMETD_CHANNEL;

  @Exclude
  private String            cometdToken;

  private String            cometdContext;

  private boolean           isAdministrator;

  private boolean           canAddProduct;

  @Override
  public UserSettings clone() { // NOSONAR
    return new UserSettings();
  }

}
