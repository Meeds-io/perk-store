<template>
  <v-dialog
    v-model="dialog"
    content-class="uiPopup with-overflow"
    class="buyProductModal"
    width="300px"
    max-width="100vw"
    persistent
    @keydown.esc="close">
    <v-card class="elevation-12">
      <div class="popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="close"></a> <span class="PopupTitle popupTitle">
            Buy {{ productTitle }}
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
          placeholder="Select a quantity to buy"
          @click:prepend-inner="decrementQuantity"
          @click:append="incrementQuantity" />
        <v-text-field
          v-if="needPassword"
          v-model="walletPassword"
          :append-icon="walletPasswordShow ? 'visibility_off' : 'visibility'"
          :type="walletPasswordShow ? 'text' : 'password'"
          :disabled="loading"
          name="walletPassword"
          label="Wallet password"
          placeholder="Enter your wallet password"
          counter
          autocomplete="current-passord"
          @click:append="walletPasswordShow = !walletPasswordShow" />
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          :disabled="disablePayButton"
          :loading="loading"
          class="primary mr-1"
          @click="payProduct">
          Pay
        </v-btn>
        <button
          class="btn"
          :disabled="loading"
          @click="close">
          Close
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import {saveOrder} from '../../js/PerkStoreProductOrder.js';

export default {
  props: {
    product: {
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
    needPassword: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
  },
  data() {
    return {
      dialog: false,
      loading: false,
      quantity: null,
      walletPassword: '',
      walletPasswordShow: false,
      error: null,
    };
  },
  computed: {
    disablePayButton() {
      return this.maxOrdersReached || !this.product || !this.quantity || (!this.product.unlimited && this.quantity > this.maxQuantity);
    },
    available() {
      if(this.product && !this.product.unlimited) {
        const available = this.product.totalSupply - this.product.purshased;
        return available > 0 ? available : 0;
      } else {
        return 0;
      }
    },
    maxOrderPerUserQuantity() {
      if(this.product && !this.product.unlimited) {
        const quantity = this.product.maxOrdersPerUser - ((this.product.userOrders && this.product.userOrders.purchasedInCurrentPeriod) || 0);
        return quantity > 0 ? quantity : 0;
      } else {
        return 0;
      }
    },
    maxQuantity() {
      return Math.min(this.available, this.maxOrderPerUserQuantity);
    },
    maxOrdersReached() {
      return this.product && !this.product.unlimited && !this.maxQuantity;
    },
    quantityInputLabel() {
      if(this.product && !this.product.unlimited && !this.maxOrdersReached) {
        return `Quantity (max: ${this.maxQuantity})`;
      } else {
        return 'Quantity';
      }
    },
    productTitle() {
      return this.product && this.product.title ?  this.product.title : '';
    }
  },
  watch: {
    error() {
      if(this.error) {
        this.loading = false;
      }
    },
    dialog() {
      if (this.dialog) {
        this.quantity = null;
        this.warning = null;
        this.error = null;
        this.loading = false;
        this.walletPassword = '';
        this.walletPasswordShow = false;
      } else {
        this.$emit('closed');
      }
    },
  },
  created() {
    document.addEventListener('exo-wallet-send-tokens-pending', this.pendingTransaction);
    document.addEventListener('exo-wallet-send-tokens-error', this.errorTransaction);
  },
  methods: {
    open() {
      this.dialog = true;
    },
    close() {
      if(!this.loading) {
        this.dialog = false;
      }
    },
    incrementQuantity() {
      this.quantity = this.quantity && (this.quantity + 1) > 0 ? this.quantity + 1 : 1;
      this.quantity = Math.floor(this.quantity);
    },
    decrementQuantity() {
      this.quantity = this.quantity && (this.quantity - 1) > 0 ? this.quantity - 1 : 0;
      this.quantity = Math.floor(this.quantity);
    },
    pendingTransaction(event) {
      const pendingTransaction = event.detail;
      return saveOrder({
        productId: this.product.id,
        transactionHash: pendingTransaction.hash,
        amount: pendingTransaction.amount,
        quantity: this.quantity,
        receiver: this.product.receiverMarchand,
      })
        .then((order) => {
          this.dialog = false;
          this.loading = false;
          this.$emit('ordered', order);
        })
        .catch(e => {
          this.loading = false;
          this.error = `Payment is in progress but an error occurred while processing order: ${e}`;
        });
    },
    errorTransaction(event) {
      this.loading = false;

      this.error = event.detail;
    },
    payProduct() {
      this.error = null;
      this.loading = true;

      if (!this.quantity || isNaN(parseFloat(this.quantity)) || !Number.isFinite(this.quantity) || !Number.isInteger(this.quantity) || this.quantity <= 0) {
        this.error = 'Invalid quantity';
        return;
      }

      if (!this.product.unlimited && this.quantity > this.maxQuantity) {
        this.error = `You can't buy more than ${this.maxQuantity} as quantity`;
        return;
      }

      if (!this.product.receiverMarchand) {
        this.error = `Product doesn't have a receiver wallet yet`;
        return;
      }

      if (!this.product.price) {
        this.error = `Product doesn't have a price yet`;
        return;
      }

      const amount = this.quantity * this.product.price;
      const message = `Purchased "${this.product.title}": ${this.quantity} x ${this.product.price}${this.symbol}`;

      document.dispatchEvent(new CustomEvent('exo-wallet-send-tokens', {'detail' : {
        amount: amount,
        receiver: this.product.receiverMarchand,
        password: this.walletPassword,
        label: message,
        message: message,
      }}));
    },
  },
};
</script>
