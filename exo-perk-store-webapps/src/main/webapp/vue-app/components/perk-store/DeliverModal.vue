<template>
  <v-dialog
    v-model="dialog"
    content-class="uiPopup with-overflow"
    class="deliverProductModal"
    width="300px"
    max-width="100vw"
    persistent
    @keydown.esc="dialog = false">
    <button slot="activator" class="btn btn-primary orderProcessingBtn ml-1">Deliver</button>

    <v-card class="elevation-12">
      <div class="popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="dialog = false"></a>
        <span class="PopupTitle popupTitle ellipsis">
          Deliver order #{{ order && order.id }}
        </span>
      </div>
      <v-card-text>
        <div v-if="error && !loading" class="alert alert-error v-content">
          <i class="uiIconError"></i>
          {{ error }}
        </div>
        <v-text-field
          v-model.number="quantity"
          :disabled="loading"
          :label="quantityInputLabel"
          append-icon="fa-plus"
          prepend-inner-icon="fa-minus"
          class="text-xs-center"
          name="quantity"
          placeholder="Set delivered quantity"
          required
          @click:prepend-inner="decrementQuantity"
          @click:append="incrementQuantity" />
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          :disabled="disableDeliverButton"
          :loading="loading"
          class="primary mr-1"
          @click="deliverProduct">
          Save
        </v-btn>
        <button
          class="btn"
          :disabled="loading"
          @click="dialog = false">
          Close
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import {saveOrderStatus} from '../../js/PerkStoreProductOrder.js';

export default {
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
        return null;
      },
    },
  },
  data() {
    return {
      dialog: false,
      loading: false,
      quantity: null,
      error: null,
    };
  },
  computed: {
    disableDeliverButton() {
      return !this.order || !this.isPositiveNumber(this.quantity) || this.quantity > this.order.remainingQuantityToProcess;
    },
    quantityInputLabel() {
      return this.order && `Quantity (max: ${this.order.remainingQuantityToProcess})`;
    },
  },
  watch: {
    error() {
      if(this.error) {
        this.loading = false;
      }
    },
    dialog() {
      if (this.dialog) {
        this.quantity = this.order && this.order.remainingQuantityToProcess;
        this.warning = null;
        this.error = null;
        this.loading = false;
      } else {
        this.$emit('closed');
      }
    },
  },
  methods: {
    incrementQuantity() {
      this.quantity = this.quantity && (this.quantity + 1) > 0 ? this.quantity + 1 : 1;
      this.quantity = Math.floor(this.quantity);
    },
    decrementQuantity() {
      this.quantity = this.quantity && (this.quantity - 1) > 0 ? this.quantity - 1 : 0;
      this.quantity = Math.floor(this.quantity);
    },
    isPositiveNumber(value) {
      return value && !isNaN(value) && value > 0 && Number.isFinite(value);
    },
    deliverProduct() {
      this.error = null;

      if (!this.isPositiveNumber(this.quantity) || (!this.product.allowFraction && !Number.isSafeInteger(this.quantity))) {
        this.error = 'Invalid quantity';
        return;
      }

      if (this.quantity > this.order.remainingQuantityToProcess) {
        this.error = `You can't deliver more than ${this.order.remainingQuantityToProcess} as quantity`;
        return;
      }

      if (!this.order.sender) {
        this.error = `Order doesn't have a sender wallet`;
        return;
      }

      this.loading = true;

      return saveOrderStatus({
        id: this.order.id,
        productId: this.order.productId,
        deliveredQuantity: Number(this.quantity),
      }, 'DELIVERED_QUANTITY')
        .then((order) => {
          this.dialog = false;
          this.loading = false;
          this.$emit('delivered', order);
        })
        .catch(e => {
          console.debug("Error saving delivered quantity", e);
          this.loading = false;
          this.error = e && e.message ? e.message : String(e);
        });
    },
  },
};
</script>
