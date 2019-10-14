package org.exoplatform.perkstore.dao;

import static org.exoplatform.perkstore.service.utils.Utils.USER_ACCOUNT_TYPE;
import static org.exoplatform.perkstore.service.utils.Utils.getIdentityByTypeAndId;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.perkstore.entity.ProductOrderEntity;
import org.exoplatform.perkstore.model.OrderFilter;
import org.exoplatform.perkstore.model.constant.ProductOrderStatus;
import org.exoplatform.perkstore.service.PerkStoreService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;

public class PerkStoreOrderDAO extends GenericDAOJPAImpl<ProductOrderEntity, Long> {
  private static final String TO_DATE_PARAMETER     = "to";

  private static final String FROM_DATE_PARAMETER   = "from";

  private static final Log    LOG                   = ExoLogger.getLogger(PerkStoreService.class);

  private static final String IDENTITY_ID_PARAMETER = "identityId";

  private static final String AND_OPERATOR          = " AND ";

  private static final String PRODUCT_ID_PARAMETER  = "productId";

  private static final String STATUS_PARAMETER      = "status";

  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(List<ProductOrderEntity> entities) {
    throw new UnsupportedOperationException();
  }

  public double countOrderedQuantityByProductId(long productId) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countOrderedQuantityByProductId", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public double countOrderedQuantityByProductIdAndStatus(long productId, ProductOrderStatus status) {
    TypedQuery<Double> query =
                             getEntityManager().createNamedQuery("Order.countOrderedQuantityByProductIdAndStatus", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter(STATUS_PARAMETER, status);
    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public double countRefundedQuantityByProductId(long productId) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countRefundedQuantityByProductId", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public long countRemainingOrdersToProcessByProductId(long productId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("Order.countRemainingOrdersByProductId", Long.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    Long result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public long countRemainingOrdersByIdentityIdAndProductId(long identityId, long productId) {
    TypedQuery<Long> query =
                           getEntityManager().createNamedQuery("Order.countRemainingOrdersByIdentityIdAndProductId", Long.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter(IDENTITY_ID_PARAMETER, identityId);
    Long result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public double countUserTotalPurchasedQuantity(long productId, long identityId) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserTotalPurchasedQuantity", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter(IDENTITY_ID_PARAMETER, identityId);
    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public double countUserTotalRefundedQuantity(long productId, long identityId) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserTotalRefundedQuantity", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter(IDENTITY_ID_PARAMETER, identityId);
    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public double countUserTotalOrderedQuantityByStatus(long productId, long identityId, ProductOrderStatus status) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserTotalOrderedQuantityByStatus", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter(IDENTITY_ID_PARAMETER, identityId);
    query.setParameter(STATUS_PARAMETER, status);

    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public double countUserPurchasedQuantityInPeriod(long productId, long identityId, long startDate, long endDate) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserPurchasedQuantityInPeriod", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter(IDENTITY_ID_PARAMETER, identityId);
    query.setParameter(FROM_DATE_PARAMETER, startDate);
    query.setParameter(TO_DATE_PARAMETER, endDate);
    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public double countUserRefundedQuantityInPeriod(long productId, long identityId, long startDate, long endDate) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserRefundedQuantityInPeriod", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter(IDENTITY_ID_PARAMETER, identityId);
    query.setParameter(FROM_DATE_PARAMETER, startDate);
    query.setParameter(TO_DATE_PARAMETER, endDate);
    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public double countUserOrderedQuantityByStatusInPeriod(long productId,
                                                         long identityId,
                                                         long startDate,
                                                         long endDate,
                                                         ProductOrderStatus status) {
    TypedQuery<Double> query =
                             getEntityManager().createNamedQuery("Order.countUserOrderedQuantityByStatusInPeriod", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter(IDENTITY_ID_PARAMETER, identityId);
    query.setParameter(FROM_DATE_PARAMETER, startDate);
    query.setParameter(TO_DATE_PARAMETER, endDate);
    query.setParameter(STATUS_PARAMETER, status);

    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public List<ProductOrderEntity> getOrders(String username, OrderFilter filter) {
    TypedQuery<ProductOrderEntity> query = getEntityManager().createQuery(getFilterQueryString(username, filter),
                                                                          ProductOrderEntity.class);

    query.setMaxResults(filter.getLimit());
    return query.getResultList();
  }

  public ProductOrderEntity findOrderByTransactionHash(String hash) {
    TypedQuery<ProductOrderEntity> query = getEntityManager().createNamedQuery("Order.findOrderByTransactionHash",
                                                                               ProductOrderEntity.class);

    query.setParameter("hash", hash);
    List<ProductOrderEntity> results = query.getResultList();
    if (results == null || results.isEmpty()) {
      return null;
    } else if (results.size() == 1) {
      return results.get(0);
    } else {
      LOG.warn("More than one order was found with transaction hash {}", hash);
      return results.get(0);
    }
  }

  public ProductOrderEntity findOrderByRefundTransactionHash(String hash) {
    TypedQuery<ProductOrderEntity> query = getEntityManager().createNamedQuery("Order.findOrderByRefundTransactionHash",
                                                                               ProductOrderEntity.class);

    query.setParameter("hash", hash);

    List<ProductOrderEntity> results = query.getResultList();
    if (results == null || results.isEmpty()) {
      return null;
    } else if (results.size() == 1) {
      return results.get(0);
    } else {
      LOG.warn("More than one order was found with refund transaction hash {}", hash);
      return results.get(0);
    }
  }

  private String getFilterQueryString(String username, OrderFilter filter) {
    StringBuilder query = new StringBuilder();

    boolean firstConditionAdded = false;
    if (filter.getProductId() != 0) {
      query.append(" o.product.id = ");
      query.append(filter.getProductId());
      firstConditionAdded = true;
    }

    if (StringUtils.isNotBlank(username)) {
      Identity identity = getIdentityByTypeAndId(USER_ACCOUNT_TYPE, username);
      if (identity != null) {
        if (firstConditionAdded) {
          query.append(AND_OPERATOR);
        } else {
          firstConditionAdded = true;
        }
        query.append(" o.senderId = ");
        query.append(identity.getId());
      }
    }

    if (filter.isNotProcessed()) {
      if (firstConditionAdded) {
        query.append(AND_OPERATOR);
      } else {
        firstConditionAdded = true;
      }
      query.append(" o.remainingQuantity > 0");
    } else {
      List<Integer> statuses = getSelectedStatuses(filter);

      if (!statuses.isEmpty()) {
        if (firstConditionAdded) {
          query.append(AND_OPERATOR);
        } else {
          firstConditionAdded = true;
        }
        query.append(" o.status in (");
        query.append(StringUtils.join(statuses, ", "));
        query.append(" )");
      }
    }

    long selectedDate = filter.getSelectedDate();
    if (filter.isSearchInDates() && selectedDate > 0) {
      if (firstConditionAdded) {
        query.append(AND_OPERATOR);
      }
      query.append(" o.createdDate > ");
      query.append(getStartOfDayMillis(selectedDate));
      query.append(" AND o.createdDate < ");
      query.append(getEndOfDayMillis(selectedDate));
    }

    if (StringUtils.isEmpty(query.toString().trim())) {
      query.insert(0, "Select o from Order o ");
    } else {
      query.insert(0, "Select o from Order o WHERE ");
    }
    query.append(" ORDER BY createdDate DESC");
    return query.toString();
  }

  private List<Integer> getSelectedStatuses(OrderFilter filter) {
    List<Integer> statuses = new ArrayList<>();
    if (filter.isOrdered()) {
      statuses.add(ProductOrderStatus.ORDERED.ordinal());
    }
    if (filter.isPaid()) {
      statuses.add(ProductOrderStatus.PAID.ordinal());
    }
    if (filter.isCanceled()) {
      statuses.add(ProductOrderStatus.CANCELED.ordinal());
    }
    if (filter.isPartial()) {
      statuses.add(ProductOrderStatus.PARTIAL.ordinal());
    }
    if (filter.isError()) {
      statuses.add(ProductOrderStatus.ERROR.ordinal());
    }
    if (filter.isDelivered()) {
      statuses.add(ProductOrderStatus.DELIVERED.ordinal());
    }
    if (filter.isRefunded()) {
      statuses.add(ProductOrderStatus.REFUNDED.ordinal());
    }
    if (filter.isFraud()) {
      statuses.add(ProductOrderStatus.FRAUD.ordinal());
    }
    return statuses;
  }

  private long getStartOfDayMillis(long selectedDate) {
    Calendar selectedDateCalendar = Calendar.getInstance();
    selectedDateCalendar.setTimeInMillis(selectedDate);
    LocalDate date = LocalDate.of(selectedDateCalendar.get(Calendar.YEAR),
                                  selectedDateCalendar.get(Calendar.MONTH) + 1,
                                  selectedDateCalendar.get(Calendar.DAY_OF_MONTH));
    long startTimeOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    return startTimeOfDay * 1000;
  }

  private long getEndOfDayMillis(long selectedDate) {
    Calendar selectedDateCalendar = Calendar.getInstance();
    selectedDateCalendar.setTimeInMillis(selectedDate);
    LocalDate date = LocalDate.of(selectedDateCalendar.get(Calendar.YEAR),
                                  selectedDateCalendar.get(Calendar.MONTH) + 1,
                                  selectedDateCalendar.get(Calendar.DAY_OF_MONTH));
    date = date.plus(1, ChronoUnit.DAYS);
    long startTimeOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    return startTimeOfDay * 1000;
  }

}
