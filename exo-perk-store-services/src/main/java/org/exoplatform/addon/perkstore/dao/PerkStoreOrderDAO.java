package org.exoplatform.addon.perkstore.dao;

import static org.exoplatform.addon.perkstore.service.utils.Utils.USER_ACCOUNT_TYPE;
import static org.exoplatform.addon.perkstore.service.utils.Utils.getIdentityByTypeAndId;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.addon.perkstore.entity.ProductOrderEntity;
import org.exoplatform.addon.perkstore.model.OrderFilter;
import org.exoplatform.addon.perkstore.model.ProductOrderStatus;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.identity.model.Identity;

public class PerkStoreOrderDAO extends GenericDAOJPAImpl<ProductOrderEntity, Long> {
  private static final String AND_OPERATOR = " AND ";
  private static final String PRODUCT_ID_PARAMETER = "productId";

  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(ProductOrderEntity entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deleteAll(List<ProductOrderEntity> entities) {
    throw new UnsupportedOperationException();
  }

  public double countOrderedQuantityByProductId(long id) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countOrderedQuantityByProductId", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, id);
    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public long countRemainingOrdersToProcessByProductId(long id) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("Order.countRemainingOrdersByProductId", Long.class);
    query.setParameter(PRODUCT_ID_PARAMETER, id);
    Long result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public double countUserTotalPurchasedQuantity(long productId, long identityId) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserTotalPurchasedQuantity", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter("identityId", identityId);
    Double result = query.getSingleResult();
    return result == null ? 0 : result;
  }

  public double countUserPurchasedQuantityInPeriod(long productId, long identityId, long startDate, long endDate) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserPurchasedQuantityInPeriod", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter("identityId", identityId);
    query.setParameter("from", startDate);
    query.setParameter("to", endDate);
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
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  private String getFilterQueryString(String username, OrderFilter filter) {
    StringBuilder query = new StringBuilder("Select o from Order o WHERE ");

    boolean firstConditionAdded = false;
    if (filter.getProductId() != 0) {
      query.append(" o.product.id = ");
      query.append(filter.getProductId());
      firstConditionAdded = true;
    }

    if (StringUtils.isNotBlank(username)) {
      Identity identity = getIdentityByTypeAndId(USER_ACCOUNT_TYPE, username);
      if (firstConditionAdded) {
        query.append(AND_OPERATOR);
      } else {
        firstConditionAdded = true;
      }
      query.append(" o.senderId = ");
      query.append(identity.getId());
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
    if (filter.isCanceled()) {
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
