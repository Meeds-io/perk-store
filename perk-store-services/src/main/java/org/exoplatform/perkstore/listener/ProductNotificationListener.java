package org.exoplatform.perkstore.listener;

import static org.exoplatform.perkstore.service.utils.NotificationUtils.*;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.perkstore.model.Product;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.services.listener.*;

@Asynchronous
public class ProductNotificationListener extends Listener<Product, Boolean> {

  private ExoContainer     container;

  private PerkStoreService perkStoreService;

  public ProductNotificationListener(ExoContainer container) {
    this.container = container;
  }

  @Override
  public void onEvent(Event<Product, Boolean> event) throws Exception {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      Product product = event.getSource();

      // Avoid sending notifications about a disabled product
      if (!product.isEnabled()) {
        return;
      }

      boolean isNew = event.getData();

      NotificationContext ctx = NotificationContextImpl.cloneInstance();

      ctx.append(SETTINGS_PARAMETER, getPerkStoreService().getGlobalSettings());
      ctx.append(PRODUCT_PARAMETER, product);
      ctx.append(PRODUCT_IS_NEW_PARAMETER, isNew);

      String pluginId = isNew ? PERKSTORE_PRODUCT_ADDED_NOTIFICATION_PLUGIN
                              : PERKSTORE_PRODUCT_MODIFIED_NOTIFICATION_PLUGIN;

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
