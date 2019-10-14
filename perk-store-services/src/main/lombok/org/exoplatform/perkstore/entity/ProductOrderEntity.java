package org.exoplatform.perkstore.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.perkstore.model.constant.ProductOrderStatus;
import org.exoplatform.perkstore.model.constant.ProductOrderTransactionStatus;

@Entity(name = "Order")
@ExoEntity
@Table(name = "ADDONS_PERKSTORE_PRODUCT_ORDER")
@NamedQueries({
    @NamedQuery(name = "Order.getAllProductOrders", query = "SELECT distinct(o) FROM Order o WHERE o.product.id = :productId ORDER BY o.createdDate DESC"),
    @NamedQuery(name = "Order.countOrderedQuantityByProductId", query = "SELECT SUM(o.quantity) FROM Order o WHERE o.product.id = :productId"),
    @NamedQuery(name = "Order.countRefundedQuantityByProductId", query = "SELECT SUM(o.refundedQuantity) FROM Order o WHERE o.product.id = :productId"),
    @NamedQuery(name = "Order.countOrderedQuantityByProductIdAndStatus", query = "SELECT SUM(o.quantity) FROM Order o WHERE o.product.id = :productId AND o.status = :status"),
    @NamedQuery(name = "Order.countRemainingOrdersByProductId", query = "SELECT COUNT(o) FROM Order o WHERE o.product.id = :productId AND o.remainingQuantity > 0"),
    @NamedQuery(name = "Order.countRemainingOrdersByIdentityIdAndProductId", query = "SELECT COUNT(o) FROM Order o WHERE o.product.id = :productId AND o.senderId = :identityId AND o.remainingQuantity > 0"),
    @NamedQuery(name = "Order.countUserTotalPurchasedQuantity", query = "SELECT SUM(o.quantity) FROM Order o WHERE o.product.id = :productId AND o.senderId = :identityId"),
    @NamedQuery(name = "Order.countUserTotalRefundedQuantity", query = "SELECT SUM(o.refundedQuantity) FROM Order o WHERE o.product.id = :productId AND o.senderId = :identityId"),
    @NamedQuery(name = "Order.countUserTotalOrderedQuantityByStatus", query = "SELECT SUM(o.quantity) FROM Order o WHERE o.product.id = :productId AND o.senderId = :identityId AND o.status = :status"),
    @NamedQuery(name = "Order.countUserPurchasedQuantityInPeriod", query = "SELECT SUM(o.quantity) FROM Order o WHERE o.product.id = :productId AND o.senderId = :identityId AND o.createdDate > :from AND o.createdDate < :to"),
    @NamedQuery(name = "Order.countUserRefundedQuantityInPeriod", query = "SELECT SUM(o.refundedQuantity) FROM Order o WHERE o.product.id = :productId AND o.senderId = :identityId AND o.createdDate > :from AND o.createdDate < :to"),
    @NamedQuery(name = "Order.countUserOrderedQuantityByStatusInPeriod", query = "SELECT SUM(o.quantity) FROM Order o WHERE o.product.id = :productId AND o.senderId = :identityId AND o.status = :status AND o.createdDate > :from AND o.createdDate < :to"),
    @NamedQuery(name = "Order.findOrderByTransactionHash", query = "SELECT distinct(o) FROM Order o WHERE o.transactionHash = :hash"),
    @NamedQuery(name = "Order.findOrderByRefundTransactionHash", query = "SELECT distinct(o) FROM Order o WHERE o.refundTransactionHash = :hash"),
})
public class ProductOrderEntity implements Serializable {

  private static final long             serialVersionUID = -592052513482849972L;

  @Id
  @SequenceGenerator(name = "SEQ_ADDONS_PERKSTORE_PRODUCT_ORDER_ID", sequenceName = "SEQ_ADDONS_PERKSTORE_PRODUCT_ORDER_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_ADDONS_PERKSTORE_PRODUCT_ORDER_ID")
  @Column(name = "ORDER_ID")
  private Long                          id;

  @Column(name = "TRANSACTION_HASH", nullable = true)
  private String                        transactionHash;

  @Column(name = "REFUND_TRANSACTION_HASH", nullable = true)
  private String                        refundTransactionHash;

  @Column(name = "QUANTITY", nullable = false)
  private double                        quantity;

  @Column(name = "AMOUNT", nullable = true)
  private double                        amount;

  @Column(name = "REFUNDED_AMOUNT", nullable = true)
  private double                        refundedAmount;

  @Column(name = "SENDER_ID", nullable = false)
  private long                          senderId;

  @Column(name = "RECEIVER_ID", nullable = false)
  private long                          receiverId;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = "STATUS", nullable = false)
  private ProductOrderStatus            status;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = "TX_STATUS", nullable = false)
  private ProductOrderTransactionStatus transactionStatus;

  @Enumerated(EnumType.ORDINAL)
  @Column(name = "REFUND_TX_STATUS", nullable = false)
  private ProductOrderTransactionStatus refundTransactionStatus;

  @Column(name = "DELIVERED_QUANTITY", nullable = true)
  private double                        deliveredQuantity;

  @Column(name = "REFUNDED_QUANTITY", nullable = true)
  private double                        refundedQuantity;

  @Column(name = "REMAINING_QUANTITY", nullable = true)
  private double                        remainingQuantity;

  @Column(name = "CREATED_DATE", nullable = false)
  private long                          createdDate;

  @Column(name = "DELIVERED_DATE", nullable = true)
  private long                          deliveredDate;

  @Column(name = "REFUNDED_DATE", nullable = true)
  private long                          refundedDate;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "PRODUCT_ID")
  private ProductEntity                 product;

  @Column(name = "ERROR_CODE")
  private int                           errorCode;

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

  public double getRefundedAmount() {
    return refundedAmount;
  }

  public void setRefundedAmount(double refundedAmount) {
    this.refundedAmount = refundedAmount;
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

  public double getDeliveredQuantity() {
    return deliveredQuantity;
  }

  public void setDeliveredQuantity(double deliveredQuantity) {
    this.deliveredQuantity = deliveredQuantity;
  }

  public double getRefundedQuantity() {
    return refundedQuantity;
  }

  public String getRefundTransactionHash() {
    return refundTransactionHash;
  }

  public void setRefundTransactionHash(String refundTransactionHash) {
    this.refundTransactionHash = refundTransactionHash;
  }

  public ProductOrderTransactionStatus getTransactionStatus() {
    return transactionStatus;
  }

  public void setTransactionStatus(ProductOrderTransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public ProductOrderTransactionStatus getRefundTransactionStatus() {
    return refundTransactionStatus;
  }

  public void setRefundTransactionStatus(ProductOrderTransactionStatus refundTransactionStatus) {
    this.refundTransactionStatus = refundTransactionStatus;
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

  public int getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

}
