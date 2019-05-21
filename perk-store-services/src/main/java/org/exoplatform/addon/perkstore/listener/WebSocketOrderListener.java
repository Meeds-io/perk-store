package org.exoplatform.addon.perkstore.listener;

import static org.exoplatform.addon.perkstore.service.utils.Utils.getProductAccessUsersList;
import static org.exoplatform.addon.perkstore.service.utils.Utils.getProductManagersUsersList;

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

    Set<String> readOnlyUsers = new HashSet<>();
    boolean sendToAll = getProductAccessUsersList(readOnlyUsers, product, globalSettings);

    Set<String> orderDetailViewerUsers = new HashSet<>();
    getProductManagersUsersList(orderDetailViewerUsers, product, globalSettings);
    // Notify sender about the order changes
    orderDetailViewerUsers.add(order.getSender().getId());

    readOnlyUsers.removeAll(orderDetailViewerUsers);
    // Send to simple users only product attributes modification
    getWebSocketService().sendMessage(event.getEventName(), readOnlyUsers, sendToAll, product);

    // Send to managers and order sender the order modifications
    getWebSocketService().sendMessage(event.getEventName(), orderDetailViewerUsers, false, product, order);
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
