package org.exoplatform.addon.perkstore.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.DynamicUpdate;

import org.exoplatform.addon.perkstore.model.ProductOrderStatus;
import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "Order")
@ExoEntity
@DynamicUpdate
@Table(name = "ADDONS_PERKSTORE_PRODUCT_ORDER")
@NamedQueries({
    @NamedQuery(name = "Order.getAllProductOrders", query = "SELECT o FROM Order WHERE o.product.id = :productId ORDER BY o.createdDate DESC"),
    @NamedQuery(name = "Order.countOrderedQuantityByProductId", query = "SELECT SUM(o.quantity) FROM Order WHERE o.product.id = :productId"),
    @NamedQuery(name = "Order.countRemainingOrdersByProductId", query = "SELECT COUNT(o) FROM Order WHERE o.product.id = :productId AND o.remainingQuantity > 0"),
    @NamedQuery(name = "Order.countUserTotalPurchasedQuantity", query = "SELECT SUM(o.quantity) FROM Order WHERE o.product.id = :productId AND o.senderId = :identityId"),
    @NamedQuery(name = "Order.countUserPurchasedQuantityInPeriod", query = "SELECT SUM(o.quantity) FROM Order WHERE o.product.id = :productId AND o.senderId = :identityId AND o.createdDate > :to AND o.createdDate < :from"),
    @NamedQuery(name = "Order.findOrderByTransactionHash", query = "SELECT o FROM Order WHERE o.transactionHash = :hash"),
})
public class ProductOrderEntity implements Serializable {

  private static final long  serialVersionUID = -592052513482849972L;

  @Id
  @SequenceGenerator(name = "SEQ_ADDONS_PERKSTORE_PRODUCT_ORDER_ID", sequenceName = "SEQ_ADDONS_PERKSTORE_PRODUCT_ORDER_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_ADDONS_PERKSTORE_PRODUCT_ORDER_ID")
  @Column(name = "ORDER_ID")
  private Long               id;

  @Column(name = "TRANSACTION_HASH", nullable = false)
  private String             transactionHash;

  @Column(name = "QUANTITY", nullable = false)
  private double             quantity;

  @Column(name = "amount", nullable = false)
  private double             amount;

  @Column(name = "SENDER_ID", nullable = false)
  private long               senderId;

  @Column(name = "RECEIVER_ID", nullable = false)
  private long               receiverId;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = "status", nullable = false)
  private ProductOrderStatus status;

  @Column(name = "ERROR", nullable = true)
  private String             error;

  @Column(name = "DELIVERED_QUANTITY", nullable = true)
  private double             deliveredQuantity;

  @Column(name = "REFUNDED_QUANTITY", nullable = true)
  private double             refundedQuantity;

  @Column(name = "REMAINING_QUANTITY", nullable = true)
  private double             remainingQuantity;

  @Column(name = "CREATED_DATE", nullable = false)
  private long               createdDate;

  @Column(name = "DELIVERED_DATE", nullable = true)
  private long               deliveredDate;

  @Column(name = "REFUNDED_DATE", nullable = true)
  private long               refundedDate;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID")
  private ProductEntity      product;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTransactionHash() {
    return transactionHash;
  }

  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }

  public double getQuantity() {
    return quantity;
  }

  public void setQuantity(double quantity) {
    this.quantity = quantity;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public long getSenderId() {
    return senderId;
  }

  public void setSenderId(long senderId) {
    this.senderId = senderId;
  }

  public long getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(long receiverId) {
    this.receiverId = receiverId;
  }

  public ProductOrderStatus getStatus() {
    return status;
  }

  public void setStatus(ProductOrderStatus status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public double getDeliveredQuantity() {
    return deliveredQuantity;
  }

  public void setDeliveredQuantity(double deliveredQuantity) {
    this.deliveredQuantity = deliveredQuantity;
  }

  public double getRefundedQuantity() {
    return refundedQuantity;
  }

  public void setRefundedQuantity(double refundedQuantity) {
    this.refundedQuantity = refundedQuantity;
  }

  public double getRemainingQuantity() {
    return remainingQuantity;
  }

  public void setRemainingQuantity(double remainingQuantity) {
    this.remainingQuantity = remainingQuantity;
  }

  public long getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(long createdDate) {
    this.createdDate = createdDate;
  }

  public long getDeliveredDate() {
    return deliveredDate;
  }

  public void setDeliveredDate(long deliveredDate) {
    this.deliveredDate = deliveredDate;
  }

  public long getRefundedDate() {
    return refundedDate;
  }

  public void setRefundedDate(long refundedDate) {
    this.refundedDate = refundedDate;
  }

  public ProductEntity getProduct() {
    return product;
  }

  public void setProduct(ProductEntity product) {
    this.product = product;
  }

}
