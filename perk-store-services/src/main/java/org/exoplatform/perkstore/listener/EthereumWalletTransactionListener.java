package org.exoplatform.perkstore.listener;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EthereumWalletTransactionListener extends Listener<Object, Map<String, Object>> {

  private static final Log LOG = ExoLogger.getLogger(EthereumWalletTransactionListener.class);

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
    String hash = null;
    try {
      Map<String, Object> transactionDetails = event.getData();
      if (transactionDetails == null) {
        throw new IllegalStateException("Transaction details is mandatory");
      }
      hash = (String) transactionDetails.get("hash");
      if (StringUtils.isBlank(hash)) {
        throw new IllegalStateException("Transaction hash is mandatory");
      }
      String oldHash = (String) transactionDetails.get("oldHash");
      if (StringUtils.isNotBlank(oldHash)) {
        this.perkStoreService.replaceTransactions(oldHash, hash);
      }
      this.perkStoreService.saveOrderTransactionStatus(transactionDetails);
    } catch (Exception e) {
      LOG.error("Error saving order details after transaction {} gets mined", hash, e);
    } finally {
      RequestLifeCycle.end();
    }
  }

}
