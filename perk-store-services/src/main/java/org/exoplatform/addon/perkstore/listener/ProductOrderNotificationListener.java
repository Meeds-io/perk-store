package org.exoplatform.addon.perkstore.listener;

import static org.exoplatform.addon.perkstore.service.utils.NotificationUtils.*;

import org.exoplatform.addon.perkstore.model.Product;
import org.exoplatform.addon.perkstore.model.ProductOrderModification;
import org.exoplatform.addon.perkstore.model.constant.ProductOrderModificationType;
import org.exoplatform.addon.perkstore.service.PerkStoreService;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.listener.*;

@Asynchronous
public class ProductOrderNotificationListener extends Listener<Product, ProductOrderModification> {

  private ExoContainer     container;

  private PerkStoreService perkStoreService;

  public ProductOrderNotificationListener(ExoContainer container) {
    this.container = container;
  }

  @Override
  public void onEvent(Event<Product, ProductOrderModification> event) throws Exception {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      ProductOrderModification orderModification = event.getData();
      ProductOrderModificationType modificationType = orderModification.getModificationType();
      boolean isNew = modificationType == ProductOrderModificationType.NEW;

      NotificationContext ctx = NotificationContextImpl.cloneInstance();

      ctx.append(SETTINGS_PARAMETER, getPerkStoreService().getGlobalSettings());
      ctx.append(PRODUCT_PARAMETER, event.getSource());
      ctx.append(NEW_ORDER_PARAMETER, orderModification.getNewValue());
      if (orderModification.getOldValue() != null) {
        ctx.append(OLD_ORDER_PARAMETER, orderModification.getOldValue());
      }
      if (orderModification.getLastModifier() != null) {
        ctx.append(MODIFIER_PARAMETER, orderModification.getLastModifier());
      }
      ctx.append(ORDER_MODIFICATION_TYPE_PARAMETER, modificationType);
      ctx.append(ORDER_IS_NEW_PARAMETER, isNew);

      String pluginId = isNew ? PERKSTORE_ORDER_ADDED_NOTIFICATION_PLUGIN : PERKSTORE_ORDER_MODIFIED_NOTIFICATION_PLUGIN;

      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(pluginId))).execute(ctx);
    } finally {
      RequestLifeCycle.end();
    }
  }

  private PerkStoreService getPerkStoreService() {
    if (perkStoreService == null) {
      perkStoreService = container.getComponentInstanceOfType(PerkStoreService.class);
    }
    return perkStoreService;
  }

}
