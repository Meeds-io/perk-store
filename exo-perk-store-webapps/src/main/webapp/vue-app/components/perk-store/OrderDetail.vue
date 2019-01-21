<template>
  <v-hover v-if="order" class="orderDetailParent">
    <v-card slot-scope="{ hover }" :class="`elevation-${hover ? 9 : 3}`">
      <v-card-title v-if="order.sender" class="pt-1 pb-1">
        <h4>
          <a
            :href="orderLink"
            rel="nofollow"
            target="_blank">
            <strong>#{{ order.id }}</strong>
          </a>
        </h4>
        <v-spacer />
        <template v-if="userData && userData.canEdit">
          <select
            v-model="order.status"
            class="small mt-1 mb-1 mr-2"
            @change="changeStatus('STATUS')">
            <option v-for="option in statusList" :key="option">
              {{ option }}
            </option>
          </select>
          <div
            v-if="order.remainingQuantityToProcess"
            :title="`${order.remainingQuantityToProcess} to deliver`"
            class="orderQuantityBadgeParent">
            <div class="orderQuantityBadge red">
              {{ order.remainingQuantityToProcess }}
            </div>
          </div>
        </template>
        <span v-else>{{ statusLabel }}</span>
      </v-card-title>
  
      <v-divider />
  
      <v-list dense>
        <v-list-tile>
          <v-list-tile-content>Buyer:</v-list-tile-content>
          <v-list-tile-content class="align-end">
            <div class="ellipsis orderDetailText">
              <profile-link
                :id="order.sender.id"
                :space-id="order.sender.spaceId"
                :url-id="order.sender.spaceURLId"
                :type="order.sender.type"
                :display-name="order.sender.displayName"
                display-avatar />
            </div>
          </v-list-tile-content>
        </v-list-tile>
        <v-list-tile>
          <v-list-tile-content>Date:</v-list-tile-content>
          <v-list-tile-content class="align-end">
            <div :title="createdDateLabel" class="orderCreatedDate ellipsis">
              {{ createdDateLabel }}
            </div>
          </v-list-tile-content>
        </v-list-tile>
        <v-list-tile>
          <v-list-tile-content>Items:</v-list-tile-content>
          <v-list-tile-content class="align-end">
            <div class="ellipsis orderDetailText">
              {{ order.quantity }} x 
              <a
                :href="productLink"
                :title="productTitle"
                @click="openProductDetail">
                {{ productTitle }}
              </a>
            </div>
          </v-list-tile-content>
        </v-list-tile>
        <v-list-tile>
          <v-list-tile-content>Payment:</v-list-tile-content>
          <v-list-tile-content class="align-end">
            <div class="no-wrap ellipsis orderDetailText">
              <v-icon
                v-if="order.transactionStatus === 'SUCCESS'"
                title="Transaction succeeded"
                class="green--text"
                size="16">
                fa-check-circle
              </v-icon>
              <v-icon
                v-if="order.transactionStatus === 'FAILED'"
                title="Transaction failed"
                class="red--text"
                size="16">
                fa-exclamation-circle
              </v-icon>
              <v-icon
                v-if="order.transactionStatus === 'PENDING'"
                title="Transaction in progress"
                class="orange--text"
                size="16">
                far fa-clock
              </v-icon>
              <a
                v-if="order.transactionHash"
                :href="transactionLink"
                rel="nofollow"
                target="_blank">
                {{ order.amount }} {{ symbol }}
              </a>
              to
              <profile-link
                :id="order.receiver.id"
                :space-id="order.receiver.spaceId"
                :url-id="order.receiver.spaceURLId"
                :type="order.receiver.type"
                :display-name="order.receiver.displayName" />
            </div>
          </v-list-tile-content>
        </v-list-tile>
      </v-list>
  
      <v-divider />
  
      <v-list dense class="orderProcessingDetails">
        <v-list-tile class="orderProcessingContent">
          <v-list-tile-content>Processing:</v-list-tile-content>
          <v-list-tile-content class="align-end orderProcessingActions">
            <div>
              <div v-if="!order.remainingQuantityToProcess || isError">
                <v-icon class="green--text mr-1" size="16px">fa-check-circle</v-icon>DONE
              </div>
              <template v-else-if="userData && userData.canEdit">
                <deliver-modal
                  v-if="order.remainingQuantityToProcess && (isPaid || isPartial)"
                  :product="product"
                  :order="order" />
                <refund-modal
                  v-if="order.remainingQuantityToProcess && (isPaid || isPartial)"
                  :product="product"
                  :order="order"
                  :symbol="symbol"
                  @refunded="refunded"
                  @closed="refundDialogClosed" />
                <button
                  v-if="isOrdered"
                  class="btn orderProcessingBtn"
                  @click="cancelOrder">
                  Cancel
                </button>
              </template>
              <div v-else-if="isCanceled">
                CANCELED
              </div>
              <div v-else>
                <v-icon class="orange--text mr-1" size="16px">far fa-clock</v-icon>PENDING
              </div>
            </div>
          </v-list-tile-content>
        </v-list-tile>
        <v-list-tile class="mt-1">
          <v-list-tile-content class="orderDeliveredLabel">
            <div class="no-wrap">
              Delivered:
              <v-progress-circular
                :rotate="360"
                :size="40"
                :width="5"
                :value="deliveredPercentage"
                color="teal"
                class="ml-2">
                <span class="no-wrap">
                  {{ order.deliveredQuantity }}/{{ order.quantity }}
                </span>
              </v-progress-circular>
            </div>
          </v-list-tile-content>
          <v-list-tile-content v-if="order.deliveredDate" class="align-end">
            <div :title="deliveredDateLabel" class="orderDeliveredDate ellipsis">
              {{ deliveredDateLabel }}
            </div>
          </v-list-tile-content>
        </v-list-tile>
        <v-list-tile v-if="order.refundedAmount && order.refundTransactionHash" class="mt-1">
          <v-list-tile-content class="orderRefundedLabel">
            <div class="no-wrap">
              Refunded:
              <v-icon
                v-if="order.refundTransactionStatus === 'SUCCESS'"
                title="Transaction succeeded"
                class="green--text"
                size="16">
                fa-check-circle
              </v-icon>
              <v-icon
                v-if="order.refundTransactionStatus === 'FAILED'"
                title="Transaction failed"
                class="red--text"
                size="16">
                fa-exclamation-circle
              </v-icon>
              <v-icon
                v-if="order.refundTransactionStatus === 'PENDING'"
                title="Transaction in progress"
                class="orange--text"
                size="16">
                far fa-clock
              </v-icon>
              <a
                v-if="order.refundTransactionHash"
                :href="refundTransactionLink"
                rel="nofollow"
                target="_blank">
                {{ order.refundedAmount }} {{ symbol }}
              </a>
              <template v-else>
                {{ order.refundedAmount }} {{ symbol }}
              </template>
            </div>
          </v-list-tile-content>
          <v-list-tile-content v-if="order.refundedDate" class="align-end">
            <div :title="refundedDateLabel" class="orderRefundedDate ellipsis">
              {{ refundedDateLabel }}
            </div>
          </v-list-tile-content>
        </v-list-tile>
      </v-list>
    </v-card>
  </v-hover>
  <span v-else class="hidden"></span>
