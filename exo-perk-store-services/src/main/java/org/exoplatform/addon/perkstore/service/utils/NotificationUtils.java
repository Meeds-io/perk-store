package org.exoplatform.addon.perkstore.service.utils;

import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.util.*;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.addon.perkstore.model.constant.ProductOrderModificationType;
import org.exoplatform.addon.perkstore.model.constant.ProductOrderStatus;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.*;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class NotificationUtils {

  public static final ArgumentLiteral<GlobalSettings> SETTINGS_PARAMETER                             =
                                                                         new ArgumentLiteral<>(GlobalSettings.class, "settings");

  public static final ArgumentLiteral<Product>        PRODUCT_PARAMETER                              =
                                                                        new ArgumentLiteral<>(Product.class, "product");

  public static final ArgumentLiteral<Boolean>        PRODUCT_IS_NEW_PARAMETER                       =
                                                                               new ArgumentLiteral<>(Boolean.class,
                                                                                                     "product.new");

  public static final ArgumentLiteral<ProductOrder>   ORDER_PARAMETER                                =
                                                                      new ArgumentLiteral<>(ProductOrder.class, "order");

  public static final ArgumentLiteral<Boolean>        ORDER_IS_NEW_PARAMETER                         =
                                                                             new ArgumentLiteral<>(Boolean.class, "order.new");

  // Notification plugins ids

  public static final String                          PERKSTORE_PRODUCT_ADDED_NOTIFICATION_PLUGIN    =
                                                                                                  "ProductAddedNotificationPlugin";

  public static final String                          PERKSTORE_PRODUCT_MODIFIED_NOTIFICATION_PLUGIN =
                                                                                                     "ProductModifiedNotificationPlugin";

  public static final String                          PERKSTORE_ORDER_ADDED_NOTIFICATION_PLUGIN      =
                                                                                                "OrderAddedNotificationPlugin";

  public static final String                          PERKSTORE_ORDER_MODIFIED_NOTIFICATION_PLUGIN   =
                                                                                                   "OrderModifiedNotificationPlugin";

  public static final PluginKey                       ORDER_MODIFIED_KEY                             =
                                                                         PluginKey.key(PERKSTORE_ORDER_MODIFIED_NOTIFICATION_PLUGIN);

  public static final PluginKey                       ORDER_ADDED_KEY                                =
                                                                      PluginKey.key(PERKSTORE_ORDER_ADDED_NOTIFICATION_PLUGIN);

  public static final PluginKey                       PRODUCT_MODIFIED_KEY                           =
                                                                           PluginKey.key(PERKSTORE_PRODUCT_MODIFIED_NOTIFICATION_PLUGIN);

  public static final PluginKey                       PRODUCT_ADDED_KEY                              =
                                                                        PluginKey.key(PERKSTORE_PRODUCT_ADDED_NOTIFICATION_PLUGIN);

  // Stored parameters

  private static final String                         STORED_PARAMETER_SETTINGS_SYMBOL               = "SYMBOL";

  private static final String                         STORED_PARAMETER_PRODUCT_ID                    = "PRODUCT_ID";

  private static final String                         STORED_PARAMETER_PRODUCT_TITLE                 = "PRODUCT_TITLE";

  private static final String                         STORED_PARAMETER_PRODUCT_SUPPLY                = "PRODUCT_SUPPLY";

  private static final String                         STORED_PARAMETER_PRODUCT_PRICE                 = "PRODUCT_PRICE";

  private static final String                         STORED_PARAMETER_PRODUCT_IS_NEW                = "PRODUCT_IS_NEW";

  private static final String                         STORED_PARAMETER_ORDER_ID                      = "ORDER_ID";

  private static final String                         STORED_PARAMETER_ORDER_STATUS                  = "ORDER_STATUS";

  private static final String                         STORED_PARAMETER_QUANTITY_ORDER                = "ORDER_QUANTITY";

  private static final String                         STORED_PARAMETER_ORDER_DELIVERED_QUANTITY      = "ORDER_DELIVERED_QUANTITY";

  private static final String                         STORED_PARAMETER_ORDER_REFUND_TX_STATUS        = "ORDER_REFUNDED_TX_STATUS";

  private static final String                         STORED_PARAMETER_ORDER_REFUNDED_QUANTITY       = "ORDER_REFUNDED_QUANTITY";

  private static final String                         STORED_PARAMETER_ORDER_REFUNDED_AMOUNT         = "ORDER_REFUNDED_AMOUNT";

  private static final String                         STORED_PARAMETER_ORDER_REMAINING_QUANTITY      = "ORDER_REMAINING_QUANTITY";

  private static final String                         STORED_PARAMETER_ORDER_IS_NEW                  = "ORDER_IS_NEW";

  private static final String                         STORED_PARAMETER_ORDER_MODIFICATION_TYPE       = "ORDER_MODIFICATION_TYPE";

  private static final String                         STORED_PARAMETER_MODIFIER_IDENTITY_ID          = "MODIFIER_ID";

  private static final String                         STORED_PARAMETER_SENDER_IDENTITY_ID            = "SENDER_ID";

  private static final String                         STORED_PARAMETER_RECEIVER_IDENTITY_ID          = "RECEIVER_ID";

  // Template variables

  private static final String                         TEMPLATE_VARIABLE_SETTINGS_SYMBOL              = "symbol";

  private static final String                         TEMPLATE_VARIABLE_PRODUCT_ID                   = "productId";

  private static final String                         TEMPLATE_VARIABLE_PRODUCT_TITLE                = "productTitle";

  private static final String                         TEMPLATE_VARIABLE_PRODUCT_SUPPLY               = "productSupply";

  private static final String                         TEMPLATE_VARIABLE_PRODUCT_PRICE                = "productPrice";

  private static final String                         TEMPLATE_VARIABLE_PRODUCT_IS_NEW               = "isNewProduct";

  private static final String                         TEMPLATE_VARIABLE_ORDER_ID                     = "orderId";

  private static final String                         TEMPLATE_VARIABLE_ORDER_STATUS                 = "orderStatus";

  private static final String                         TEMPLATE_VARIABLE_ORDER_LABEL_STATUS           = "orderStatusLabel";

  private static final String                         TEMPLATE_VARIABLE_ORDER_QUANTITY               = "orderQuantity";

  private static final String                         TEMPLATE_VARIABLE_ORDER_DELIVERED_QUANTITY     = "orderDeliveredQuantity";

  private static final String                         TEMPLATE_VARIABLE_ORDER_REFUNDED_QUANTITY      = "orderRefundedQuantity";

  private static final String                         TEMPLATE_VARIABLE_ORDER_REFUND_TX_STATUS       =
                                                                                               "orderRefundTransactionStatus";

  private static final String                         TEMPLATE_VARIABLE_ORDER_REFUNDED_AMOUNT        = "orderRefundedAmount";

  private static final String                         TEMPLATE_VARIABLE_ORDER_REMAINING_QUANTITY     = "orderRemainingQuantity";

  private static final String                         TEMPLATE_VARIABLE_ORDER_IS_NEW                 = "isNewOrder";

  private static final String                         TEMPLATE_VARIABLE_ORDER_MODIFICATION_TYPE      = "modificationType";

  private static final String                         TEMPLATE_VARIABLE_NOTIFICATION_URL             = "detailsURL";

  private static final String                         TEMPLATE_VARIABLE_PREFIX_ORDER_IDENTITY        = "isOrder";

  private static final String                         TEMPLATE_VARIABLE_SUFFIX_IDENTITY_ID           = "Id";

  private static final String                         TEMPLATE_VARIABLE_SUFFIX_IDENTITY_AVATAR       = "Avatar";

  private static final String                         TEMPLATE_VARIABLE_SUFFIX_IDENTITY_NAME         = "Name";

  private static final String                         TEMPLATE_VARIABLE_SUFFIX_IDENTITY_URL          = "Url";

  private static final String                         TEMPLATE_VARIABLE_SUFFIX_IS_SPACE_TYPE         = "IsSpaceType";

  private static String                               defaultSite;

  private NotificationUtils() {
  }

  public static final GlobalSettings getSettingsParameter(NotificationContext ctx) {
    return ctx.value(SETTINGS_PARAMETER);
  }

  public static final Product getProductParameter(NotificationContext ctx) {
    return ctx.value(PRODUCT_PARAMETER);
  }

  public static final ProductOrder getOrderParameter(NotificationContext ctx) {
    return ctx.value(ORDER_PARAMETER);
  }

  public static final boolean isNewProductParameter(NotificationContext ctx) {
    Boolean value = ctx.value(PRODUCT_IS_NEW_PARAMETER);
    return value != null && value;
  }

  public static final boolean isNewOrderParameter(NotificationContext ctx) {
    Boolean value = ctx.value(ORDER_IS_NEW_PARAMETER);
    return value != null && value;
  }

  public static final void setSettingsParameter(NotificationContext ctx, GlobalSettings settings) {
    ctx.append(SETTINGS_PARAMETER, settings);
  }

  public static final void setProductParameter(NotificationContext ctx, Product product) {
    ctx.append(PRODUCT_PARAMETER, product);
  }

  public static final void setOrderParameter(NotificationContext ctx, ProductOrder order) {
    ctx.append(ORDER_PARAMETER, order);
  }

  public static final void setIsNewProductParameter(NotificationContext ctx) {
    ctx.append(PRODUCT_IS_NEW_PARAMETER, true);
  }

  public static final void setIsNewOrderParameter(NotificationContext ctx) {
    ctx.append(ORDER_IS_NEW_PARAMETER, true);
  }

  public static final void setNotificationRecipients(NotificationInfo notification,
                                                     GlobalSettings globalSettings,
                                                     Product product,
                                                     ProductOrder order,
                                                     boolean newProduct,
                                                     boolean newOrder) {
    Set<String> ignoredUsers = new HashSet<>();
    Set<String> recipientList = new HashSet<>();

    if (order == null) {// New or modified product
      if (newProduct) {
        ignoredUsers.add(product.getCreator().getId()); // Avoid sending
                                                        // notification to
                                                        // product creator
      } else {
        ignoredUsers.add(product.getLastModifier().getId()); // Avoid sending
                                                             // notification to
                                                             // modifier
      }

      List<Profile> productAccessPermissions = product.getAccessPermissions();
      if (productAccessPermissions == null || productAccessPermissions.isEmpty()) {
        productAccessPermissions = null;
      }
      List<Profile> applicationAccessPermissions = globalSettings.getAccessPermissionsProfiles();
      if (applicationAccessPermissions == null || applicationAccessPermissions.isEmpty()) {
        applicationAccessPermissions = null;
      }

      // Send notification to all who can get access permission to product
      if (productAccessPermissions != null && applicationAccessPermissions != null) {
        // Retain in recipient list only users who are member of both ACL
        List<String> applicationRecipientList = new ArrayList<>();
        addIdentityMembersFromProfiles(applicationAccessPermissions, applicationRecipientList);
        addIdentityMembersFromProfiles(productAccessPermissions, recipientList);
        recipientList.retainAll(applicationRecipientList);
      } else if (productAccessPermissions != null) {
        addIdentityMembersFromProfiles(productAccessPermissions, recipientList);
      } else if (applicationAccessPermissions != null) {
        addIdentityMembersFromProfiles(applicationAccessPermissions, recipientList);
      } else {
        addIdentityMembersFromProfiles(product.getMarchands(), recipientList);
        addIdentityMembersFromProfiles(Collections.singleton(product.getReceiverMarchand()), recipientList);
      }
    } else if (newOrder) {// New order
      ignoredUsers.add(order.getSender().getId());

      // Retain in recipient list only users who are member of both ACL
      addIdentityMembersFromProfiles(product.getMarchands(), recipientList);
    } else {// Modified order
      if (order.getLastModifier() != null) {
        ignoredUsers.add(order.getLastModifier().getId());
      }

      // Always send to buyer
      recipientList.add(order.getSender().getId());

      addIdentityMembersFromProfiles(product.getMarchands(), recipientList);
    }

    recipientList.removeAll(ignoredUsers);
    notification.to(new ArrayList<>(recipientList));
  }

  public static final void storeOrderParameters(NotificationInfo notification, ProductOrder order, boolean isNew) {
    if (order.getReceiver() == null || order.getReceiver().getTechnicalId() == 0) {
      throw new IllegalStateException("receiver is null");
    }
    if (order.getSender() == null || order.getSender().getTechnicalId() == 0) {
      throw new IllegalStateException("sender is null");
    }

    // Last modifier could be null when the order is modified on transaction
    // finish
    if (order.getLastModifier() != null) {
      notification.with(STORED_PARAMETER_MODIFIER_IDENTITY_ID, String.valueOf(order.getLastModifier().getTechnicalId()));
    }

    notification.with(STORED_PARAMETER_ORDER_ID, String.valueOf(order.getId()))
                .with(STORED_PARAMETER_RECEIVER_IDENTITY_ID, String.valueOf(order.getReceiver().getTechnicalId()))
                .with(STORED_PARAMETER_SENDER_IDENTITY_ID, String.valueOf(order.getSender().getTechnicalId()))
                .with(STORED_PARAMETER_ORDER_STATUS, order.getStatus())
                .with(STORED_PARAMETER_QUANTITY_ORDER, stringifyDouble(order.getQuantity()))
                .with(STORED_PARAMETER_ORDER_DELIVERED_QUANTITY, stringifyDouble(order.getDeliveredQuantity()))
                .with(STORED_PARAMETER_ORDER_REFUND_TX_STATUS, order.getRefundTransactionStatus())
                .with(STORED_PARAMETER_ORDER_MODIFICATION_TYPE, order.getModificationType().name())
                .with(STORED_PARAMETER_ORDER_REFUNDED_AMOUNT, stringifyDouble(order.getRefundedAmount()))
                .with(STORED_PARAMETER_ORDER_REFUNDED_QUANTITY, stringifyDouble(order.getRefundedQuantity()))
                .with(STORED_PARAMETER_ORDER_REMAINING_QUANTITY, stringifyDouble(order.getRemainingQuantityToProcess()))
                .with(STORED_PARAMETER_ORDER_IS_NEW, String.valueOf(isNew));
  }

  public static final void storeSettingsParameters(GlobalSettings globalSettings, NotificationInfo notification) {
    String symbol = globalSettings.getSymbol();
    if (symbol == null) {
      symbol = "";
    }
    notification.with(STORED_PARAMETER_SETTINGS_SYMBOL, symbol);
  }

  public static final void storeProductParameters(NotificationInfo notification, Product product, boolean isNew) {
    if (product.getCreator() == null || product.getCreator().getTechnicalId() == 0) {
      throw new IllegalStateException("creator is null");
    }

    if (!isNew && (product.getLastModifier() == null || product.getLastModifier().getTechnicalId() == 0)) {
      throw new IllegalStateException("last modifier is null");
    }

    long modifierId = isNew ? product.getCreator().getTechnicalId() : product.getLastModifier().getTechnicalId();
    notification.with(STORED_PARAMETER_PRODUCT_ID, String.valueOf(product.getId()))
                .with(STORED_PARAMETER_SENDER_IDENTITY_ID, String.valueOf(modifierId))
                .with(STORED_PARAMETER_PRODUCT_TITLE, product.getTitle())
                .with(STORED_PARAMETER_PRODUCT_SUPPLY, stringifyDouble(product.getTotalSupply()))
                .with(STORED_PARAMETER_PRODUCT_PRICE, stringifyDouble(product.getPrice()))
                .with(STORED_PARAMETER_PRODUCT_IS_NEW, String.valueOf(isNew));
  }

  public static final String stringifyDouble(double value) {
    long longValue = (long) value;
    if (value == longValue) {
      return String.format("%d", longValue);
    } else {
      return String.format("%s", value);
    }
  }

  public static final String getNotificationURL(Product product, ProductOrder productOrder) {
    String currentSite = getDefaultSite();
    String currentDomain = CommonsUtils.getCurrentDomain();
    if (!currentDomain.endsWith("/")) {
      currentDomain += "/";
    }
    String notificationURL = currentDomain + "portal/" + currentSite + "/perkstore?productId=" + product.getId();
    if (productOrder != null) {
      notificationURL += "&orderId=" + productOrder.getId();
    }
    return notificationURL;
  }

  public static String getDefaultSite() {
    if (defaultSite != null) {
      return defaultSite;
    }
    UserPortalConfigService portalConfig = CommonsUtils.getService(UserPortalConfigService.class);
    defaultSite = portalConfig.getDefaultPortal();
    return defaultSite;
  }

  public static final TemplateContext buildTemplateParameters(TemplateProvider templateProvider,
                                                              NotificationInfo notification,
                                                              String notificationURL) {
    String language = NotificationPluginUtils.getLanguage(notification.getTo());
    TemplateContext templateContext = getTemplateContext(templateProvider, notification, language);

    setFooter(notification, templateContext);
    setRead(notification, templateContext);
    setNotificationId(notification, templateContext);
    setLasModifiedTime(notification, templateContext, language);

    setIdentityNameAndAvatar(notification, templateContext, "sender");
    setIdentityNameAndAvatar(notification, templateContext, "receiver");
    setIdentityNameAndAvatar(notification, templateContext, "modifier");

    setSettingsDetails(templateContext, notification);
    setProductDetails(templateContext, notification);
    setOrderDetails(templateContext, notification);

    templateContext.put(TEMPLATE_VARIABLE_NOTIFICATION_URL, notificationURL);
    return templateContext;
  }

  public static final MessageInfo buildMessageSubjectAndBody(TemplateContext templateContext,
                                                             NotificationInfo notification,
                                                             String pushNotificationURL) {
    MessageInfo messageInfo = new MessageInfo();
    setMessageSubject(messageInfo, templateContext, getProductTitle(notification), pushNotificationURL);
    setMessageBody(templateContext, messageInfo);
    return messageInfo.end();
  }

  private static final void setMessageSubject(MessageInfo messageInfo,
                                              TemplateContext templateContext,
                                              String title,
                                              String pushNotificationURL) {
    if (pushNotificationURL != null) {
      messageInfo.subject(pushNotificationURL);
    } else {
      messageInfo.subject(TemplateUtils.processSubject(templateContext) + ": " + title);
    }
  }

  private static String getProductTitle(NotificationInfo notification) {
    return notification.getValueOwnerParameter(STORED_PARAMETER_PRODUCT_TITLE);
  }

  private static final TemplateContext getTemplateContext(TemplateProvider templateProvider,
                                                          NotificationInfo notification,
                                                          String language) {
    PluginKey pluginKey = notification.getKey();
    String pluginId = pluginKey.getId();
    ChannelKey channelKey = templateProvider.getChannelKey();
    return TemplateContext.newChannelInstance(channelKey, pluginId, language);
  }

  private static final void setMessageBody(TemplateContext templateContext, MessageInfo messageInfo) {
    messageInfo.body(TemplateUtils.processGroovy(templateContext));
  }

  private static final void setOrderDetails(TemplateContext templateContext, NotificationInfo notification) {
    String orderId = notification.getValueOwnerParameter(STORED_PARAMETER_ORDER_ID);
    if (StringUtils.isNotBlank(orderId)) {
      String isNewString = notification.getValueOwnerParameter(STORED_PARAMETER_ORDER_IS_NEW);
      boolean isNew = StringUtils.isNotBlank(isNewString) && Boolean.parseBoolean(isNewString);

      String modificationType = notification.getValueOwnerParameter(STORED_PARAMETER_ORDER_MODIFICATION_TYPE);
      String orderStatus = notification.getValueOwnerParameter(STORED_PARAMETER_ORDER_STATUS);
      String deliveredQuantity = notification.getValueOwnerParameter(STORED_PARAMETER_ORDER_DELIVERED_QUANTITY);
      String refundedQuantity = notification.getValueOwnerParameter(STORED_PARAMETER_ORDER_REFUNDED_QUANTITY);
      String remainingQuantity = notification.getValueOwnerParameter(STORED_PARAMETER_ORDER_REMAINING_QUANTITY);
      String orderRefundedAmount = notification.getValueOwnerParameter(STORED_PARAMETER_ORDER_REFUNDED_AMOUNT);
      String orderRefundTxStatus = notification.getValueOwnerParameter(STORED_PARAMETER_ORDER_REFUND_TX_STATUS);

      if (StringUtils.isBlank(orderRefundedAmount)) {
        orderRefundedAmount = "0";
      }
      if (StringUtils.isBlank(orderRefundTxStatus)) {
        orderRefundTxStatus = "NONE";
      }

      if (ProductOrderModificationType.valueOf(modificationType) == ProductOrderModificationType.REFUND_TX_STATUS) {
        String orderStatusLabel = ProductOrderStatus.REFUNDED.name().toLowerCase();
        if (StringUtils.equals(orderRefundTxStatus, "failed")) {
          templateContext.put(TEMPLATE_VARIABLE_ORDER_LABEL_STATUS, "error." + orderStatusLabel);
        } else {
          templateContext.put(TEMPLATE_VARIABLE_ORDER_LABEL_STATUS, orderStatusLabel);
        }
      } else if (ProductOrderModificationType.valueOf(modificationType) == ProductOrderModificationType.REFUNDED_QUANTITY) {
        templateContext.put(TEMPLATE_VARIABLE_ORDER_LABEL_STATUS, ProductOrderStatus.REFUNDED.name().toLowerCase());
      } else if (ProductOrderModificationType.valueOf(modificationType) == ProductOrderModificationType.DELIVERED_QUANTITY) {
        templateContext.put(TEMPLATE_VARIABLE_ORDER_LABEL_STATUS, ProductOrderStatus.DELIVERED.name().toLowerCase());
      } else {
        templateContext.put(TEMPLATE_VARIABLE_ORDER_LABEL_STATUS, orderStatus.toLowerCase());
      }

      templateContext.put(TEMPLATE_VARIABLE_ORDER_ID, orderId);
      templateContext.put(TEMPLATE_VARIABLE_ORDER_STATUS, orderStatus);
      templateContext.put(TEMPLATE_VARIABLE_ORDER_QUANTITY, notification.getValueOwnerParameter(STORED_PARAMETER_QUANTITY_ORDER));
      templateContext.put(TEMPLATE_VARIABLE_ORDER_DELIVERED_QUANTITY, deliveredQuantity);
      templateContext.put(TEMPLATE_VARIABLE_ORDER_REFUNDED_QUANTITY, refundedQuantity);
      templateContext.put(TEMPLATE_VARIABLE_ORDER_REFUNDED_AMOUNT, orderRefundedAmount);
      templateContext.put(TEMPLATE_VARIABLE_ORDER_REFUND_TX_STATUS, orderRefundTxStatus);
      templateContext.put(TEMPLATE_VARIABLE_ORDER_REMAINING_QUANTITY, remainingQuantity);
      templateContext.put(TEMPLATE_VARIABLE_ORDER_MODIFICATION_TYPE, modificationType);
      templateContext.put(TEMPLATE_VARIABLE_ORDER_IS_NEW, String.valueOf(isNew));
    }
  }

  private static final void setSettingsDetails(TemplateContext templateContext, NotificationInfo notification) {
    String symbol = notification.getValueOwnerParameter(STORED_PARAMETER_SETTINGS_SYMBOL);
    if (symbol == null) {
      symbol = "";
    }
    templateContext.put(TEMPLATE_VARIABLE_SETTINGS_SYMBOL, symbol);
  }

  private static final void setProductDetails(TemplateContext templateContext, NotificationInfo notification) {
    String isNewString = notification.getValueOwnerParameter(STORED_PARAMETER_PRODUCT_IS_NEW);
    boolean isNew = StringUtils.isNotBlank(isNewString) && Boolean.parseBoolean(isNewString);

    templateContext.put(TEMPLATE_VARIABLE_PRODUCT_ID, notification.getValueOwnerParameter(STORED_PARAMETER_PRODUCT_ID));
    templateContext.put(TEMPLATE_VARIABLE_PRODUCT_TITLE, getProductTitle(notification));
    templateContext.put(TEMPLATE_VARIABLE_PRODUCT_SUPPLY, notification.getValueOwnerParameter(STORED_PARAMETER_PRODUCT_SUPPLY));
    templateContext.put(TEMPLATE_VARIABLE_PRODUCT_PRICE, notification.getValueOwnerParameter(STORED_PARAMETER_PRODUCT_PRICE));
    templateContext.put(TEMPLATE_VARIABLE_PRODUCT_IS_NEW, String.valueOf(isNew));
  }

  private static final void setIdentityNameAndAvatar(NotificationInfo notification,
                                                     TemplateContext templateContext,
                                                     String prefix) {
    String identityId = notification.getValueOwnerParameter(prefix.toUpperCase() + "_ID");
    if (StringUtils.isBlank(identityId)) {
      templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IDENTITY_ID, "");
      templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IDENTITY_ID, "");
      templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IDENTITY_NAME, "");
      templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IDENTITY_AVATAR, "");
      templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IS_SPACE_TYPE, "");
      templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IDENTITY_URL, "");
      templateContext.put(TEMPLATE_VARIABLE_PREFIX_ORDER_IDENTITY + StringUtils.capitalize(prefix), "false");
      return;
    }
    Identity identity = getIdentityById(identityId);
    if (identity == null) {
      throw new IllegalStateException("Identity with id " + identityId + " not found, can't send notification");
    }

    boolean isSpaceType = isSpaceType(identity.getProviderId());

    templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IS_SPACE_TYPE, String.valueOf(isSpaceType));

    String remoteId = identity.getRemoteId();
    templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IDENTITY_ID, remoteId);
    templateContext.put(TEMPLATE_VARIABLE_PREFIX_ORDER_IDENTITY + StringUtils.capitalize(prefix),
                        String.valueOf(StringUtils.equals(notification.getTo(), remoteId)));

    String fullName = identity.getProfile().getFullName();
    if (StringUtils.isBlank(fullName) && isSpaceType) {
      Space space = getSpace(remoteId);
      if (space != null) {
        fullName = space.getDisplayName();
      }
    }
    templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IDENTITY_NAME, fullName);

    String avatarURL = identity.getProfile().getAvatarUrl();
    if (avatarURL == null) {
      if (isSpaceType) {
        avatarURL = LinkProvider.SPACE_DEFAULT_AVATAR_URL;
      } else {
        avatarURL = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
      }
    }
    templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IDENTITY_AVATAR, avatarURL);

    templateContext.put(prefix + TEMPLATE_VARIABLE_SUFFIX_IDENTITY_URL, getAbsoluteURL(remoteId, isSpaceType));
  }

  public static String getAbsoluteURL(String id, boolean isSpaceType) {
    if (isSpaceType) {
      return getSpaceAbsoluteURL(id);
    } else {
      return getUserAbsoluteURI(id);
    }
  }

  private static String getUserAbsoluteURI(String id) {
    String currentSite = getDefaultSite();
    String currentDomain = CommonsUtils.getCurrentDomain();
    if (!currentDomain.endsWith("/")) {
      currentDomain += "/";
    }
    return currentDomain + "portal/" + currentSite + "/profile/" + id;
  }

  public static String getSpaceAbsoluteURL(String id) {
    Space space = getSpace(id);
    if (space != null) {
      String spaceUrlId = space.getGroupId().replace(SPACE_GROUP_PREFIX, "");
      String currentDomain = CommonsUtils.getCurrentDomain();
      if (!currentDomain.endsWith("/")) {
        currentDomain += "/";
      }
      return currentDomain + "portal/g/:spaces:" + spaceUrlId;
    } else {
      return "#";
    }
  }

  private static final void setFooter(NotificationInfo notification, TemplateContext templateContext) {
    SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
  }

  private static final void setRead(NotificationInfo notification, TemplateContext templateContext) {
    templateContext.put("READ",
                        Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read"
                                                                                                                              : "unread");
  }

  private static final void setNotificationId(NotificationInfo notification, TemplateContext templateContext) {
    templateContext.put("NOTIFICATION_ID", notification.getId());
  }

  private static final void setLasModifiedTime(NotificationInfo notification, TemplateContext templateContext, String language) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(notification.getLastModifiedDate());
    templateContext.put("LAST_UPDATED_TIME",
                        TimeConvertUtils.convertXTimeAgoByTimeServer(cal.getTime(),
                                                                     "EE, dd yyyy",
                                                                     new Locale(language),
                                                                     TimeConvertUtils.YEAR));
  }

}
