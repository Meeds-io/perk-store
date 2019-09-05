<template>
  <div>
    <v-card-text class="py-0">
      <div v-if="error && !loading" class="alert alert-error v-content">
        <i class="uiIconError"></i>
        {{ error }}
      </div>
      <v-form ref="form">
        <v-text-field
          v-model.number="quantity"
          :disabled="loading"
          :rules="requiredNumberRule"
          :label="quantityInputLabel"
          :placeholder="$t('exoplatform.perkstore.label.quantityPlaceholder')"
          append-icon="fa-plus"
          prepend-inner-icon="fa-minus"
          class="text-center"
          name="quantity"
          required
          @click:prepend-inner="decrementQuantity"
          @click:append="incrementQuantity" />
        <div>{{ $t('exoplatform.perkstore.label.purchasePrice', {0: amountLabel}) }} </div>
        <v-text-field
          v-if="needPassword && opened"
          v-model="walletPassword"
          :append-icon="walletPasswordShow ? 'visibility_off' : 'visibility'"
          :type="walletPasswordShow ? 'text' : 'password'"
          :disabled="loading"
          :rules="requiredRule"
          :label="$t('exoplatform.perkstore.label.walletPassword')"
          :placeholder="$t('exoplatform.perkstore.label.walletPasswordPlaceholder')"
          name="walletPassword"
          autocomplete="current-passord"
          counter
          required
          autofocus
          @click:append="walletPasswordShow = !walletPasswordShow" />
      </v-form>
    </v-card-text>
    <v-card-actions>
      <v-spacer />
      <v-btn
        :disabled="disablePayButton"
        :loading="loading || walletLoading"
        class="primary mr-1"
        @click="payProduct">
        {{ $t('exoplatform.perkstore.button.buy') }}
      </v-btn>
      <button
        v-if="!integratedForm"
        class="ignore-vuetify-classes btn"
        :disabled="loading || walletLoading"
        @click="$emit('close')">
        {{ $t('exoplatform.perkstore.button.close') }}
      </button>
      <v-spacer />
    </v-card-actions>
  </div>
</template>