</template>

<script>
import RefundModal from './RefundModal.vue';
import DeliverModal from './DeliverModal.vue';
import ProfileLink from '../ProfileLink.vue';

import {saveOrderStatus} from '../../js/PerkStoreProductOrder.js';

export default {
  components: {
    DeliverModal,
    RefundModal,
    ProfileLink,
  },
  props: {
    product: {
      type: Object,
      default: function() {
        return {};
      },
    },
    order: {
      type: Object,
      default: function() {
        return {};
      },
    },
    symbol: {
      type: String,
      default: function() {
        return "";
      },
    },
  },
  data() {
    return {
      statusList: [
        'ORDERED',
        'CANCELED',
        'ERROR',
        'PAID',
        'PARTIAL',
        'DELIVERED',
        'REFUNDED'
      ],
    };
  },
  computed: {
    transactionLink() {
      if(this.order.transactionHash) {
        if((this.order.receiver.type === 'user' && this.order.receiver.id === eXo.env.portal.userName) || (this.order.sender.type === 'user' && this.order.sender.id === eXo.env.portal.userName)) {
          return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/wallet?hash=${this.order.transactionHash}&principal=true`;
        } else if (this.order.receiver.type === 'space') {
          return `${eXo.env.portal.context}/g/:spaces:${this.order.receiver.spaceURLId}/${this.order.receiver.id}/EthereumSpaceWallet?hash=${this.order.transactionHash}&principal=true`;
        }
      }
      return '#';
    },
    refundTransactionLink() {
      if(this.order.refundTransactionHash) {
        if((this.order.receiver.type === 'user' && this.order.receiver.id === eXo.env.portal.userName) || (this.order.sender.type === 'user' && this.order.sender.id === eXo.env.portal.userName)) {
          return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/wallet?hash=${this.order.refundTransactionHash}&principal=true`;
        } else if (this.order.receiver.type === 'space') {
          return `${eXo.env.portal.context}/g/:spaces:${this.order.receiver.spaceURLId}/${this.order.receiver.id}/EthereumSpaceWallet?hash=${this.order.refundTransactionHash}&principal=true`;
        }
      }
      return '#';
    },
    orderLink() {
      return (this.order && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore?productId=${this.order.productId}&orderId=${this.order.id}`) || '#';
    },
    productLink() {
      return (this.order && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore?productId=${this.order.productId}`) || '#';
    },
    productTitle() {
      return (this.product && this.product.title) || (this.order && this.order.productTitle) || '';
    },
    userData() {
      return (this.product && this.product.userData) || {};
    },
    createdDateLabel() {
      return this.order.createdDate ? new Date(this.order.createdDate).toLocaleString() : '-';
    },
    deliveredDateLabel() {
      return this.order.deliveredDate ? new Date(this.order.deliveredDate).toLocaleString() : '-';
    },
    refundedDateLabel() {
      return this.order.refundedDate ? new Date(this.order.refundedDate).toLocaleString() : '-';
    },
    statusLabel() {
      return this.order.status;
    },
    deliveredPercentage() {
      return parseInt(((this.order.deliveredQuantity + this.order.refundedQuantity) * 100) / this.order.quantity);
    },
    statusLowerCase() {
      return this.order.status && this.order.status.toLowerCase();
    },
    isError() {
      return this.statusLowerCase === 'error';
    },
    isPaid() {
      return this.statusLowerCase === 'paid';
    },
    isDelivered() {
      return this.statusLowerCase === 'delivered';
    },
    isRefunded() {
      return this.statusLowerCase === 'refunded';
    },
    isCanceled() {
      return this.statusLowerCase === 'canceled';
    },
    isOrdered() {
      return this.statusLowerCase === 'ordered';
    },
    isPartial() {
      return this.statusLowerCase === 'partial';
    },
  },
  methods: {
    openProductDetail(event) {
      event.stopPropagation();
      event.preventDefault();

      this.$emit('display-product', this.order.productId);
    },
    cancelOrder() {
      this.order.status= 'CANCELED';
      return this.changeStatus('STATUS');
    },
    refunded(order) {
      Object.assign(this.order, order);
    },
    refundDialogClosed() {
      // We have to re-init wallet settings to the current user instead of wallet of Order receiver
      this.$emit('init-wallet');
    },
    changeStatus(modificationType) {
      console.log("this.order", this.order);

      this.$emit('loading', true);
      return saveOrderStatus(this.order, modificationType)
        .then(order => {
          this.$emit('changed', order);
          this.$forceUpdate();
        })
        .catch(e => {
          console.debug("Error saving status", e);
          this.$emit('error', e && e.message ? e.message : String(e));
        }).finally(() => this.$emit('loading', false));
    },
  }
}
</script>