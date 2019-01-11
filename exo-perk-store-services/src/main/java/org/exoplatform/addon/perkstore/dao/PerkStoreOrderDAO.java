package org.exoplatform.addon.perkstore.dao;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.addon.perkstore.entity.ProductOrderEntity;
import org.exoplatform.addon.perkstore.model.OrderFilter;
import org.exoplatform.addon.perkstore.model.ProductOrderStatus;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class PerkStoreOrderDAO extends GenericDAOJPAImpl<ProductOrderEntity, Long> {
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
    return query.getSingleResult();
  }

  public long countRemainingOrdersToProcessByProductId(long id) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("Order.countRemainingOrdersByProductId", Long.class);
    query.setParameter(PRODUCT_ID_PARAMETER, id);
    return query.getSingleResult();
  }

  public double countUserTotalPurchasedQuantity(long productId, long identityId) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserTotalPurchasedQuantity", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter("sender", identityId);
    return query.getSingleResult();
  }

  public double countUserPurchasedQuantityInPeriod(long productId, long identityId, long startDate, long endDate) {
    TypedQuery<Double> query = getEntityManager().createNamedQuery("Order.countUserPurchasedQuantityInPeriod", Double.class);
    query.setParameter(PRODUCT_ID_PARAMETER, productId);
    query.setParameter("sender", identityId);
    query.setParameter("from", startDate);
    query.setParameter("to", endDate);
    return query.getSingleResult();
  }

  public List<ProductOrderEntity> getOrders(String username, OrderFilter filter) {
    TypedQuery<ProductOrderEntity> query = getEntityManager().createQuery(getFilterQueryString(username, filter),
                                                                          ProductOrderEntity.class);

    query.setMaxResults(filter.getLimit());
    return query.getResultList();
  }

  public ProductOrderEntity findOrderByTransactionHash(String hash) {
    TypedQuery<ProductOrderEntity> query = getEntityManager().createQuery("Order.findOrderByTransactionHash",
                                                                          ProductOrderEntity.class);

    query.setParameter("hash", hash);
    return query.getSingleResult();
  }

  private String getFilterQueryString(String username, OrderFilter filter) {
    StringBuilder query = new StringBuilder("Select o from Order WHERE ");
    query.append(" o.id = ");
    query.append(filter.getProductId());

    if (StringUtils.isNotBlank(username)) {
      query.append(" AND o.creator = '");
      query.append(username);
      query.append("'");
    }

    if (filter.isNotProcessed()) {
      query.append(" AND o.remainingQuantity > 0");
    } else {
      List<Integer> statuses = getSelectedStatuses(filter);

      if (!statuses.isEmpty()) {
        query.append(" AND o.status in (");
        query.append(StringUtils.join(statuses, ", "));
        query.append(" )");
      }
    }

    Date selectedDate = filter.getSelectedDate();
    if (filter.isSearchInDates() && selectedDate != null) {
      query.append(" AND o.createdDate > ");
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
    if (filter.isPayed()) {
      statuses.add(ProductOrderStatus.PAYED.ordinal());
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

  private long getStartOfDayMillis(Date selectedDate) {
    Calendar selectedDateCalendar = Calendar.getInstance();
    selectedDateCalendar.setTime(selectedDate);
    LocalDate date = LocalDate.of(selectedDateCalendar.get(Calendar.YEAR),
                                  selectedDateCalendar.get(Calendar.MONTH),
                                  selectedDateCalendar.get(Calendar.DAY_OF_MONTH));
    long startTimeOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    return startTimeOfDay * 1000;
  }

  private long getEndOfDayMillis(Date selectedDate) {
    Calendar selectedDateCalendar = Calendar.getInstance();
    selectedDateCalendar.setTime(selectedDate);
    LocalDate date = LocalDate.of(selectedDateCalendar.get(Calendar.YEAR),
                                  selectedDateCalendar.get(Calendar.MONTH),
                                  selectedDateCalendar.get(Calendar.DAY_OF_MONTH));
    date = date.plus(1, ChronoUnit.DAYS);
    long startTimeOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
    return startTimeOfDay * 1000;
  }

}
