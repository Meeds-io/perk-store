package org.exoplatform.addon.perkstore.service.utils;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.addon.perkstore.entity.ProductEntity;
import org.exoplatform.addon.perkstore.entity.ProductOrderEntity;
import org.exoplatform.addon.perkstore.exception.PerkStoreException;
import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.Constants;
import org.exoplatform.portal.localization.LocaleContextInfoUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.resources.*;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.ws.frameworks.json.JsonGenerator;
import org.exoplatform.ws.frameworks.json.JsonParser;
import org.exoplatform.ws.frameworks.json.impl.*;

public class Utils {
  private static final Log          LOG                                       = ExoLogger.getLogger(Utils.class);

  public static final JsonParser    JSON_PARSER                               = new JsonParserImpl();

  public static final JsonGenerator JSON_GENERATOR                            = new JsonGeneratorImpl();

  public static final String        SCOPE_NAME                                = "ADDONS_PERKSTORE";

  public static final String        SETTINGS_KEY_NAME                         = "ADDONS_PERKSTORE_SETTINGS";

  public static final Context       PERKSTORE_CONTEXT                         = Context.GLOBAL;

  public static final String        ADMINISTRATORS_GROUP                      = "/platform/administrators";

  public static final Scope         PERKSTORE_SCOPE                           = Scope.APPLICATION.id(SCOPE_NAME);

  public static final String        ERROR_SUFFIX_GLOBAL_SETTINGS_ACCESS       = "settings.access";

  public static final String        ERROR_SUFFIX_GLOBAL_SETTINGS_MODIFICATION = "settings.modification";

  public static final String        ERROR_SUFFIX_ORDER_CREATION               = "order.creation";

  public static final String        ERROR_SUFFIX_ORDER_MODIFICATION           = "order.modification";

  public static final String        ERROR_SUFFIX_ORDER_NOT_EXISTS             = "order.existance";

  public static final String        ERROR_SUFFIX_PRODUCT_CREATION             = "product.creation";

  public static final String        ERROR_SUFFIX_PRODUCT_MODIFICATION         = "product.modification";

  public static final String        ERROR_SUFFIX_PRODUCT_NOT_EXISTS           = "product.existance";

  public static final String        SPACE_ACCOUNT_TYPE                        = "space";

  public static final String        USER_ACCOUNT_TYPE                         = "user";

  public static final String        FAKE_TRANSACTION_HASH                     =
                                                          "0x0000000000000000000000000000000000000000000000000000000000000000";

  private Utils() {
  }

  public static <T> T fromString(Class<T> type, String value) throws JsonException {
    if (StringUtils.isBlank(value)) {
      return null;
    }
    JsonDefaultHandler jsonDefaultHandler = new JsonDefaultHandler();
    JSON_PARSER.parse(new ByteArrayInputStream(value.getBytes()), jsonDefaultHandler);
    return ObjectBuilder.createObject(type, jsonDefaultHandler.getJsonObject());
  }

  public static String transformToString(Object object) {
    try {
      return JSON_GENERATOR.createJsonObject(object).toString();
    } catch (Exception e) {
      throw new IllegalStateException("Can't transform object to string", e);
    }
  }

  public static long timeToMilliseconds(LocalDateTime time) {
    return time.atZone(ZoneOffset.systemDefault()).toEpochSecond() * 1000;
  }

  public static String getIdentityIdByType(Identity receiverIdentity) {
    if (SpaceIdentityProvider.NAME.equals(receiverIdentity.getProviderId())) {
      Space space = getSpace(receiverIdentity.getRemoteId());
      if (space != null) {
        return space.getId();
      }
    }
    return receiverIdentity.getId();
  }

