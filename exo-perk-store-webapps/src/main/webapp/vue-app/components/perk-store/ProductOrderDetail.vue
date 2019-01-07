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
      <v-list-tile v-if="order.id">
        <v-list-tile-content>Ordered id:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          #{{ order.id }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile v-if="order.quantity">
        <v-list-tile-content>Ordered quantity:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ order.quantity }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile v-if="order.amount">
        <v-list-tile-content>Order price:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ order.amount }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile v-if="order.status">
        <v-list-tile-content>Order status:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ statusLabel }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile v-if="order.canceledQuantity">
        <v-list-tile-content>Order canceled quantity:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ order.canceledQuantity }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile v-if="order.refundedQuantity">
        <v-list-tile-content>Order refunded quantity:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ order.refundedQuantity }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile v-if="order.createdDate">
        <v-list-tile-content>Date of order:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ createdDateLabel }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile v-if="order.deliveredDate">
        <v-list-tile-content>Date of delivery:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ deliveredDateLabel }}
        </v-list-tile-content>
      </v-list-tile>
      <v-list-tile v-if="order.refundedDate">
        <v-list-tile-content>Date of refund:</v-list-tile-content>
        <v-list-tile-content class="align-end">
          {{ refundedDateLabel }}
        </v-list-tile-content>
      </v-list-tile>
    </v-list>
    <v-card-actions>
      <v-spacer />
      <button
        v-if="status === 'ordered'"
        class="btn btn-primary mr-1"
        @click="changeOrderStatus('canceled')">
        Cancel order
      </button>
      <button
        v-if="status === 'payed'"
        class="btn btn-primary mr-1"
        @click="changeOrderStatus('delivered')">
        Set delivered
      </button>
      <button
        v-if="status === 'payed'"
        class="btn mr-1"
        @click="changeOrderStatus('refunded')">
        Set refunded
      </button>
      <button
        v-if="status === 'delivered'"
        class="btn btn-primary mr-1"
        @click="changeOrderStatus('payed')">
        Cancel delivery
      </button>
      <button
        v-if="status === 'refunded'"
        class="btn btn-primary mr-1"
        @click="changeOrderStatus('payed')">
        Cancel refund
      </button>
      <v-dialog
        v-if="displayRefundPartiallyButton"
        id="refundModal"
        v-model="refundModalDialog"
        content-class="uiPopup with-overflow"
        width="500px"
        max-width="100vw"
        persistent
        @keydown.esc="refundModalDialog = false">
        <button
          slot="activator"
          class="btn mr-1">
          Partially refund
        </button>
        <v-card class="elevation-12">
          <div class="popupHeader ClearFix">
            <a
              class="uiIconClose pull-right"
              aria-hidden="true"
              @click="refundModalDialog = false"></a> <span class="PopupTitle popupTitle">
                Refund order #{{ order.id }}
              </span>
          </div>
          <v-card-title v-if="refundError" class="text-xs-center">
            <div class="alert alert-error">
              <i class="uiIconError"></i> {{ refundError }}
            </div>
          </v-card-title>
          <v-card-text>
            <v-text-field
              v-model.number="toRefund"
              name="toRefund"
              label="To refund"
              placeholder="Set a quantity of product to refund" />
            <v-text-field
              v-model.number="refunded"
              name="refunded"
              label="Refunded"
              placeholder="Set a quantity of refunded product" />
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <button
              class="btn btn-primary mr-1"
              @click="saveRefund()">
              Save
            </button>
            <button
              class="btn mr-1"
              @click="refundModalDialog = false">
              Close
            </button>
            <v-spacer />
          </v-card-actions>
        </v-card>
      </v-dialog>
      <v-spacer />
    </v-card-actions>
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
    order: {
      type: Object,
      default: function() {
        return {};
      },
    },
  },
  data() {
    return {
      refundError: null,
      toRefund: null,
      refunded: null,
      refundModalDialog: false,
    };
  },
  computed: {
    displayRefundPartiallyButton() {
      return this.status === 'payed' || (this.order.toRefundQuantity && (this.status === 'delivered' || this.status === 'refunded'));
    },
    status() {
      return this.order.status;
    },
    createdDateLabel() {
      return new Date(this.order.createdDate);
    },
    deliveredDateLabel() {
      return new Date(this.order.deliveredDate);
    },
    refundedDateLabel() {
      return new Date(this.order.refundedDate);
    },
    statusLabel() {
      return this.order.status;
    }
  },
  watch: {
    refundModalDialog() {
      if(this.refundModalDialog) {
        this.toRefund = this.order.toRefundQuantity || null;
        this.refunded = this.order.refundedQuantity || null;
      }
    }
  },
  methods: {
    saveRefund() {
      this.refundError = null;
      if(!$.isNumeric(this.toRefund) || $.isNumeric(this.refunded)) {
        this.refundError = 'Please enter a numeric input';
        return;
      }
      if(Number(this.toRefund) + Number(this.refunded) > this.order.quantity) {
        this.refundError = 'The sum of items is more than the ordered quantity';
        return;
      }
      return this.changeOrderStatus(null, this.toRefund, this.refunded).then(() => this.refundModalDialog = false);
    },
    changeOrderStatus(newStatus, toRefund, refunded) {
      this.$emit('loading', true);
      return saveOrderStatus(this.order.id, newStatus, toRefund, refunded)
        .then(order => this.$emit('changed', order))
        .then(order => this.$forceUpdate())
        .catch(e => this.$emit('error', e))
        .finally(() => this.$emit('loading', false));
    }
  }
}
</script>