<template>
  <v-dialog
    v-model="dialog"
    :disabled="!walletAddonInstalled"
    content-class="uiPopup with-overflow"
    class="refundProductModal"
    width="300px"
    max-width="100vw"
    persistent
    @keydown.esc="close">
    <button
      slot="activator"
      :disabled="!walletAddonInstalled"
      class="btn orderProcessingBtn ml-1">
      Refund
    </button>

    <v-card class="elevation-12">
      <div class="popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="close"></a>
        <span class="PopupTitle popupTitle ellipsis">
          Refund order #{{ order && order.id }}
        </span>
      </div>
      <v-card-text>
        <div v-if="warning && !loading && !walletLoading" class="alert alert-warning v-content">
          <i class="uiIconWarning"></i>
          {{ warning }}
        </div>
        <div v-if="error && !loading" class="alert alert-error v-content">
          <i class="uiIconError"></i>
          {{ error }}
        </div>
        <v-form ref="form">
          <v-text-field
            v-model.number="quantity"
            :disabled="loading"
            :label="quantityInputLabel"
            :rules="requiredNumberRule"
            append-icon="fa-plus"
            prepend-inner-icon="fa-minus"
            class="text-xs-center"
            name="quantity"
            placeholder="Select a quantity to refund"
            required
            @click:prepend-inner="decrementQuantity"
            @click:append="incrementQuantity" />
          <v-text-field
            v-model.number="amount"
            :disabled="loading"
            :label="amountInputLabel"
            :rules="requiredAmountRule"
            name="amount"
            class="text-xs-center"
            placeholder="Enter the amount to refund"
            required />
          <v-text-field
            v-if="needPassword"
            v-model="walletPassword"
            :append-icon="walletPasswordShow ? 'visibility_off' : 'visibility'"
            :type="walletPasswordShow ? 'text' : 'password'"
            :disabled="loading"
            :rules="requiredRule"
            name="walletPassword"
            label="Wallet password"
            placeholder="Enter your wallet password"
            counter
            required
            autocomplete="current-passord"
            @click:append="walletPasswordShow = !walletPasswordShow" />
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          :disabled="disableRefundButton"
          :loading="loading || walletLoading"
          class="primary mr-1"
          @click="refundProduct">
          Refund
        </v-btn>
        <button
          class="btn"
          :disabled="loading || walletLoading"
          @click="close">
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
      walletAddonInstalled: false,
      walletLoading: false,
      walletEnabled: false,
      needPassword: false,
      loading: false,
      quantity: null,
      amount: null,
      walletPassword: '',
      walletPasswordShow: false,
      warning: null,
      error: null,
      requiredRule: [(v) => !!v || 'Required field'],
      requiredNumberRule: [
        (v) => !!v || 'Required field',
        (v) => this.isPositiveNumber(v, true) || 'Invalid positive number',
        (v) => !this.order || this.quantity <= Number(this.order.remainingQuantityToProcess) || `Orders quantity must be less than ${this.order.remainingQuantityToProcess}`,
      ],
      requiredAmountRule: [
        (v) => !!v || 'Required field',
        (v) => this.isPositiveNumber(v) || 'Invalid positive number',
        (v) => v <= this.maxAmount || `Amount must be less than ${this.maxAmount}`,
      ],
    };
  },
  computed: {
    disableRefundButton() {
      return this.walletLoading || !this.walletEnabled || !this.isPositiveNumber(this.amount) || !this.isPositiveNumber(this.quantity, true) || this.quantity > this.order.remainingQuantityToProcess || this.amount > this.maxAmount;
    },
    amountInputLabel() {
      return `Amount in ${this.symbol}`;
    },
    quantityInputLabel() {
      return this.order && `Quantity (max: ${this.order.remainingQuantityToProcess})`;
    },
    maxAmount() {
      return (this.order && this.product && this.order.remainingQuantityToProcess * this.product.price) || 0;
    },
    productTitle() {
      return (this.product && this.product.title)  || (this.order.productTitle) || '';
    },
  },
  watch: {
    quantity() {
      if(this.quantity) {
        this.amount = this.quantity * ((this.product && this.product.price) || 0);
      } else {
        this.amount = 0;
      }
    },
    error() {
      if(this.error) {
        this.loading = false;
      }
    },
    dialog() {
      if (this.dialog) {
        this.walletLoading = true;
        this.walletEnabled = false;
        document.dispatchEvent(new CustomEvent('exo-wallet-init', {'detail' : {sender: this.order.receiver}}));
        this.quantity = this.order && this.order.remainingQuantityToProcess;
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
    this.walletAddonInstalled = window.walletAddonInstalled;

    document.addEventListener('exo-wallet-send-tokens-pending', this.pendingTransaction);
    document.addEventListener('exo-wallet-send-tokens-error', this.errorTransaction);
    document.addEventListener('exo-wallet-init-result', this.walletInitialized);
  },
  methods: {
    close() {
      if(!this.walletLoading) {
        this.dialog = false;
      }
    },
    isPositiveNumber(value, isInt) {
      return this.product && value && !isNaN(value) && value > 0 && Number.isFinite(value) && (!isInt || this.product.allowFraction || Number.isSafeInteger(value));
    },
    walletInitialized(event) {
      this.walletLoading = false;
      const result = event && event.detail;
      if(!result || result.error) {
        this.warning = `${result && result.error ? (`${  result.error}`) : 'Wallet seems not configured properly'}`;
        this.walletEnabled = false;
      } else {
        this.walletEnabled = true;
        this.needPassword = result.needPassword;
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
      // Check if the event is triggered for current window
      if(this.dialog && this.loading) {
        return saveOrderStatus({
          id: this.order.id,
          productId: this.order.productId,
          refundTransactionHash: pendingTransaction.hash,
          refundedQuantity: Number(this.quantity),
          refundedAmount: Number(this.amount),
        }, 'REFUNDED_QUANTITY')
          .then((order) => {
            this.dialog = false;
            this.loading = false;
            this.$emit('refunded', order);
          })
          .catch(e => {
            console.debug("Error saving refund order", e);
            this.loading = false;
            this.error = e && e.message ? e.message : String(e);
          });
      }
    },
    errorTransaction(event) {
      this.loading = false;
      this.error = event.detail;
    },
    refundProduct(event) {
      this.error = null;

      event.preventDefault();
      event.stopPropagation();

      if(!this.$refs.form.validate()) {
        return;
      }

      if (!this.isPositiveNumber(this.quantity, true)) {
        this.error = 'Invalid quantity';
        return;
      }

      if (!this.isPositiveNumber(this.amount)) {
        this.error = 'Invalid amount';
        return;
      }

      if (this.quantity > this.order.remainingQuantityToProcess) {
        this.error = `You can't refund more than ${this.order.remainingQuantityToProcess} as quantity`;
        return;
      }

      if (this.amount > (this.quantity * this.product.price)) {
        this.error = 'Amount to send is greater than quantity * price';
        return;
      }

      if (!this.order.sender) {
        this.error = `Order doesn't have a sender wallet`;
        return;
      }

      this.loading = true;

      const message = `Refunded "${this.product.title}": ${this.quantity} x ${this.product.price} ${this.symbol ? this.symbol : ''}`;

      // simulate saving order before sending transaction to blockchain
      document.dispatchEvent(new CustomEvent('exo-wallet-send-tokens', {'detail' : {
        amount: this.amount,
        sender: this.order.receiver,
        receiver: this.order.sender,
        password: this.walletPassword,
        label: message,
        message: message,
      }}));
    },
  },
};
</script>