<script>
import {createOrder} from '../../js/PerkStoreProductOrder.js';

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
    opened: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    integratedForm: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    needPassword: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    walletEnabled: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    walletLoading: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
  },
  data() {
    return {
      loading: false,
      quantity: 1,
      walletPassword: '',
      walletPasswordShow: false,
      error: null,
      requiredRule: [(v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField')],
      requiredNumberRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => !v || this.isPositiveNumber(v) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
        (v) => !v || !this.limitedQuantity || this.quantity <= this.maxQuantity || this.$t('exoplatform.perkstore.warning.maxQuantityReached', {0: this.maxQuantity}),
      ],
    };
  },
  computed: {
    disablePayButton() {
      return !this.product || !this.walletEnabled || this.walletLoading || !this.quantity || (this.needPassword && !this.walletPassword) || (this.maxOrdersReached && !this.product.unlimited) || !this.isPositiveNumber(this.quantity) || (!this.product.unlimited && this.quantity > this.maxQuantity);
    },
    amount() {
      return (this.quantity && this.product && this.product.price && (this.product.price * this.quantity)) || 0;
    },
    available() {
      if(this.product && !this.product.unlimited) {
        const available = this.product.totalSupply - this.product.purchased;
        return available > 0 ? available : 0;
      } else {
        return 0;
      }
    },
    maxOrdersRemaining() {
      if(this.product && this.product.maxOrdersPerUser && this.product.userData) {
        const totalPurchased = this.product.orderPeriodicity ? (this.product.userData.purchasedInCurrentPeriod || 0) : (this.product.userData.totalPurchased || 0);
        const quantity = this.product.maxOrdersPerUser - totalPurchased;
        return quantity > 0 ? quantity : 0;
      }
      return (this.product && this.product.unlimited) ? 0 : this.available;
    },
    maxQuantity() {
      return this.product && this.product.unlimited ? this.maxOrdersRemaining : Math.min(this.available, this.maxOrdersRemaining);
    },
    maxOrdersReached() {
      return this.product && !this.maxQuantity;
    },
    amountLabel() {
      return `${this.amount || 0} ${this.symbol}`;
    },
    limitedQuantity() {
      return this.product && (this.product.maxOrdersPerUser || !this.product.unlimited);
    },
    quantityInputLabel() {
      if(this.limitedQuantity) {
        return this.$t('exoplatform.perkstore.label.quantityWithMax', {0: this.maxQuantity});
      } else {
        return this.$t('exoplatform.perkstore.label.quantity');
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
  },
  created() {
    document.addEventListener('exo-wallet-send-tokens-pending', this.pendingTransaction);
    document.addEventListener('exo-wallet-send-tokens-error', this.errorTransaction);
  },
  methods: {
    init() {
      this.quantity = 1;
      this.warning = null;
      this.error = null;
      this.loading = false;
      this.walletPassword = '';
      this.walletPasswordShow = false;
    },
    isPositiveNumber(value) {
      return this.product && value && !isNaN(value) && value > 0 && Number.isFinite(value) && (this.product.allowFraction || Number.isSafeInteger(value));
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
      // Check if the event is triggered for current window
      if(this.opened && this.loading) {
        const pendingTransaction = event.detail;
        return createOrder({
          productId: this.product.id,
          transactionHash: pendingTransaction.hash,
          amount: this.amount,
          quantity: this.quantity,
          receiver: this.product.receiverMarchand,
        })
          .then((order) => {
            this.loading = false;
            this.$emit('ordered', order);
          })
          .catch(e => {
            console.debug("Error saving order", e);
            this.loading = false;
            this.error = e && e.message ? e.message : String(e);
          });
      }
    },
    errorTransaction(event) {
      this.loading = false;

      this.error = event.detail;
    },
    payProduct(event) {
      event.preventDefault();
      event.stopPropagation();

      this.error = null;

      if(!this.$refs.form.validate()) {
        return false;
      }

      this.loading = true;

      let qty = this.quantity;
      try {
        qty = this.product.allowFraction ? parseFloat(qty) : parseInt(qty);
      } catch(e) {
        // Nothing to do
      }

      if (!qty || isNaN(qty) || qty <= 0 || !Number.isFinite(qty) || (!this.product.allowFraction && !Number.isSafeInteger(this.quantity))) {
        this.error = this.$t('exoplatform.perkstore.warning.invalidQuantity');
        return;
      }

      this.quantity = qty;

      if (!this.product.unlimited && this.quantity > this.maxQuantity) {
        this.error = this.$t('exoplatform.perkstore.warning.invalidQuantityWithMax', {0: this.maxQuantity});
        return;
      }

      if (!this.product.receiverMarchand) {
        this.error = this.$t('exoplatform.perkstore.warning.noProductMarchand');
        return;
      }

      if (!this.product.price) {
        this.error = this.$t('exoplatform.perkstore.warning.noProductPrice');
        return;
      }

      if (!this.amount) {
        this.error = this.$t('exoplatform.perkstore.warning.noAmountToSend');
        return;
      }

      const message = `${this.product.title}: ${this.quantity} x ${this.product.price}${this.symbol ? this.symbol : ''}`;

      // simulate saving order before sending transaction to blockchain
      return createOrder({
        productId: this.product.id,
        quantity: this.quantity,
        receiver: this.product.receiverMarchand,
      }, true)
        .then(() => {
          document.dispatchEvent(new CustomEvent('exo-wallet-send-tokens', {'detail' : {
            amount: this.amount,
            receiver: this.product.receiverMarchand,
            sender: {
              type: 'user',
              id: eXo.env.portal.userName,
            },
            password: this.walletPassword,
            label: message,
            message: message,
          }}));
        })
        .catch(e => {
          console.debug("Checking order availability error", e);
          this.loading = false;
          this.error = e && e.message ? e.message : String(e);
        });
    },
  },
};
</script>
