<template>
  <v-card v-if="order">
    <v-card-title v-if="order.sender">
      <h4>
        <profile-link
          :id="order.sender.id"
          :technical-id="order.sender.technicalId"
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
          class="btn btn-primary mr-1"
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
            :technical-id="order.receiver.technicalId"
            :type="order.receiver.type"
            :display-name="order.receiver.displayName" />
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Ordered id:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          #{{ order.id }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Order transaction:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          <a
            v-if="order.transactionHash"
            :href="order.transactionLink"
            rel="nofollow"
            target="_blank">
            link
          </a>
          <template v-else>-</template>
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Ordered quantity:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ order.quantity || '-' }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile :class="edit && 'grey lighten-3'">
        <v-list-tile-content>Order status:</v-list-tile-content>
        <v-list-tile-content v-if="edit" class="align-end">
          <select v-model="status" class="small">
            <option v-for="option in statusList" :key="option">
              {{ option }}
            </option>
          </select>
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
            class="text-xs-right small">
        </v-list-tile-content>
        <v-list-tile-content v-else class="align-end">
          {{ order.deliveredQuantity || '-' }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile :class="edit && 'grey lighten-3'">
        <v-list-tile-content>Order refunded quantity:</v-list-tile-content>
        <v-list-tile-content v-if="edit" class="align-end">
          <input
            v-model.number="refunded"
            type="text"
            name="OrderRefundedQuantity"
            class="text-xs-right small">
        </v-list-tile-content>
        <v-list-tile-content v-else class="align-end">
          {{ order.refundedQuantity || '-' }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile>
        <v-list-tile-content>Remaining quantity to treat:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          <v-badge
            v-if="order.remainingQuantityToTreat"
            color="red"
            left>
            <span slot="badge">{{ order.remainingQuantityToTreat }}</span>
            <span></span>
          </v-badge>
          <template v-else>-</template>
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
        'ordered',
        'canceled',
        'payed',
        'delivered',
        'refunded'
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
      this.changeOrderStatus(this.status, this.delivered, this.refunded).then(() => this.edit = false);
    },
    changeOrderStatus(newStatus, delivered, refunded) {
      this.$emit('loading', true);
      return saveOrderStatus(this.order.id, newStatus, delivered, refunded)
        .then(order => this.$emit('changed', order))
        .then(() => this.$forceUpdate())
        .catch(e => this.$emit('error', e))
        .finally(() => this.$emit('loading', false));
    }
  }
}
</script>