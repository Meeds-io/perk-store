package org.exoplatform.addon.perkstore.service.utils;

import java.io.ByteArrayInputStream;
import java.time.*;
import java.util.Collections;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.addon.perkstore.entity.ProductEntity;
import org.exoplatform.addon.perkstore.entity.ProductOrderEntity;
import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
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
  private static final Log          LOG                = ExoLogger.getLogger(Utils.class);

  public static final JsonParser    JSON_PARSER        = new JsonParserImpl();

  public static final JsonGenerator JSON_GENERATOR     = new JsonGeneratorImpl();

  public static final String        SCOPE_NAME         = "ADDONS_PERKSTORE";

  public static final String        SETTINGS_KEY_NAME  = "ADDONS_PERKSTORE_SETTINGS";

  public static final Context       PERKSTORE_CONTEXT  = Context.GLOBAL;

  public static final Scope         PERKSTORE_SCOPE    = Scope.APPLICATION.id(SCOPE_NAME);

  public static final String        SPACE_ACCOUNT_TYPE = "space";

  public static final String        USER_ACCOUNT_TYPE  = "user";

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

  public static ProductOrderEntity toEntity(ProductOrder order) {
    if (order == null) {
      throw new IllegalArgumentException("Product order is null");
    }
    ProductOrderEntity entity = new ProductOrderEntity();

    entity.setId(order.getId());
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
    product.setIllustrationURL(entity.getIllustrationURL());
    product.setEnabled(entity.isEnabled());
    product.setUnlimited(entity.isUnlimited());
    product.setTotalSupply(entity.getTotalSupply());
    product.setMaxOrdersPerUser(entity.getMaxOrdersPerUser());
    product.setCreator(entity.getCreator());
    product.setLastModifier(entity.getLastModifier());
    product.setCreatedDate(entity.getCreatedDate());
    product.setLastModifiedDate(entity.getLastModifiedDate());
    product.setReceiverMarchand(toProfile(entity.getReceiverId()));
    if (entity.getMarchands() == null) {
      product.setMarchands(Collections.emptyList());
    } else {
      product.setMarchands(entity.getMarchands().stream().map(Utils::toProfile).collect(Collectors.toList()));
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
    entity.setDescription(product.getDescription());
    entity.setIllustrationURL(product.getIllustrationURL());
    entity.setEnabled(product.isEnabled());
    entity.setUnlimited(product.isUnlimited());
    entity.setTotalSupply(product.getTotalSupply());
    entity.setMaxOrdersPerUser(product.getMaxOrdersPerUser());
    entity.setCreatedDate(product.getCreatedDate());
    entity.setCreator(product.getCreator());
    entity.setLastModifier(product.getLastModifier());
    entity.setLastModifiedDate(product.getLastModifiedDate());

    entity.setReceiverId(getTechnicalId(product.getReceiverMarchand()));

    if (product.getMarchands() == null || product.getMarchands().isEmpty()) {
      entity.setMarchands(Collections.emptyList());
    } else {
      entity.setMarchands(product.getMarchands().stream().map(Utils::getTechnicalId).collect(Collectors.toList()));
    }

    String orderPeriodicity = product.getOrderPeriodicity();
    if (orderPeriodicity != null) {
      ProductOrderPeriodType productOrderPeriodType = ProductOrderPeriodType.valueOf(orderPeriodicity.toUpperCase());
      entity.setOrderPeriodicity(productOrderPeriodType);
    }

    return entity;
  }

  private static long getTechnicalId(Profile profile) {
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

  private static Profile toProfile(long identityId) {
    Profile profile = null;
    if (identityId > 0) {
      Identity identity = getIdentityById(identityId);
      if (identity != null) {
        profile = new Profile();
        profile.setDisplayName(identity.getProfile().getFullName());
        profile.setTechnicalId(identityId);
        profile.setId(identity.getRemoteId());
        profile.setType(getIdentityTypeByProviderId(identity.getProviderId()));
      }
    }
    return profile;
  }

  public static final String getCurrentUserId() {
    if (ConversationState.getCurrent() != null && ConversationState.getCurrent().getIdentity() != null) {
      return ConversationState.getCurrent().getIdentity().getUserId();
    }
    return null;
  }
}
