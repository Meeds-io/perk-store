package org.exoplatform.addon.perkstore.statistic;

import java.util.Map;

/**
 * This service is used to add statistic log entry by using annotation
 * {@link ExoPerkStoreStatistic} on methods
 */
public interface ExoPerkStoreStatisticService {
  /**
   * Retrieve statistic log parameters
   * 
   * @param operation
   * @param result
   * @param methodArgs
   * @return a {@link Map} of parameters to include in statistic log
   */
  Map<String, Object> getStatisticParameters(String operation, Object result, Object... methodArgs);
}
