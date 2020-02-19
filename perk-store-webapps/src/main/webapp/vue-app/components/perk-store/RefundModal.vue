<template>
  <v-dialog
    v-model="dialog"
    content-class="uiPopup with-overflow"
    class="refundProductModal"
    width="300px"
    max-width="100vw"
    persistent
    eager
    @keydown.esc="close">
    <v-card class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="close"></a>
        <span class="ignore-vuetify-classes PopupTitle popupTitle text-truncate">
          {{ $t('exoplatform.perkstore.title.refundOrderModal', {0: order && order.id}) }}
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
        <v-form ref="form" v-model="validForm">
          <v-text-field
            v-model.number="quantity"
            :disabled="loading"
            :label="quantityInputLabel"
            :placeholder="$t('exoplatform.perkstore.label.refundQuantityPlaceholder')"
            :rules="requiredNumberRule"
            append-icon="fa-plus"
            prepend-inner-icon="fa-minus"
            class="text-center"
            name="quantity"
            required
            @click:prepend-inner="decrementQuantity"
            @click:append="incrementQuantity" />
          <v-text-field
            v-if="dialog"
            v-model.number="amount"
            :disabled="loading"
            :label="$t('exoplatform.perkstore.label.amount')"
            :placeholder="$t('exoplatform.perkstore.label.refundAmountPlaceholder')"
            :rules="requiredAmountRule"
            name="amount"
            class="text-center"
            autofocus
            required />
          <v-text-field
            v-if="needPassword"
            v-model="walletPassword"
            :append-icon="walletPasswordShow ? 'visibility_off' : 'visibility'"
            :type="walletPasswordShow ? 'text' : 'password'"
            :disabled="loading"
            :rules="requiredRule"
            :label="$t('exoplatform.perkstore.label.walletPassword')"
            :placeholder="$t('exoplatform.perkstore.label.walletPasswordPlaceholder')"
            name="walletPassword"
            counter
            required
            validate-on-blur
            autocomplete="current-passord"
            @click:append="walletPasswordShow = !walletPasswordShow" />
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <button
          :disabled="!validForm || loading || walletLoading"
          class="ignore-vuetify-classes btn btn-primary mr-1"
          @click="refundProduct">
          {{ $t('exoplatform.perkstore.button.refund') }}
        </button>
        <button
          class="ignore-vuetify-classes btn"
          :disabled="loading || walletLoading"
          @click="close">
          {{ $t('exoplatform.perkstore.button.close') }}
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import {toFixed, saveOrderStatus} from '../../js/PerkStoreProductOrder.js';

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
      validForm: false,
      needPassword: false,
      loading: false,
      quantity: null,
      amount: null,
      walletPassword: '',
      walletPasswordShow: false,
      warning: null,
      error: null,
      requiredRule: [(v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField')],
      requiredNumberRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => this.isPositiveNumber(v, true) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
        (v) => !this.order || this.quantity <= Number(this.order.remainingQuantityToProcess) || this.$t('exoplatform.perkstore.warning.maxQuantityReached', {0: this.remainingQuantityToProcess}),
      ],
      requiredAmountRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => this.isPositiveNumber(v, false) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
        (v) => v <= this.maxAmount || this.$t('exoplatform.perkstore.warning.maxAmountReached', {0: this.maxAmount}),
      ],
    };
  },
  computed: {
    amountInputLabel() {
      return this.order && this.$t('exoplatform.perkstore.label.amountWithMax', {0: this.maxAmount});
    },
    quantityInputLabel() {
      return this.order && this.$t('exoplatform.perkstore.label.quantityWithMax', {0: this.order.remainingQuantityToProcess});
    },
    maxAmount() {
      return (this.order && this.product && toFixed(this.order.remainingQuantityToProcess * this.product.price)) || 0;
    },
    productTitle() {
      return (this.product && this.product.title)  || (this.order.productTitle) || '';
    },
  },
  watch: {
    quantity() {
      if(this.quantity) {
        this.amount = (this.product && this.product.price && this.quantity && toFixed(this.quantity * this.product.price)) || 0;
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
    open() {
      this.dialog = true;
    },
    close() {
      if(!this.walletLoading) {
        this.dialog = false;
      }
    },
    isPositiveNumber(value, isInt) {
      value = Number(value);
      return this.product && value && value > 0 && Number.isFinite(value) && (!isInt || Number.isSafeInteger(value));
    },
    walletInitialized(event) {
      this.walletLoading = false;
      const result = event && event.detail;
      if(!result || result.error) {
        this.warning = `${result && result.error ? (`${  result.error}`) : this.$t('exoplatform.perkstore.warning.walletNotConfiguredProperly')}`;
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
        this.$emit('refunding');

        return saveOrderStatus({
          id: this.order.id,
          productId: this.order.productId,
          refundTransactionHash: pendingTransaction.hash,
          refundedQuantity: Number(this.quantity),
          refundedAmount: Number(this.amount),
        }, 'REFUNDED_QUANTITY')
          .then((order) => {
            this.$emit('refunded', order);
            this.dialog = false;
            this.loading = false;
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
        return false;
      }

      if (!this.isPositiveNumber(this.quantity, true)) {
        this.error = this.$t('exoplatform.perkstore.warning.invalidQuantity');
        return;
      }

      if (!this.isPositiveNumber(this.amount)) {
        this.error = this.$t('exoplatform.perkstore.warning.invalidAmount');
        return;
      }

      if (this.quantity > this.order.remainingQuantityToProcess) {
        this.error = this.$t('exoplatform.perkstore.warning.cantRefundMoreThanQuantity', {0: this.order.remainingQuantityToProcess});
        return;
      }

      if (this.amount > (this.quantity * this.product.price)) {
        this.error = this.$t('exoplatform.perkstore.warning.amountGreaterThanRealPrice');
        return;
      }

      if (!this.order.sender) {
        this.error = this.$t('exoplatform.perkstore.warning.orderWithoutSenderWallet');
        return;
      }

      this.loading = true;

      const message = this.$t('exoplatform.perkstore.info.refundedQuantityAndAmount', {
        0: this.product.title,
        1: this.quantity,
        2: this.product.price,
        3: this.symbol || '',
      });

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
