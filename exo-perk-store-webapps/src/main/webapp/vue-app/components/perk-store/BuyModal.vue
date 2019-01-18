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
          @click="close"></a> <span class="PopupTitle popupTitle ellipsis">
            Buy {{ productTitle }}
          </span>
      </div>
      <v-card-text>
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
            placeholder="Select a quantity to buy"
            required
            @click:prepend-inner="decrementQuantity"
            @click:append="incrementQuantity" />
          <div>Amount: {{ amountLabel }} </div>
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
            autocomplete="current-passord"
            counter
            required
            @click:append="walletPasswordShow = !walletPasswordShow" />
        </v-form>
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
      quantity: 1,
      walletPassword: '',
      walletPasswordShow: false,
      error: null,
      requiredRule: [(v) => !!v || 'Required field'],
      requiredNumberRule: [
        (v) => !!v || 'Required field',
        (v) => !v || this.isPositiveNumber(v) || 'Invalid positive number',
        (v) => !v || !this.limitedQuantity || this.quantity <= this.maxQuantity || `Orders quantity must be less than ${this.maxQuantity}`,
      ],
    };
  },
  computed: {
    disablePayButton() {
      return !this.product || !this.quantity || (this.maxOrdersReached && !this.product.unlimited) || !this.isPositiveNumber(this.quantity) || (!this.product.unlimited && this.quantity > this.maxQuantity);
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
        return `Quantity (max: ${this.maxQuantity})`;
      } else {
        return `Quantity`;
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
        this.quantity = 1;
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
      if(this.dialog && this.loading) {
        const pendingTransaction = event.detail;
        return createOrder({
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
      this.error = null;

      event.preventDefault();
      event.stopPropagation();

      if(!this.$refs.form.validate()) {
        return;
      }

      this.loading = true;

      let qty = this.quantity;
      try {
        qty = this.product.allowFraction ? parseFloat(qty) : parseInt(qty);
      } catch(e) {
        // Nothing to do
      }

      if (!qty || isNaN(qty) || qty <= 0 || !Number.isFinite(qty) || (!this.product.allowFraction && !Number.isSafeInteger(this.quantity))) {
        this.error = 'Invalid quantity';
        return;
      }

      this.quantity = qty;

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

      if (!this.amount) {
        this.error = `Empty amount`;
        return;
      }

      const message = `Purchased "${this.product.title}": ${this.quantity} x ${this.product.price}${this.symbol ? this.symbol : ''}`;

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
