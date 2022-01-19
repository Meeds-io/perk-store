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
package org.exoplatform.perkstore.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.perkstore.model.constant.ProductOrderPeriodType;

@Entity(name = "Product")
@ExoEntity
@Table(name = "ADDONS_PERKSTORE_PRODUCT")
@NamedQueries({ @NamedQuery(name = "Product.getAllProducts", query = "select p from Product p ORDER BY p.createdDate DESC") })
public class ProductEntity implements Serializable {

  private static final long      serialVersionUID = -592052513482849972L;

  @Id
  @SequenceGenerator(name = "SEQ_ADDONS_PERKSTORE_PRODUCT_ID", sequenceName = "SEQ_ADDONS_PERKSTORE_PRODUCT_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_ADDONS_PERKSTORE_PRODUCT_ID")
  @Column(name = "PRODUCT_ID")
  private Long                   id;

  @Column(name = "TITLE", nullable = false)
  private String                 title;

  @Column(name = "DESCRIPTION", nullable = true)
  private String                 description;

  @Column(name = "ILLUSTRATION_URL", nullable = true)
  private String                 illustrationURL;

  @Column(name = "ENABLED", nullable = false)
  private boolean                enabled;

  @Column(name = "UNLIMITED", nullable = false)
  private boolean                unlimited;

  @Column(name = "ALLOW_FRACTION", nullable = false)
  private boolean                allowFraction;

  @Column(name = "TOTAL_SUPPLY", nullable = false)
  private double                 totalSupply;

  @Column(name = "PRICE", nullable = false)
  private double                 price;

  @Column(name = "RECEIVER_ID", nullable = false)
  private long                   receiverId;

  @Column(name = "PERIODICITY")
  private ProductOrderPeriodType orderPeriodicity;

  @Column(name = "MAX_ORDERS_PER_USER", nullable = false)
  private double                 maxOrdersPerUser;

  @Column(name = "CREATED_DATE", nullable = false)
  private long                   createdDate;

  @Column(name = "LAST_MODIFIED_DATE", nullable = true)
  private long                   lastModifiedDate;

  @Column(name = "CREATOR", nullable = false)
  private long                   creator;

  @Column(name = "LAST_MODIFIER", nullable = true)
  private long                   lastModifier;

  @ElementCollection
  @CollectionTable(name = "ADDONS_PERKSTORE_PRODUCT_MARCHAND", joinColumns = @JoinColumn(name = "PRODUCT_ID"))
  @Column(name = "MARCHAND_IDENTITY_ID")
  private List<Long>             marchands;

  @ElementCollection
  @CollectionTable(name = "ADDONS_PERKSTORE_PRODUCT_PERMISSION", joinColumns = @JoinColumn(name = "PRODUCT_ID"))
  @Column(name = "PERMISSION_IDENTITY_ID")
  private List<Long>             accessPermissions;

  @ElementCollection
  @CollectionTable(name = "ADDONS_PERKSTORE_IMAGE", joinColumns = @JoinColumn(name = "PRODUCT_ID"))
  @Column(name = "IMAGE_FILE_ID")
  private Set<Long>              images;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getIllustrationURL() {
    return illustrationURL;
  }

  public void setIllustrationURL(String illustrationURL) {
    this.illustrationURL = illustrationURL;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isUnlimited() {
    return unlimited;
  }

  public void setUnlimited(boolean unlimited) {
    this.unlimited = unlimited;
  }

  public boolean isAllowFraction() {
    return allowFraction;
  }

  public void setAllowFraction(boolean allowFraction) {
    this.allowFraction = allowFraction;
  }

  public double getTotalSupply() {
    return totalSupply;
  }

  public void setTotalSupply(double totalSupply) {
    this.totalSupply = totalSupply;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public long getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(long receiverId) {
    this.receiverId = receiverId;
  }

  public List<Long> getMarchands() {
    return marchands;
  }

  public void setMarchands(List<Long> marchands) {
    this.marchands = marchands;
  }

  public List<Long> getAccessPermissions() {
    return accessPermissions;
  }

  public void setAccessPermissions(List<Long> accessPermissions) {
    this.accessPermissions = accessPermissions;
  }

  public ProductOrderPeriodType getOrderPeriodicity() {
    return orderPeriodicity;
  }

  public void setOrderPeriodicity(ProductOrderPeriodType orderPeriodicity) {
    this.orderPeriodicity = orderPeriodicity;
  }

  public double getMaxOrdersPerUser() {
    return maxOrdersPerUser;
  }

  public void setMaxOrdersPerUser(double maxOrdersPerUser) {
    this.maxOrdersPerUser = maxOrdersPerUser;
  }

  public long getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(long createdDate) {
    this.createdDate = createdDate;
  }

  public long getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(long lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public long getCreator() {
    return creator;
  }

  public void setCreator(long creator) {
    this.creator = creator;
  }

  public long getLastModifier() {
    return lastModifier;
  }

  public void setLastModifier(long lastModifier) {
    this.lastModifier = lastModifier;
  }

  public Set<Long> getImages() {
    return images;
  }

  public void setImages(Set<Long> images) {
    this.images = images;
  }

}
