package org.exoplatform.perkstore.listener;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.services.listener.*;

@Asynchronous
public class EthereumWalletTransactionListener extends Listener<Object, Map<String, Object>> {

  private ExoContainer     container;

  private PerkStoreService perkStoreService;

  public EthereumWalletTransactionListener(PerkStoreService perkStoreService, ExoContainer container) {
    this.perkStoreService = perkStoreService;
    this.container = container;
  }

  @Override
  public void onEvent(Event<Object, Map<String, Object>> event) throws Exception {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      Map<String, Object> transactionDetails = event.getData();
      if (transactionDetails == null) {
        throw new IllegalStateException("Transaction details is mandatory");
      }
      String hash = (String) transactionDetails.get("hash");
      if (StringUtils.isBlank(hash)) {
        throw new IllegalStateException("Transaction hash is mandatory");
      }

      this.perkStoreService.saveOrderTransactionStatus(transactionDetails);
    } finally {
      RequestLifeCycle.end();
    }
  }

}
