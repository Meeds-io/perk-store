<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-layout
    column
    grow
    class="buyProductForm">
    <v-flex v-if="error && !loading" xs12>
      <div class="alert alert-error v-content">
        <i class="uiIconError"></i>
        {{ error }}
      </div>
    </v-flex>
    <v-flex xs12>
      <v-layout
        row
        grow
        class="no-wrap mx-0">
        <template v-if="!integrated">
          <v-flex
            v-if="productImage"
            class="d-sm-flex d-none"
            sm5>
            <v-img
              :src="productImage"
              aspect-ratio="1"
              max-width="200px"
              max-height="100%"
              class="productImageBuyPage" />
          </v-flex>
          <v-flex
            v-else
            class="d-sm-flex d-none"
            sm5>
            <v-icon class="productImageBuyPage productNoImages">fa-images</v-icon>
          </v-flex>
        </template>
        <v-flex
          :sm7="!integrated"
          xs12>
          <v-layout column grow>
            <v-card-text class="py-0">
              <v-form ref="form">
                <v-divider v-if="integrated" />
                <div class="my-3 primary--text">{{ $t('exoplatform.perkstore.label.unitPrice') }}: {{ productPrice }} {{ symbol }}</div>
                <v-divider />
                <template v-if="product && !product.unlimited">
                  <div class="my-3">{{ $t('exoplatform.perkstore.label.availableQuantity') }}: {{ maxOrdersRemaining }}</div>
                  <v-divider />
                </template>
                <template v-if="integrated">
                  <div class="my-3">
                    {{ $t('exoplatform.perkstore.label.yourOrders') }}: {{ purchasedOrders }}
                    <template v-if="maxOrdersPerUser">
                      / {{ maxOrdersPerUser }}
                    </template>
                  </div>
                  <v-divider />
                </template>
                <div class="my-3">{{ $t('exoplatform.perkstore.label.purchasePrice', {0: amountLabel}) }}</div>
                <div class="buyFormFields">
                  <v-text-field
                    v-model.number="quantity"
                    :disabled="loading"
                    :rules="quantityRules"
                    :label="quantityInputLabel"
                    :placeholder="$t('exoplatform.perkstore.label.quantityPlaceholder')"
                    append-icon="fa-plus"
                    prepend-inner-icon="fa-minus"
                    class="text-center"
                    name="quantity"
                    required
                    @click:prepend-inner="decrementQuantity"
                    @click:append="incrementQuantity" />
                  <v-text-field
                    v-if="needPassword && opened"
                    v-model="walletPassword"
                    :append-icon="walletPasswordShow ? 'mdi-eye' : 'mdi-eye-off'"
                    :type="walletPasswordShow ? 'text' : 'password'"
                    :disabled="loading"
                    :rules="requiredRule"
                    :label="$t('exoplatform.perkstore.label.walletPassword')"
                    :placeholder="$t('exoplatform.perkstore.label.walletPasswordPlaceholder')"
                    name="walletPassword"
                    autocomplete="current-passord"
                    required
                    autofocus
                    validate-on-blur
                    @click:append="walletPasswordShow = !walletPasswordShow" />
                </div>
              </v-form>
            </v-card-text>
          </v-layout>
        </v-flex>
      </v-layout>
    </v-flex>
    <v-flex class="offset-sm-5 sm7 xs12 mt-3">
      <v-card-actions>
        <v-btn
          :disabled="disablePayButton"
          :loading="loading || walletLoading"
          class="primary me-1"
          large
          @click="payProduct">
          {{ $t('exoplatform.perkstore.button.buy') }}
        </v-btn>
        <v-spacer />
        <button
          class="ignore-vuetify-classes btn"
          :disabled="loading || walletLoading"
          @click="$emit('close')">
          {{ $t('exoplatform.perkstore.button.cancel') }}
        </button>
      </v-card-actions>
    </v-flex>
  </v-layout>
</template>

<script>
import {toFixed, createOrder} from '../../js/PerkStoreProductOrder.js';

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
    integrated: {
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
      quantityRules: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => this.isPositiveNumber(v) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
        (v) => !this.product || this.product.unlimited || this.quantity <= this.available || this.$t('exoplatform.perkstore.warning.quantityExceedsTotalSupply', {0: this.available}),
        (v) => !this.product || !this.product.maxOrdersPerUser || this.quantity <= this.remainingOrdersForUser || this.$t('exoplatform.perkstore.warning.quantityExceedsAllowedOrders', {0: this.remainingOrdersForUser}),
      ],
    };
  },
  computed: {
    productImage() {
      return this.product && this.product.imageFiles && this.product.imageFiles[0] && this.product.imageFiles[0].src;
    },
    disablePayButton() {
      return !this.product || !this.walletEnabled || this.walletLoading || !this.quantity || (this.needPassword && !this.walletPassword) || (this.maxOrdersReached && !this.product.unlimited) || !this.isPositiveNumber(this.quantity) || (!this.product.unlimited && this.quantity > this.maxQuantity);
    },
    amount() {
      return (this.quantity && this.product && this.product.price && toFixed(this.product.price * this.quantity)) || 0;
    },
    available() {
      if(this.product && !this.product.unlimited) {
        const available = this.product.totalSupply - this.product.purchased;
        return available > 0 ? available : 0;
      } else {
        return 0;
      }
    },
    maxOrdersPerUser() {
      return this.product && this.product.maxOrdersPerUser;
    },
    purchasedOrders() {
      return this.product && this.product.userData && (this.product.orderPeriodicity ? (this.product.userData.purchasedInCurrentPeriod || 0) : (this.product.userData.totalPurchased || 0));
    },
    remainingOrdersForUser() {
      if(this.maxOrdersPerUser && this.product.userData) {
        return this.maxOrdersPerUser - this.purchasedOrders;
      }
      return 0;
    },
    productPrice() {
      return this.product && this.product.price;
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
