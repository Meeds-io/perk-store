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

import lombok.*;
import lombok.EqualsAndHashCode.Exclude;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class FileDetail extends PerkStoreCloneable implements Serializable {

  private static final long serialVersionUID = 3118275951807957785L;

  public FileDetail(long id) {
    this.id = id;
  }

  private long   id;

  private String uploadId;

  @Exclude
  private String name;

  @Exclude
  private long   size;

  @Exclude
  private String src;

  @Exclude
  private byte[] data;

  @Exclude
  private long   lastUpdated;

}
