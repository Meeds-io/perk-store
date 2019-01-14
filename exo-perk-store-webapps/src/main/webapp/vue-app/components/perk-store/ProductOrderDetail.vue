<template>
  <v-card v-if="order">
    <v-card-title v-if="order.sender">
      <h4>
        <profile-link
          :id="order.sender.id"
          :space-id="order.sender.spaceId"
          :url-id="order.sender.spaceURLId"
          :type="order.sender.type"
          :display-name="order.sender.displayName" />
      </h4>
      <v-spacer />
      <template v-if="product.canEdit">
        <template v-if="edit">
          <button
            class="btn btn-primary mr-1"
            @click="saveOrder">
            Save
          </button>
          <button
            class="btn mr-1"
            @click="edit = false">
            Cancel
          </button>
        </template>
        <button
          v-else
          class="btn mr-1"
          @click="edit = true">
          Edit
        </button>
      </template>
    </v-card-title>
    <v-divider />
    <v-list dense>
      <v-list-tile>
        <v-list-tile-content>Receiver:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          <profile-link
            :id="order.receiver.id"
            :space-id="order.receiver.spaceId"
            :url-id="order.receiver.spaceURLId"
            :type="order.receiver.type"
            :display-name="order.receiver.displayName" />
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Ordered id:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          <a
            v-if="order.transactionLink"
            :href="order.transactionLink"
            rel="nofollow"
            target="_blank">
            #{{ order.id }}
          </a>
          <template v-else>#{{ order.id }}</template>
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile :class="edit && 'grey lighten-3'">
        <v-list-tile-content>Order status:</v-list-tile-content>
        <v-list-tile-content v-if="edit" class="align-end">
          <select v-model="status" class="small mt-1 mb-1">
            <option v-for="option in statusList" :key="option">
              {{ option }}
            </option>
          </select>
        </v-list-tile-content>
        <v-list-tile-content v-else-if="order.status === 'ERROR'" class="align-end">
          <v-icon :title="order.error || `Transaction ${order.hash || ''} failed`" color="orange">error</v-icon>
        </v-list-tile-content>
        <v-list-tile-content v-else class="align-end">
          {{ statusLabel }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile :class="edit && 'grey lighten-3'">
        <v-list-tile-content>Order delivered quantity:</v-list-tile-content>
        <v-list-tile-content v-if="edit" class="align-end">
          <input
            v-model.number="delivered"
            type="text"
            name="OrderDeliveredQuantity"
            class="text-xs-right small mt-1 mb-1">
        </v-list-tile-content>
        <v-list-tile-content v-else class="orderQuantityBadgeParent">
          <span
            :class="order.remainingQuantityToProcess ? 'red' : ''"
            class="orderQuantityBadge">
            {{ order.deliveredQuantity }}
          </span>
          <span> / {{ order.quantity }}</span>
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile :class="edit && 'grey lighten-3'">
        <v-list-tile-content>Order refunded quantity:</v-list-tile-content>
        <v-list-tile-content v-if="edit" class="align-end">
          <input
            v-model.number="refunded"
            type="text"
            name="OrderRefundedQuantity"
            class="text-xs-right small mt-1 mb-1">
        </v-list-tile-content>
        <v-list-tile-content v-else class="orderQuantityBadgeParent">
          <span
            :class="order.remainingQuantityToProcess ? 'red' : ''"
            class="orderQuantityBadge">
            {{ order.refundedQuantity }}
          </span>
          <span> / {{ order.quantity }}</span>
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Remaining quantity to process:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ order.remainingQuantityToProcess || '-' }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Date of order:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ createdDateLabel }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Date of delivery:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ deliveredDateLabel }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Date of refund:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ refundedDateLabel }}
        </v-list-tile-content>
      </v-list-tile>
    </v-list>
  </v-card>
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
  },
  data() {
    return {
      edit: false,
      status: null,
      delivered: null,
      refunded: null,
      statusList: [
        'ORDERED',
        'CANCELED',
        'ERROR',
        'PAYED',
        'DELIVERED',
        'REFUNDED'
      ],
    };
  },
  computed: {
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
    }
  },
  watch: {
    edit() {
      if(this.edit) {
        this.status = this.order.status;
        this.delivered = this.order.deliveredQuantity;
        this.refunded = this.order.refundedQuantity;
      }
    }
  },
  methods: {
    saveOrder() {
      if(this.order.quantity < this.delivered + this.refunded) {
        this.$emit('error', 'Product quantity is less than entered values for (delivered + refunded)');
        return;
      }

      this.$emit('loading', true);
      return saveOrderStatus(this.order.id, this.order.productId, this.status, this.delivered, this.refunded)
        .then(order => {
          this.edit = false;
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