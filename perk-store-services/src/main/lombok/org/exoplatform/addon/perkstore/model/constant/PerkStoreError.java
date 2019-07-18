package org.exoplatform.addon.perkstore.model.constant;

import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.io.Serializable;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.slf4j.helpers.MessageFormatter;

public enum PerkStoreError {

  // ORDER SHOULDN'T BE MODIFIED, it's embedded to ensure that code is always
  // the same
  GLOBAL_SETTINGS_ACCESS_DENIED(ERROR_SUFFIX_GLOBAL_SETTINGS_ACCESS, "User {} isn't alowed to access global settings", 1),
  GLOBAL_SETTINGS_MODIFICATION_DENIED(ERROR_SUFFIX_GLOBAL_SETTINGS_MODIFICATION, "User {} isn't alowed to modify global settings",
      1),
  PRODUCT_CREATION_DENIED(ERROR_SUFFIX_PRODUCT_CREATION, "User {} isn't alowed to create a product", 1),
  PRODUCT_MODIFICATION_DENIED(ERROR_SUFFIX_PRODUCT_MODIFICATION, "User {} isn't alowed to modify product {}", 2),
  PRODUCT_NOT_EXISTS(ERROR_SUFFIX_PRODUCT_NOT_EXISTS, "Product with id {} doesn't exists", 1),
  ORDER_CREATION_DENIED(ERROR_SUFFIX_ORDER_CREATION, "User {} isn't allowed to order on product {}", 2),
  ORDER_CREATION_STATUS_DENIED(ERROR_SUFFIX_ORDER_CREATION, "Order status should be automatically created by internal process"),
  ORDER_CREATION_EMPTY_TX(ERROR_SUFFIX_ORDER_CREATION, "Order transaction is mandatory"),
  ORDER_CREATION_EMPTY_QUANTITY(ERROR_SUFFIX_ORDER_CREATION, "Order quantity should be positive"),
  ORDER_CREATION_EMPTY_AMOUNT(ERROR_SUFFIX_ORDER_CREATION, "Order amount should be positive"),
  ORDER_CREATION_EMPTY_RECEIVER(ERROR_SUFFIX_ORDER_CREATION, "Order receiver is mandatory"),
  ORDER_CREATION_EMPTY_SENDER(ERROR_SUFFIX_ORDER_CREATION, "Order sender is mandatory"),
  ORDER_CREATION_QUANTITY_EXCEEDS_SUPPLY(ERROR_SUFFIX_ORDER_CREATION, "Ordered quantity by user {} exceeds available supply.", 1),
  ORDER_CREATION_QUANTITY_EXCEEDS_ALLOWED(ERROR_SUFFIX_ORDER_CREATION,
      "Ordered quantity by user {} exceeds allowed quantity per user.", 1),
  ORDER_MODIFICATION_DENIED(ERROR_SUFFIX_ORDER_MODIFICATION, "User {} isn't allowed to changed order on product '{}'", 2),
  ORDER_MODIFICATION_QUANTITY_INVALID_REMAINING(ERROR_SUFFIX_ORDER_MODIFICATION,
      "Remaining quantity to process {} isn't valid for order with id {}.", 2),
  ORDER_NOT_EXISTS(ERROR_SUFFIX_ORDER_NOT_EXISTS, "Order with id {} doesn't exists", 1),
  ORDER_CREATION_EMPTY_PRODUCT(ERROR_SUFFIX_ORDER_CREATION, "Order product is mandatory"),
  PRODUCT_IS_DISABLED(ERROR_SUFFIX_PRODUCT_IS_DISABLED, "Product with {} is disabled", 1),
  PRODUCT_ACCESS_DENIED(ERROR_SUFFIX_PRODUCT_ACCESS_DENIED, "Denied access to product '{}' for user {}", 2),
  ORDER_ACCESS_DENIED(ERROR_SUFFIX_ORDER_ACCESS_DENIED, "Denied access to order with id {} for user {}", 2);

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

  @SuppressWarnings("all")
  public String getErrorDescription(String message, boolean localizedMessage, Serializable... parameters) {
    this.checkValidParametersCount(parameters);

    String descriptionDisplay = StringUtils.isBlank(message) ? description : message;

    if (this.descriptionParametersCount == 0) {
      return descriptionDisplay;
    } else if (localizedMessage) {
      return MessageFormat.format(descriptionDisplay.replaceAll("'", "''"), parameters);
    } else {
      return MessageFormatter.arrayFormat(descriptionDisplay, parameters).getMessage();
    }
  }

  public String computeErrorMessage(String message, boolean localizedMessage, Serializable... parameters) {
    if (localizedMessage) {
      return getErrorDescription(message, localizedMessage, parameters);
    } else {
      return getErrorCode().toUpperCase() + ": " + getErrorDescription(message, localizedMessage, parameters);
    }
  }

  public String getErrorCode() {
    return (suffix == null ? "" : suffix) + "-" + code;
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
