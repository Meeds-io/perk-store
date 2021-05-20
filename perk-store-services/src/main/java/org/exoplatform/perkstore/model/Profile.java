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

import groovy.transform.ToString;
import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends PerkStoreCloneable implements Serializable {

  private static final long serialVersionUID = 3842109328846936552L;

  public Profile(long technicalId) {
    this.technicalId = technicalId;
  }

  public Profile(String id, String type) {
    this.id = id;
    this.type = type;
  }

  private String type;

  private String id;

  private long   technicalId;

  private long   spaceId;

  @Exclude
  private String spaceURLId;

  @Exclude
  private String displayName;

  @SuppressWarnings("all")
  @Override
  public Object clone() {
    return super.clone();
  }
}
