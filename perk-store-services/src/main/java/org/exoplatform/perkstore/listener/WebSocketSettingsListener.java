package org.exoplatform.perkstore.listener;

import static org.exoplatform.perkstore.service.utils.Utils.getApplicationAccessUsersList;

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.perkstore.model.GlobalSettings;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.perkstore.service.PerkStoreWebSocketService;
import org.exoplatform.services.listener.*;

@Asynchronous
public class WebSocketSettingsListener extends Listener<PerkStoreService, GlobalSettings> {

  private PerkStoreWebSocketService webSocketService;

  @Override
  public void onEvent(Event<PerkStoreService, GlobalSettings> event) throws Exception {
    GlobalSettings globalSettings = event.getData();

    Set<String> recipientUsers = new HashSet<>();
    boolean sendToAll = getApplicationAccessUsersList(recipientUsers, globalSettings);

    getWebSocketService().sendMessage(event.getEventName(), recipientUsers, sendToAll, globalSettings);
  }

  private PerkStoreWebSocketService getWebSocketService() {
    if (webSocketService == null) {
      webSocketService = CommonsUtils.getService(PerkStoreWebSocketService.class);
    }
    return webSocketService;
  }

}
