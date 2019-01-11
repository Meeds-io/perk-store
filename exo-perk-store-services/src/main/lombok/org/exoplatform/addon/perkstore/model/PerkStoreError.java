package org.exoplatform.addon.perkstore.model;

import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

public enum PerkStoreError {

  GLOBAL_SETTINGS_ACCESS_DENIED(ERROR_SUFFIX_GLOBAL_SETTINGS_MODIFICATION, "User {} isn't alowed to access global settings", 1),
  GLOBAL_SETTINGS_MODIFICATION_DENIED(ERROR_SUFFIX_GLOBAL_SETTINGS_MODIFICATION, "User {} isn't alowed to modify global settings",
      1),
  PRODUCT_CREATION_DENIED(ERROR_SUFFIX_PRODUCT_CREATION, "User {} isn't alowed to create a product", 1),
  PRODUCT_MODIFICATION_DENIED(ERROR_SUFFIX_PRODUCT_MODIFICATION, "User {} isn't alowed to modify product {}", 2),
  PRODUCT_NOT_EXISTS(ERROR_SUFFIX_PRODUCT_NOT_EXISTS, "Product with id {} doesn't exists", 1),
  ORDER_MODIFICATION_DENIED(ERROR_SUFFIX_ORDER_MODIFICATION, "User {} isn't allowed to order on product with id {}", 2),
  ORDER_NOT_EXISTS(ERROR_SUFFIX_ORDER_NOT_EXISTS, "Order with id {} doesn't exists", 1),
  ORDER_CREATION_DENIED(ERROR_SUFFIX_ORDER_CREATION, "User {} isn't allowed to order on product {}", 2),
  ORDER_CREATION_STATUS_DENIED(ERROR_SUFFIX_ORDER_CREATION, "Order status should be automatically created by internal process"),
  ORDER_CREATION_EMPTY_TX(ERROR_SUFFIX_ORDER_CREATION, "Order transaction is mandatory"),
  ORDER_CREATION_EMPTY_QUANTITY(ERROR_SUFFIX_ORDER_CREATION, "Order quantity should be positive"),
  ORDER_CREATION_EMPTY_AMOUNT(ERROR_SUFFIX_ORDER_CREATION, "Order amount should be positive"),
  ORDER_CREATION_EMPTY_RECEIVER(ERROR_SUFFIX_ORDER_CREATION, "Order receiver is mandatory"),
  ORDER_CREATION_QUANTITY_EXCEEDS_SUPPLY(ERROR_SUFFIX_ORDER_CREATION, "Ordered quantity by user {} exceeds available supply.", 1),
  ORDER_CREATION_QUANTITY_EXCEEDS_ALLOWED(ERROR_SUFFIX_ORDER_CREATION,
      "Ordered quantity by user {} exceeds allowed quantity per user.", 1),
  ORDER_MODIFICATION_QUANTITY_INVALID_REMAINING(ERROR_SUFFIX_ORDER_MODIFICATION,
      "Remaining quantity to process {} isn't valid for order with id {}.", 2);

  private int    code;

  private String suffix;

  private String description;

  private int    descriptionParametersCount;

  private PerkStoreError(String errorSuffix, String description) {
    this(errorSuffix, description, 0);
  }

  private PerkStoreError(String errorSuffix, String description, int descriptionParametersCount) {
    if (StringUtils.isBlank(description)) {
      throw new IllegalArgumentException("Description is mandatory");
    }
    if (StringUtils.isBlank(errorSuffix)) {
      throw new IllegalArgumentException("Error suffix is mandatory");
    }

    this.code = this.ordinal() + 1;
    this.suffix = errorSuffix;
    this.description = description;
    this.descriptionParametersCount = descriptionParametersCount;
  }

  public String getDescription(Serializable... parameters) {
    this.checkValidParametersCount(parameters);
    if (descriptionParametersCount == 0) {
      return description;
    } else {
      String descriptionDisplay = description;
      for (Object parameter : parameters) {
        descriptionDisplay = descriptionDisplay.replaceFirst("\\{\\}", String.valueOf(parameter));
      }
      return descriptionDisplay;
    }
  }

  public String getMessage(Serializable... parameters) {
    return suffix.toUpperCase() + "-" + code + ": " + getDescription(parameters);
  }

  public void checkValidParametersCount(Serializable... parameters) {
    if ((parameters == null && descriptionParametersCount > 0)
        || (parameters != null && parameters.length != descriptionParametersCount)) {
      throw new IllegalArgumentException("Parameters count should be " + descriptionParametersCount);
    }
  }

  public int getCode() {
    return code;
  }

  public String getSuffix() {
    return suffix;
  }
}
