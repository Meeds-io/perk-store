package org.exoplatform.addon.perkstore.listener;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import org.exoplatform.addon.perkstore.service.PerkStoreService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.listener.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@Asynchronous
public class EthereumWalletTransactionListener extends Listener<Object, JSONObject> {
  private static final Log LOG =
                               ExoLogger.getLogger(EthereumWalletTransactionListener.class);

  private ExoContainer     container;

  private PerkStoreService perkStoreService;

  public EthereumWalletTransactionListener(PerkStoreService perkStoreService, ExoContainer container) {
    this.perkStoreService = perkStoreService;
    this.container = container;
  }

  @Override
  public void onEvent(Event<Object, JSONObject> event) throws Exception {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      JSONObject transactionDetails = event.getData();
      if (transactionDetails == null) {
        throw new IllegalStateException("Transaction details is mandatory");
      }
      String hash = (String) transactionDetails.get("hash");
      if (StringUtils.isBlank(hash)) {
        throw new IllegalStateException("Transaction hash is mandatory");
      }

      Boolean status = (Boolean) transactionDetails.get("status");
      if (status == null) {
        LOG.error("Transaction with hash " + hash + " status is null");
      } else {
        // This could be triggered three times:
        // - when contract TX saved
        // - sender TX saved
        // - receiver TX saved
        this.perkStoreService.saveOrderTransactionStatus(hash, status);
      }
    } finally {
      RequestLifeCycle.end();
    }
  }

}
