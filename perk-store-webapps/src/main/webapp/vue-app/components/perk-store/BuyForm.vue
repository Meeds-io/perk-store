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
  <v-form id="buyFormProduct" ref="form">
    <v-container>
      <v-flex
        v-if="errors && errors.length > 0 && !loading"
        class="errorContainer">
        <div
          v-for="error in errors"
          :key="error"
          class="alert alert-error v-content">
          <i class="uiIconError"></i>
          {{ error }}
        </div>
      </v-flex>
      <v-row v-if="product && product.description">
        <div class="d-flex flex-column pl-4 pb-2">
          <label class="mb-1 font-weight-bold">{{ $t('exoplatform.perkstore.label.productDescription') }}:</label>
          <div class="font-weight-regular">{{ product.description }}</div>
        </div>
      </v-row>
      <v-row v-if="productImage">
        <label class="pl-4 pb-2 font-weight-bold">{{ $t('exoplatform.perkstore.label.Gallery') }}</label>
      </v-row>
      <v-row v-if="productImage">
        <v-col
          v-for="img in product.imageFiles.slice(0, 3)"
          :key="img.src"
          class="pl-4"
          cols="12"
          sm="4">
          <a :href="img.src" target="_blank">
            <v-img
              :src="img.src"
              aspect-ratio="1"
              max-width="200px"
              max-height="100%"
              class="productImageBuyPage" />
          </a>
        </v-col>
      </v-row>
      <v-row class="pl-5">
        <v-col
          cols="12"
          sm="6">
          <v-row class="pb-3">
            <label class="font-weight-bold">{{ $t('exoplatform.perkstore.label.Marchant') }}:</label>
          </v-row>
          <v-row v-if="product && product.receiverMarchand" class="pb-3">
            <exo-user-avatar
              v-if="product.receiverMarchand.type === 'user'"
              :key="product.receiverMarchand.id"
              :profile-id="product.receiverMarchand.id"
              :size="25"
              extra-class="buyFormMarchant"
              popover />
            <exo-space-avatar
              v-if="product.receiverMarchand.type === 'space'"
              :key="product.receiverMarchand.id"
              :space-id="product.receiverMarchand.spaceId"
              :size="25"
              extra-class="buyFormMarchant"
              popover />
          </v-row>
          <v-row>
            <label class="font-weight-bold">{{ $t('exoplatform.perkstore.label.QuantityToBuy') }}:</label>
          </v-row>
          <v-row>
            <v-text-field
              v-model.number="quantity"
              :disabled="loading"
              :rules="quantityRules"
              append-icon="fa-plus"
              prepend-inner-icon="fa-minus"
              class="text-center pt-1 quantity perkStoreTextField"
              name="quantity"
              required
              @click:prepend-inner="decrementQuantity"
              @click:append="incrementQuantity" />
          </v-row>
        </v-col>
        <v-col
          cols="12"
          sm="6">
          <v-row class="pb-3">
            <label class="font-weight-bold">
              {{ $t('exoplatform.perkstore.label.unitPrice') }}:
            </label>
          </v-row>
          <v-row class="pb-3">
            <label class="font-weight-bold">
              <span class="priceSymbol">{{ symbol }}</span> {{ productPrice }}
            </label>
          </v-row>
          <v-row class="pb-4 mt-1">
            <label class="font-weight-bold">
              {{ $t('exoplatform.perkstore.label.AmountToBuy') }}:
            </label>
          </v-row>
          <v-row>
            <label class="font-weight-bold">
              <span class="priceSymbol">{{ symbol }}</span>  {{ amountLabel }}
            </label>
          </v-row>
        </v-col>
      </v-row>
      <v-row class="pl-5">
        <label v-if="needPassword && isInternalWallet" class="font-weight-bold">{{ $t('exoplatform.perkstore.label.walletPassword') }}</label>
        <label v-else-if="!isInternalWallet" class="font-weight-bold">{{ $t('exoplatform.perkstore.label.wallet') }}</label>
      </v-row>
      <v-row class="pl-5">
        <v-col
          v-if="!isInternalWallet"
          cols="12"
          sm="6" 
          class="d-flex align-center ml-n3">
          <img
            class="mt-n1"
            :src="`/wallet-common/images/metamask.svg`"
            alt="Metamask"
            width="18">
          <span class="pl-2">Metamask :</span>
        </v-col>
        <v-col
          v-if="!isInternalWallet"
          cols="12"
          sm="6"
          align="right">
          <v-chip class="grey-background">  
            <span class="mr-3 dark-grey-color walletTitle">
              {{ metamaskAddressPreview }}
            </span>
            <wallet-settings-jdenticon :address="senderAddress" />
          </v-chip>
        </v-col>
        <v-text-field
          v-if="needPassword && isInternalWallet"
          v-model="walletPassword"
          :append-icon="walletPasswordShow ? 'mdi-eye' : 'mdi-eye-off'"
          :type="walletPasswordShow ? 'text' : 'password'"
          :disabled="loading"
          :rules="requiredRule"
          :placeholder="$t('exoplatform.perkstore.label.walletPasswordPlaceholder')"
          name="walletPassword"
          autocomplete="current-passord"
          class="passwordWallet pt-0"
          required
          autofocus
          color="grey darken-4"
          validate-on-blur
          @click:append="walletPasswordShow = !walletPasswordShow" />
      </v-row>
    </v-container>
  </v-form>
