<template>
  <v-card v-if="order">
    <v-card-title v-if="order.sender">
      <h4><strong>#{{ order.id }}</strong></h4>
      <v-spacer />
      <template v-if="userData.canEdit">
        <select
          v-model="order.status"
          class="small mt-1 mb-1 mr-2"
          @change="changeStatus">
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
          <profile-link
            :id="order.sender.id"
            :space-id="order.sender.spaceId"
            :url-id="order.sender.spaceURLId"
            :type="order.sender.type"
            :display-name="order.sender.displayName"
            display-avatar />
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Date:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ createdDateLabel }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Items:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          <div>
            {{ order.quantity }} x 
            <a href="javascript:void(0);" @click="$emit('display-product', product)">
              {{ productTitle }}
            </a>
          </div>
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>
          Payment:
          <v-icon
            v-if="transactionSuccess"
            title="Transaction succeeded"
            class="green">
            fa-check-circle
          </v-icon>
          <v-icon
            v-else-if="transactionFailed"
            title="Transaction failed"
            class="red">
            fa-exclamation-circle
          </v-icon>
          <v-progress-circular
            v-else-if="transactionLoading"
            color="primary"
            indeterminate
            size="20" />
        </v-list-tile-content>
        <v-list-tile-content class="align-end">
          <div>
            <a
              v-if="order.transactionLink"
              :href="order.transactionLink"
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

    <v-list dense class="orderProcessingContent">
      <v-list-tile>
        <v-list-tile-content>Processing:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          <div class="no-wrap">
            <div v-if="!order.remainingQuantityToProcess || isError">
              <v-icon class="green">fa-check-circle</v-icon>DONE
            </div>
            <button
              v-if="order.remainingQuantityToProcess && (isPaid || isPartial)"
              class="btn btn-primary orderProcessingBtn mr-1"
              @click="$emit('open-deliver', order)">
              Deliver
            </button>
            <button
              v-if="order.remainingQuantityToProcess && (isPaid || isPartial)"
              class="btn orderProcessingBtn mr-1"
              @click="$emit('open-refund', order)">
              {{ refundButtonLabel }}
            </button>
            <button
              v-if="isOrdered"
              class="btn orderProcessingBtn mr-1"
              @click="edit = false">
              Cancel
            </button>
          </div>
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>
          <div>
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
          {{ deliveredDateLabel }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile v-if="order.refundedQuantity && order.refundTransactionLink">
        <v-list-tile-content>
          <div>
            Refunded:
            <a
              v-if="order.refundTransactionLink"
              :href="order.refundTransactionLink"
              rel="nofollow"
              target="_blank">
              {{ order.refundedQuantity }} {{ symbol }}
            </a>
          </div>
        </v-list-tile-content>
        <v-list-tile-content v-if="order.refundedDate" class="align-end">
          {{ refundedDateLabel }}
        </v-list-tile-content>
      </v-list-tile>
    </v-list>
  </v-card>
  <span v-else class="hidden"></span>
</template>

<script>
import ProfileLink from '../ProfileLink.vue';

import {saveOrderStatus} from '../../js/PerkStoreProductOrder.js';

export default {
  components: {
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
      transactionSuccess: false,
      transactionError: false,
      transactionLoading: false,
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
    refundButtonLabel() {
      return this.order.deliveredQuantity > 0 ? 'Refund' : 'Refund all';
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
    changeStatus() {
      this.$emit('loading', true);
      return saveOrderStatus(this.order.id, this.order.productId, this.order.status, this.order.delivered, this.order.refunded)
        .then(order => {
          this.$emit('changed', order);
          this.$forceUpdate();
        })
        .catch(e => {
          console.debug("Error saving status", e);
          this.$emit('error', e && e.message ? e.message : String(e));
        }).finally(() => this.$emit('loading', false));
    }
  }
}
</script>