  public static Identity getIdentityById(long identityId) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    return identityManager.getIdentity(String.valueOf(identityId), true);
  }

  public static Identity getIdentityByTypeAndId(String type, String remoteId) {
    String providerId = SPACE_ACCOUNT_TYPE.equals(type) ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    return identityManager.getOrCreateIdentity(providerId, remoteId, true);
  }

  public static String getIdentityTypeByProviderId(String providerId) {
    if (providerId == null) {
      throw new IllegalArgumentException("Provider id is null");
    }
    return SpaceIdentityProvider.NAME.equals(providerId) ? SPACE_ACCOUNT_TYPE : USER_ACCOUNT_TYPE;
  }

  public static Space getSpace(String id) {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    if (id.indexOf(SpaceUtils.SPACE_GROUP) >= 0) {
      return spaceService.getSpaceByGroupId(id);
    }
    Space space = spaceService.getSpaceById(id);
    if (space == null) {
      space = spaceService.getSpaceByPrettyName(id);
      if (space == null) {
        space = spaceService.getSpaceByGroupId("/spaces/" + id);
        if (space == null) {
          space = spaceService.getSpaceByDisplayName(id);
          if (space == null) {
            space = spaceService.getSpaceByUrl(id);
          }
        }
      }
    }
    return space;
  }

  public static ProductOrder fromEntity(ProductOrderEntity entity) {
    if (entity == null) {
      throw new IllegalArgumentException("Order entity is null");
    }
    ProductOrder order = new ProductOrder();

    order.setId(entity.getId());
    order.setProductId(entity.getProduct().getId());
    order.setTransactionHash(entity.getTransactionHash());
    order.setQuantity(entity.getQuantity());
    order.setAmount(entity.getAmount());
    order.setSender(toProfile(entity.getSenderId()));
    order.setReceiver(toProfile(entity.getReceiverId()));
    order.setDeliveredQuantity(entity.getDeliveredQuantity());
    order.setRefundedQuantity(entity.getRefundedQuantity());
    order.setCreatedDate(entity.getCreatedDate());
    order.setDeliveredDate(entity.getDeliveredDate());
    order.setRefundedDate(entity.getRefundedDate());
    order.setError(entity.getError());
    order.setStatus(entity.getStatus().name());
    order.setRemainingQuantityToProcess(entity.getRemainingQuantity());
    return order;
  }

  public static ProductOrderEntity toEntity(ProductEntity productEntity, ProductOrder order) {
    if (order == null) {
      throw new IllegalArgumentException("Product order is null");
    }
    ProductOrderEntity entity = new ProductOrderEntity();

    entity.setId(order.getId());
    entity.setProduct(productEntity);
    entity.setTransactionHash(order.getTransactionHash());
    entity.setQuantity(order.getQuantity());
    entity.setAmount(order.getAmount());
    entity.setSenderId(getTechnicalId(order.getSender()));
    entity.setReceiverId(getTechnicalId(order.getReceiver()));
    entity.setDeliveredQuantity(order.getDeliveredQuantity());
    entity.setRefundedQuantity(order.getRefundedQuantity());
    entity.setCreatedDate(order.getCreatedDate());
    entity.setDeliveredDate(order.getDeliveredDate());
    entity.setRefundedDate(order.getRefundedDate());
    entity.setError(order.getError());
    entity.setStatus(ProductOrderStatus.valueOf(order.getStatus()));
    entity.setRemainingQuantity(order.getQuantity() - order.getDeliveredQuantity() - order.getRefundedQuantity());
    return entity;
  }

  public static Product fromEntity(ProductEntity entity) {
    if (entity == null) {
      throw new IllegalArgumentException("Product entity is null");
    }
    Product product = new Product();

    product.setId(entity.getId());
    product.setTitle(entity.getTitle());
    product.setDescription(entity.getDescription());
    product.setPrice(entity.getPrice());
    product.setIllustrationURL(entity.getIllustrationURL());
    product.setEnabled(entity.isEnabled());
    product.setUnlimited(entity.isUnlimited());
    product.setTotalSupply(entity.getTotalSupply());
    product.setMaxOrdersPerUser(entity.getMaxOrdersPerUser());
    product.setCreator(toProfile(entity.getCreator()));
    product.setLastModifier(toProfile(entity.getLastModifier()));
    product.setCreatedDate(entity.getCreatedDate());
    product.setLastModifiedDate(entity.getLastModifiedDate());
    product.setReceiverMarchand(toProfile(entity.getReceiverId()));
    if (entity.getMarchands() == null) {
      product.setMarchands(Collections.emptyList());
    } else {
      product.setMarchands(entity.getMarchands().stream().map(Utils::toProfile).collect(Collectors.toList()));
    }
    if (entity.getAccessPermissions() == null) {
      product.setAccessPermissions(Collections.emptyList());
    } else {
      product.setAccessPermissions(entity.getAccessPermissions().stream().map(Utils::toProfile).collect(Collectors.toList()));
    }

    ProductOrderPeriodType orderPeriodicity = entity.getOrderPeriodicity();
    if (orderPeriodicity != null) {
      product.setOrderPeriodicity(orderPeriodicity.getName());
      product.setOrderPeriodicityLabel(orderPeriodicity.getLabel());
    }
    return product;
  }

  public static ProductEntity toEntity(Product product) {
    if (product == null) {
      throw new IllegalArgumentException("Entity is null");
    }
    ProductEntity entity = new ProductEntity();

    entity.setId(product.getId());
    entity.setTitle(product.getTitle());
    entity.setPrice(product.getPrice());
    entity.setDescription(product.getDescription());
    entity.setIllustrationURL(product.getIllustrationURL());
    entity.setEnabled(product.isEnabled());
    entity.setUnlimited(product.isUnlimited());
    entity.setTotalSupply(product.getTotalSupply());
    entity.setMaxOrdersPerUser(product.getMaxOrdersPerUser());
    entity.setCreatedDate(product.getCreatedDate());
    entity.setCreator(product.getCreator() == null ? 0 : product.getCreator().getTechnicalId());
    entity.setLastModifier(product.getLastModifier() == null ? 0 : product.getLastModifier().getTechnicalId());
    entity.setLastModifiedDate(product.getLastModifiedDate());

    entity.setReceiverId(getTechnicalId(product.getReceiverMarchand()));

    if (product.getMarchands() == null || product.getMarchands().isEmpty()) {
      entity.setMarchands(Collections.emptyList());
    } else {
      entity.setMarchands(product.getMarchands().stream().map(Utils::getTechnicalId).collect(Collectors.toList()));
    }
    if (product.getAccessPermissions() == null || product.getAccessPermissions().isEmpty()) {
      entity.setAccessPermissions(Collections.emptyList());
    } else {
      entity.setAccessPermissions(product.getAccessPermissions()
                                         .stream()
                                         .map(Utils::getTechnicalId)
                                         .collect(Collectors.toList()));
    }

    entity.setOrderPeriodicity(getOrderPeriodicity(product.getOrderPeriodicity()));

    return entity;
  }

  public static ProductOrderPeriodType getOrderPeriodicity(String orderPeriodicity) {
    ProductOrderPeriodType productOrderPeriodType = null;
    if (StringUtils.isNoneBlank(orderPeriodicity)) {
      productOrderPeriodType = ProductOrderPeriodType.valueOf(orderPeriodicity.toUpperCase());
    }
    return productOrderPeriodType;
  }

  public static long getTechnicalId(Profile profile) {
    if (profile == null) {
      return 0;
    }
    if (profile.getTechnicalId() == 0) {
      Identity identity = getIdentityByTypeAndId(profile.getType(), profile.getId());
      if (identity != null) {
        return Long.parseLong(identity.getId());
      }
    } else {
      return profile.getTechnicalId();
    }
    return 0;
  }

  public static Profile toProfile(String type, String id) {
    Identity identity = getIdentityByTypeAndId(type, id);
    return toProfile(identity);
  }

  public static Profile toProfile(long identityId) {
    if (identityId > 0) {
      Identity identity = getIdentityById(identityId);
      return toProfile(identity);
    }
    return null;
  }

  private static Profile toProfile(Identity identity) {
    if (identity != null) {
      Profile profile = new Profile();
      profile.setDisplayName(identity.getProfile().getFullName());
      profile.setTechnicalId(Long.parseLong(identity.getId()));
      profile.setId(identity.getRemoteId());
      profile.setType(getIdentityTypeByProviderId(identity.getProviderId()));
      return profile;
    }
    return null;
  }

  public static final String getCurrentUserId() {
    if (ConversationState.getCurrent() != null && ConversationState.getCurrent().getIdentity() != null) {
      return ConversationState.getCurrent().getIdentity().getUserId();
    }
    return null;
  }

  public static final Locale getCurrentUserLocale() throws Exception {
    String username = getCurrentUserId();

    LocalePolicy localePolicy = CommonsUtils.getService(LocalePolicy.class);
    LocaleContextInfo localeCtx = LocaleContextInfoUtils.buildLocaleContextInfo((HttpServletRequest) null);
    localeCtx.setUserProfileLocale(getUserLocale(username));
    localeCtx.setRemoteUser(username);
    Set<Locale> supportedLocales = LocaleContextInfoUtils.getSupportedLocales();

    Locale locale = localePolicy.determineLocale(localeCtx);
    boolean supported = supportedLocales.contains(locale);

    if (!supported && !"".equals(locale.getCountry())) {
      locale = new Locale(locale.getLanguage());
      supported = supportedLocales.contains(locale);
    }
    if (!supported) {
      LOG.warn("Unsupported locale returned by LocalePolicy: " + localePolicy + ". Falling back to 'en'.");
      locale = Locale.ENGLISH;
    }
    return locale;
  }

  public static final String getI18NMessage(String messageKey) throws Exception {
    ResourceBundleService resourceBundleService = CommonsUtils.getService(ResourceBundleService.class);
    Locale userLocale = getCurrentUserLocale();
    if (userLocale == null) {
      userLocale = Locale.ENGLISH;
    }
    ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("locale.addon.PerkStoreError", userLocale);
    if (resourceBundle == null) {
      throw new IllegalStateException("Resource bundle not found");
    }
    String message = resourceBundle.getString(messageKey);
    if (StringUtils.isBlank(message)) {
      throw new IllegalStateException("Resource bundle key " + messageKey + "not found");
    }
    return message;
  }

  public static final Locale getUserLocale(String username) throws Exception {
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    UserProfile profile = organizationService.getUserProfileHandler().findUserProfileByName(username);
    String lang = null;
    if (profile != null) {
      lang = profile.getAttribute(Constants.USER_LANGUAGE);
    }
    if (StringUtils.isNotBlank(lang)) {
      return LocaleUtils.toLocale(lang);
    }
    return null;
  }

  public static final Response computeErrorResponse(Log log, PerkStoreException e, String message) {
    displayErrorLog(log, message, e);

    try {
      String errorJSONFormat = getErrorJSONFormat(e);
      return Response.status(500)
                     .type(MediaType.APPLICATION_JSON)
                     .entity(errorJSONFormat)
                     .build();
    } catch (Exception exception) {
      LOG.error("Error computing error message", exception);
      return Response.status(500)
                     .build();
    }
  }

  public static final void displayErrorLog(Log log, String message, PerkStoreException e) {
    if (log.isDebugEnabled()) {
      log.warn(message, e);
    } else {
      log.warn(message + ": {}", e.getMessage());
    }
  }

  public static final String getErrorJSONFormat(PerkStoreException e) throws JSONException {
    JSONObject errorObject = new JSONObject();
    errorObject.put("code", e.getErrorType().getCode());
    errorObject.put("suffix", e.getErrorType().getSuffix());
    errorObject.put("message", e.getLocalizedMessage());
    return errorObject.toString();
  }

  public static final boolean isUserAdmin(String username) throws Exception {
    return isUserMemberOf(username, ADMINISTRATORS_GROUP);
  }

  public static final boolean hasPermission(String username, List<String> productCreationPermissions) throws Exception {
    boolean isProductCreationAllowed = productCreationPermissions == null;
    if (productCreationPermissions != null) {
      for (String creationPermission : productCreationPermissions) {
        if (isUserMemberOf(username, creationPermission)) {
          isProductCreationAllowed = true;
        }
      }
    }
    return isProductCreationAllowed;
  }

  public static final boolean isUserMemberOf(String username, List<Profile> permittedProfiles) {
    if (permittedProfiles == null || permittedProfiles.isEmpty()) {
      throw new IllegalArgumentException("permissions field is mandatory");
    }

    for (Profile profile : permittedProfiles) {
      if (profile == null) {
        continue;
      }

      if (USER_ACCOUNT_TYPE.equals(profile.getType())) {
        if (profile.getId().equals(username)) {
          return true;
        }
      } else {
        Space space = getSpace(profile.getId());
        if (space == null) {
          LOG.warn("Can't check identity permission on space '{}' because the space wasn't found", profile.getId());
        } else if (CommonsUtils.getService(SpaceService.class).isMember(space, username)) {
          return true;
        }
      }
    }
    return false;
  }

  public static final boolean isUserMemberOf(String username, String permissionExpression) throws Exception {
    if (StringUtils.isBlank(permissionExpression)) {
      throw new IllegalArgumentException("Permission expression is mandatory");
    }
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username is mandatory");
    }

    if (permissionExpression.contains("/")) {
      return StringUtils.equals(username, permissionExpression);
    } else if (permissionExpression.contains(":")) {
      throw new UnsupportedOperationException("Permission check with role/membershipType isn't implemented ");
    } else {
      org.exoplatform.services.security.Identity identity = CommonsUtils.getService(IdentityRegistry.class).getIdentity(username);
      if (identity != null) {
        return identity.isMemberOf(permissionExpression);
      }

      Collection<Group> groupsOfUser;
      try {
        groupsOfUser = CommonsUtils.getService(OrganizationService.class)
                                   .getGroupHandler()
                                   .findGroupsOfUser(username);
      } catch (Exception e) {
        LOG.error("Error getting groups of user " + username);
        throw e;
      }
      if (groupsOfUser == null || groupsOfUser.isEmpty()) {
        return false;
      }
      for (Group group : groupsOfUser) {
        if (permissionExpression.equals(group.getId())) {
          return true;
        }
      }
      return false;
    }
  }

}