</template>

<script>
import {toFixed, createOrder} from '../../js/PerkStoreProductOrder.js';
import {searchUserOrSpaceObject} from '../../js/PerkStoreIdentityRegistry.js';

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
      errors: [],
      requiredRule: [(v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField')],
      quantityRules: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => this.isPositiveNumber(v) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
        // eslint-disable-next-line no-unused-vars
        (v) => !this.product || this.product.unlimited || this.quantity <= this.available || this.$t('exoplatform.perkstore.warning.quantityExceedsTotalSupply', {0: this.available}),
        // eslint-disable-next-line no-unused-vars
        (v) => !this.product || !this.product.maxOrdersPerUser || this.quantity <= this.remainingOrdersForUser || this.$t('exoplatform.perkstore.warning.quantityExceedsAllowedOrders', {0: this.remainingOrdersForUser}),
      ],

    };
  },
  computed: {
    getUrl() {
      if (this.product.creator.type === 'user') {
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.product.creator.id}`;
      } else if (this.product.creator.spaceId && this.product.creator.spaceId !== 0) {
        let groupId = this.product.creator.spaceURLId;
        if (groupId) {
          groupId = groupId.replace(/\//g, ':');
          return `${eXo.env.portal.context}/g/${groupId}/`;
        }
      }
      return '';
    },
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
      if (this.product && !this.product.unlimited) {
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
      if (this.maxOrdersPerUser && this.product.userData) {
        return this.maxOrdersPerUser - this.purchasedOrders;
      }
      return 0;
    },
    productPrice() {
      return this.product && this.product.price;
    },
    maxOrdersRemaining() {
      if (this.product && this.product.maxOrdersPerUser && this.product.userData) {
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
      return `${this.amount || 0}`;
    },
    limitedQuantity() {
      return this.product && (this.product.maxOrdersPerUser || !this.product.unlimited);
    },
    quantityInputLabel() {
      if (this.limitedQuantity) {
        return this.$t('exoplatform.perkstore.label.quantityWithMax', {0: this.maxQuantity});
      } else {
        return this.$t('exoplatform.perkstore.label.quantity');
      }
    },
    productTitle() {
      console.log('##########product', this.product);
      return this.product && this.product.title ?  this.product.title : '';
    },
    provider() {
      return window.walletSettings.wallet.provider;
    },

    isInternalWallet() {
      return this.provider === 'INTERNAL_WALLET';
    },
    senderAddress() {
      return window.walletSettings.wallet.address;
    },
    contractDetails() {
      return this.tokenUtils.getContractDetails(this.senderAddress);
    },
    DecimalsAmount() {
      return this.walletUtils.convertTokenAmountToSend(this.amount, this.contractDetails.decimals);
    },
    metamaskAddressPreview() {
      return this.senderAddress && `${this.senderAddress.substring(0,5)}...${this.senderAddress.substring(this.senderAddress.length-4,this.senderAddress.length)}`;
    },
    
  },
  watch: {
    errors() {
      if (this.errors && this.errors.length > 0) {
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
      this.errors = [];
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
      if (this.loading) {
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
            console.error('Error saving order', e);
            this.loading = false;
            this.errors.push(e && e.message ? e.message : String(e));
          });
      }
    },
    errorTransaction(event) {
      this.loading = false;

      this.errors.push(event.detail);
    },
    payProduct(event) {
      event.preventDefault();
      event.stopPropagation();

      this.errors = [];

      if (!this.$refs.form.validate()) {
        return false;
      }

      this.loading = true;

      let qty = this.quantity;
      try {
        qty = this.product.allowFraction ? parseFloat(qty) : parseInt(qty);
      } catch (e) {
        // Nothing to do
      }

      if (!qty || isNaN(qty) || qty <= 0 || !Number.isFinite(qty) || (!this.product.allowFraction && !Number.isSafeInteger(this.quantity))) {
        this.errors.push(this.$t('exoplatform.perkstore.warning.invalidQuantity'));
        return;
      }

      this.quantity = qty;

      if (!this.product.unlimited && this.quantity > this.maxQuantity) {
        this.errors.push(this.$t('exoplatform.perkstore.warning.invalidQuantityWithMax', {0: this.maxQuantity}));
        return;
      }

      if (!this.product.receiverMarchand) {
        this.errors.push(this.$t('exoplatform.perkstore.warning.noProductMarchand'));
        return;
      }

      if (!this.product.price) {
        this.errors.push(this.$t('exoplatform.perkstore.warning.noProductPrice'));
        return;
      }

      if (!this.amount) {
        this.errors.push(this.$t('exoplatform.perkstore.warning.noAmountToSend'));
        return;
      }
      const message = `${this.product.title}: ${this.quantity} x ${this.product.price}${this.symbol ? this.symbol : ''}`;

      if (this.isInternalWallet) {
        // simulate saving order before sending transaction to blockchain
        return createOrder({
          productId: this.product.id,
          quantity: this.quantity,
          receiver: this.product.receiverMarchand,
        }, true)
          .then(() => {
            document.dispatchEvent(new CustomEvent('exo-wallet-send-tokens', {'detail': {
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
            console.error('Checking order availability error', e);
            this.loading = false;
            this.errors.push(e && e.message ? e.message : String(e));
          }).finally(() => {
            if (!this.errors || this.errors.length === 0) {
              this.$emit('close');
            }
          });
      } else {
        if (window.ethereum?.isMetaMask) {
          return searchUserOrSpaceObject(
            this.product.receiverMarchand.id,
            this.product.receiverMarchand.type
          )
            .then(wallet => {
              this.$emit('opened-transaction', true);
              const transactionParameters = {
                to: this.contractDetails.address, 
                from: this.senderAddress, 
                data: this.contractDetails.contract.methods.transfer(wallet.address, this.DecimalsAmount).encodeABI(),
              };
              window.ethereum.request({
                method: 'eth_sendTransaction',
                params: [transactionParameters],
              })
                .then((transactionHash)=>{
                  this.$emit('opened-transaction', false);
                  return createOrder({
                    productId: this.product.id,
                    quantity: this.quantity,
                    receiver: this.product.receiverMarchand,
                  }, true)
                    .then(() => {
                      this.transactionUtils.saveTransactionDetails({
                        'contractAddress': this.contractDetails.address,
                        'contractAmount': this.amount,
                        'contractMethodName': 'transfer',
                        'from': this.senderAddress,
                        'label': message,
                        'message': message,
                        'pending': true,
                        'hash': transactionHash,
                        'timestamp': Date.now(),
                        'to': wallet.address,
                      })
                        .then((savedTransaction)=> {document.dispatchEvent(new CustomEvent('exo-wallet-send-tokens-pending', {detail: savedTransaction}));});
                    })
                    .catch(e => {
                      console.error('Checking order availability error', e);
                      this.loading = false;
                      this.errors.push(e && e.message ? e.message : String(e));
                    }).finally(() => {
                      if (!this.errors || this.errors.length === 0) {
                        this.$emit('close');
                        this.$root.$emit('show-alert', {type: 'success',message: this.$t('exoplatform.perkstore.order.alert.success')});
                      }
                    });
                })
                .catch(() => {
                  this.loading = false;
                  this.$emit('opened-transaction', false);
                });
            });
            
           

         
        }
      }
    },
  },
};
</script>
