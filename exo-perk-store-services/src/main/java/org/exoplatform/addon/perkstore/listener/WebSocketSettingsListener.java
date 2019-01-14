package org.exoplatform.addon.perkstore.listener;

import static org.exoplatform.addon.perkstore.service.utils.Utils.*;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.addon.perkstore.model.GlobalSettings;
import org.exoplatform.addon.perkstore.service.PerkStoreService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.*;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

@Asynchronous
public class WebSocketSettingsListener extends Listener<PerkStoreService, GlobalSettings> {

  private ContinuationService continuationService;

  @Override
  public void onEvent(Event<PerkStoreService, GlobalSettings> event) throws Exception {
    GlobalSettings globalSettings = event.getData();

    Set<String> recipientUsers = new HashSet<>();
    boolean sendToAll = getApplicationAccessUsersList(recipientUsers, globalSettings);
    String message = transformToString(globalSettings);

    getContinuationService();
    if (sendToAll) {
      continuationService.sendBroadcastMessage(PRODUCT_COMETD_CHANNEL, message);
    } else {
      for (String recipient : recipientUsers) {
        if (continuationService.isPresent(recipient)) {
          continuationService.sendMessage(recipient, PRODUCT_COMETD_CHANNEL, message);
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

}
