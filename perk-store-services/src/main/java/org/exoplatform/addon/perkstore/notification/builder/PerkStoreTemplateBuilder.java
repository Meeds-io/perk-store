package org.exoplatform.addon.perkstore.notification.builder;

import static org.exoplatform.addon.perkstore.service.utils.NotificationUtils.*;

import java.io.Writer;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.addon.perkstore.model.Product;
import org.exoplatform.addon.perkstore.model.ProductOrder;
import org.exoplatform.addon.perkstore.service.PerkStoreService;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.*;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;

public class PerkStoreTemplateBuilder extends AbstractTemplateBuilder {
  private static final Log LOG = ExoLogger.getLogger(PerkStoreTemplateBuilder.class);

  private PerkStoreService perkStoreService;

  private TemplateProvider templateProvider;

  private ExoContainer     container;

  private boolean          isPushNotification;

  private boolean          isOrderNotification;

  private PluginKey        key;

  private boolean          development;

  public PerkStoreTemplateBuilder(TemplateProvider templateProvider,
                                  ExoContainer container,
                                  PluginKey key,
                                  boolean pushNotification) {
    this(templateProvider, container, key, false, pushNotification);
  }

  public PerkStoreTemplateBuilder(TemplateProvider templateProvider,
                                  ExoContainer container,
                                  PluginKey key,
                                  boolean orderNotification,
                                  boolean pushNotification) {
    this.templateProvider = templateProvider;
    this.container = container;
    this.isPushNotification = pushNotification;
    this.isOrderNotification = orderNotification;
    this.key = key;
    String developmentString = System.getProperty("exo.addons.perkstore.development");
    this.development = !StringUtils.isBlank(developmentString) && Boolean.parseBoolean(developmentString);
  }

  @Override
  protected MessageInfo makeMessage(NotificationContext ctx) {
    NotificationInfo notification = ctx.getNotificationInfo();

    RequestLifeCycle.begin(container);
    try {
      Product product = getProduct(notification);
      ProductOrder order = getProductOrder(notification, isOrderNotification);

      String notificationURL = getNotificationURL(product, order);
      String pushNotificationURL = isPushNotification ? notificationURL : null;

      TemplateContext templateContext = buildTemplateParameters(templateProvider, notification, notificationURL);
      MessageInfo messageInfo = buildMessageSubjectAndBody(templateContext, notification, pushNotificationURL);
      Throwable exception = templateContext.getException();
      logException(notification, exception);
      ctx.setException(exception);
      return messageInfo;
    } catch (Throwable e) {
      ctx.setException(e);
      logException(notification, e);
      return null;
    } finally {
      RequestLifeCycle.end();
    }
  }

  private void logException(NotificationInfo notification, Throwable e) {
    if (e != null) {
      if (LOG.isDebugEnabled()) {
        LOG.warn("Error building notification content: {}", notification, e);
      } else {
        LOG.warn("Error building notification content: {}, error: {}", notification, e.getMessage());
      }
    }
  }

  @Override
  protected boolean makeDigest(NotificationContext ctx, Writer writer) {
    return false;
  }

  private final Product getProduct(NotificationInfo notification) {
    String productIdString = notification.getValueOwnerParameter("PRODUCT_ID");
    if (StringUtils.isBlank(productIdString)) {
      throw new IllegalStateException("Product id is missing in notification");
    }
    long productId = Long.parseLong(productIdString);
    return getPerkStoreService().getProductById(productId);
  }

  private final ProductOrder getProductOrder(NotificationInfo notification, boolean mandatory) {
    String orderIdString = notification.getValueOwnerParameter("ORDER_ID");
    if (StringUtils.isBlank(orderIdString)) {
      if (mandatory) {
        throw new IllegalStateException("Order is mandatory");
      }
      return null;
    }
    long orderId = Long.parseLong(orderIdString);
    if (orderId == 0) {
      throw new IllegalStateException("Order id is equal to 0 in notification");
    }
    return getPerkStoreService().getOrderById(orderId);
  }

  public PerkStoreService getPerkStoreService() {
    if (perkStoreService == null) {
      perkStoreService = CommonsUtils.getService(PerkStoreService.class);
    }
    return perkStoreService;
  }

  @Override
  public Template getTemplateEngine() {
    if (this.development) {
      String templatePath = null;
      try {
        templatePath = templateProvider.getTemplateFilePathConfigs().get(key);
        String template = TemplateUtils.loadGroovyTemplate(templatePath);
        if (StringUtils.isBlank(template)) {
          throw new IllegalStateException("Template with path " + templatePath + " wasn't found");
        }
        return new GStringTemplateEngine().createTemplate(template);
      } catch (Exception e) {
        LOG.warn("Error while compiling template {}", templatePath, e);
        try {
          return new GStringTemplateEngine().createTemplate("");
        } catch (Exception e1) {
          return null;
        }
      }
    } else {
      return super.getTemplateEngine();
    }
  }

  public TemplateProvider getTemplateProvider() {
    return templateProvider;
  }
}
