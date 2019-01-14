package org.exoplatform.addon.perkstore.listener;

import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.addon.perkstore.model.*;
import org.exoplatform.addon.perkstore.service.PerkStoreService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.*;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

@Asynchronous
public class WebSocketOrderListener extends Listener<Product, ProductOrder> {

  private ContinuationService continuationService;

  private PerkStoreService    perkStoreService;

  @Override
  public void onEvent(Event<Product, ProductOrder> event) throws Exception {
    Product product = event.getSource();
    ProductOrder order = event.getData();
    GlobalSettings globalSettings = getPerkStoreService().getGlobalSettings();

    Set<String> recipientUsers = new HashSet<>();
    boolean sendToAll = getProductAccessUsersList(recipientUsers, product, globalSettings);

    getContinuationService();

    String message = transformToString(order);

    if (sendToAll) {
      continuationService.sendBroadcastMessage(SETTINGS_COMETD_CHANNEL, message);
    } else {
      for (String recipient : recipientUsers) {
        if (continuationService.isPresent(recipient)) {
          continuationService.sendMessage(recipient, SETTINGS_COMETD_CHANNEL, message);
        }
      }
    }
  }

  public ContinuationService getContinuationService() {
    if (continuationService == null) {
      continuationService = CommonsUtils.getService(ContinuationService.class);
    }
    return continuationService;
  }

  public PerkStoreService getPerkStoreService() {
    if (perkStoreService == null) {
      perkStoreService = CommonsUtils.getService(PerkStoreService.class);
    }
    return perkStoreService;
  }
}
