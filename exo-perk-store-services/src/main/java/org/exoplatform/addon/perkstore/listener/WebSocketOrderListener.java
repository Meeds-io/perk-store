package org.exoplatform.addon.perkstore.listener;

import static org.exoplatform.addon.perkstore.service.utils.Utils.getProductAccessUsersList;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.addon.perkstore.service.PerkStoreService;
import org.exoplatform.addon.perkstore.service.PerkStoreWebSocketService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.*;

@Asynchronous
public class WebSocketOrderListener extends Listener<Product, ProductOrder> {

  private PerkStoreService          perkStoreService;

  private PerkStoreWebSocketService webSocketService;

  @Override
  public void onEvent(Event<Product, ProductOrder> event) throws Exception {
    Product product = event.getSource();
    ProductOrder order = event.getData();
    GlobalSettings globalSettings = getPerkStoreService().getGlobalSettings();

    Set<String> recipientUsers = new HashSet<>();
    boolean sendToAll = getProductAccessUsersList(recipientUsers, product, globalSettings);
    getWebSocketService().sendMessage(event.getEventName(), recipientUsers, sendToAll, product, order);
  }

  public PerkStoreService getPerkStoreService() {
    if (perkStoreService == null) {
      perkStoreService = CommonsUtils.getService(PerkStoreService.class);
    }
    return perkStoreService;
  }

  private PerkStoreWebSocketService getWebSocketService() {
    if (webSocketService == null) {
      webSocketService = CommonsUtils.getService(PerkStoreWebSocketService.class);
    }
    return webSocketService;
  }
}
