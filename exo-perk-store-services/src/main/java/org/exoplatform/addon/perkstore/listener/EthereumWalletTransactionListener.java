package org.exoplatform.addon.perkstore.listener;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import org.exoplatform.addon.perkstore.service.PerkStoreService;
import org.exoplatform.container.ExoContainer;
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
      RequestLifeCycle.begin(this.container);
      try {
        this.perkStoreService.saveOrderPaymentStatus(hash, status);
      } finally {
        RequestLifeCycle.end();
      }
    }
  }

}
