package org.exoplatform.perkstore.listener;

import static org.exoplatform.perkstore.service.utils.Utils.getProductAccessUsersList;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.perkstore.model.GlobalSettings;
import org.exoplatform.perkstore.model.Product;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.perkstore.service.PerkStoreWebSocketService;
import org.exoplatform.services.listener.*;

@Asynchronous
public class WebSocketProductListener extends Listener<Product, Boolean> {

  private PerkStoreWebSocketService webSocketService;

  private PerkStoreService          perkStoreService;

  @Override
  public void onEvent(Event<Product, Boolean> event) throws Exception {
    Product product = event.getSource();

    if (!product.isEnabled()) {
      return;
    }
    GlobalSettings globalSettings = getPerkStoreService().getGlobalSettings();

    Set<String> recipientUsers = new HashSet<>();
    boolean sendToAll = getProductAccessUsersList(recipientUsers, product, globalSettings);
    if (!sendToAll && product.getLastModifier() != null) {
      recipientUsers.add(product.getLastModifier().getId());
    }
    getWebSocketService().sendMessage(event.getEventName(), recipientUsers, sendToAll, product);
  }

  private PerkStoreWebSocketService getWebSocketService() {
    if (webSocketService == null) {
      webSocketService = CommonsUtils.getService(PerkStoreWebSocketService.class);
    }
    return webSocketService;
  }

  public PerkStoreService getPerkStoreService() {
    if (perkStoreService == null) {
      perkStoreService = CommonsUtils.getService(PerkStoreService.class);
    }
    return perkStoreService;
  }
}
