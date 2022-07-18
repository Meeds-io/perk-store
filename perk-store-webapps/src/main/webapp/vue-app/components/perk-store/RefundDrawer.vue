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
  <exo-drawer
    ref="refundForm"
    :right="!$vuetify.rtl">
    <template slot="title">
      {{ $t('exoplatform.perkstore.title.refundOrderModal', {0: order && order.id}) }}
    </template>
    <template slot="content">
      <v-card
        id="refundForm"
        flat>
        <v-card-text class="pt-0">
          <div v-if="warning && !loading && !walletLoading" class="alert alert-warning v-content ma-5">
            <i class="uiIconWarning"></i>
            {{ warning }}
          </div>
          <div v-if="isInternalWallet && error && !loading" class="alert alert-error v-content ma-5">
            <i class="uiIconError"></i>
            {{ error }}
          </div>
          <v-form
            ref="form"
            v-model="validForm">
            <v-row class="ma-2">
              <v-col
                cols="12"
                sm="6" 
                class="d-flex align-center ml-n5">
                <label class="font-weight-bold">{{ $t('exoplatform.perkstore.label.quantity') }}</label>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                align="right">
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
              </v-col>
            </v-row>
            <label class="font-weight-bold">{{ $t('exoplatform.perkstore.label.amount') }}</label>
            <v-row class="ma-1">
              <v-text-field
                v-model.number="amount"
                :disabled="loading"
                :placeholder="$t('exoplatform.perkstore.label.refundAmountPlaceholder')"
                :rules="requiredAmountRule"
                name="amount"
                class="text-left quantity perkStoreTextField"
                autofocus
                required />  
            </v-row>
            <label v-if="needPassword && !storedPassword && isInternalWallet" class="font-weight-bold mt-5">{{ $t('exoplatform.wallet.label.walletPassword') }}</label>
            <label v-else-if="!isInternalWallet" class="font-weight-bold mt-5">{{ $t('exoplatform.wallet.label.settings.internal') }}</label>

            <v-row class="pl-6" v-if="!isInternalWallet">
              <v-col
                cols="12"
                sm="6" 
                class="d-flex align-center ml-n5">
                <img
                  class="mt-n1"
                  :src="`/wallet-common/images/metamask.svg`"
                  alt="Metamask"
                  width="18">
                <span class="pl-2 amountLabel">{{ $t('exoplatform.wallet.label.metamask') }}</span>
              </v-col>
              <v-col
                cols="12"
                sm="6"
                align="right">
                <v-chip class="grey-background">  
                  <span class="mr-3 dark-grey-color walletTitle">
                    {{ metamaskAddressPreview }}
                  </span>
                  <wallet-settings-jdenticon :address="walletAddress" />
                </v-chip>
              </v-col>
            </v-row>
            <v-text-field
              v-if="needPassword && !storedPassword && isInternalWallet"
              ref="walletPassword"
              v-model="walletPassword"
              :append-icon="walletPasswordShow ? 'mdi-eye' : 'mdi-eye-off'"
              :type="walletPasswordShow ? 'text' : 'password'"
              :disabled="loading"
              :rules="mandatoryRule"
              :placeholder="$t('exoplatform.wallet.label.walletPasswordPlaceholder')"
              name="walletPassword"
              required
              validate-on-blur
              class="ma-3"
              autocomplete="current-password"
              @click:append="walletPasswordShow = !walletPasswordShow" />
          </v-form>
        </v-card-text>
      </v-card>
    </template>
    <template slot="footer">
      <div v-if="displayMetamaskWarnings">
        <wallet-metamask-warnings
          :not-installed="!metamaskInstalled"
          :not-connected="!metamaskConnected"
          :invalid-network="invalidMetamaskNetwork"
          :invalid-account="invalidMetamaskAccount" />      
      </div>
      <div class="d-flex mr-2" v-else>
        <v-spacer />
        <button
          :disabled="!validForm || loading || walletLoading"
          class="ignore-vuetify-classes btn btn-primary mx-1"
          @click="refundProduct">
          {{ $t('exoplatform.perkstore.button.refund') }}
        </button>
        <button
          class="ignore-vuetify-classes btn"
          :disabled="loading || walletLoading"
          @click="close">
          {{ $t('exoplatform.perkstore.button.close') }}
        </button>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
import {toFixed, saveOrderStatus, createOrder} from '../../js/PerkStoreProductOrder.js';
import {searchUserOrSpaceObject} from '../../js/PerkStoreIdentityRegistry.js';
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
      drawer: false,
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
        // eslint-disable-next-line no-unused-vars
        (v) => !this.order || this.quantity <= Number(this.order.remainingQuantityToProcess) || this.$t('exoplatform.perkstore.warning.maxQuantityReached', {0: this.remainingQuantityToProcess}),
      ],
      requiredAmountRule: [
        (v) => !!v || this.$t('exoplatform.perkstore.warning.requiredField'),
        (v) => this.isPositiveNumber(v, false) || this.$t('exoplatform.perkstore.warning.invalidPositiveNumber'),
        (v) => v <= this.maxAmount || this.$t('exoplatform.perkstore.warning.maxAmountReached', {0: this.maxAmount}),
      ],
      metamaskAddress: null,
      metamaskNetworkId: null,
      metamaskConnected: false,
    };
  },
  created() {
    this.walletAddonInstalled = window.walletAddonInstalled;
    
    document.addEventListener('exo-wallet-send-tokens-pending', this.pendingTransaction);
    document.addEventListener('exo-wallet-send-tokens-error', this.errorTransaction);
    document.addEventListener('exo-wallet-init-result', this.walletInitialized);

    document.addEventListener('wallet-metamask-accountsChanged', this.updateSelectedMetamaskAddress);
    document.addEventListener('wallet-metamask-chainChanged', this.updateSelectedMetamaskNetworkId);
    document.addEventListener('wallet-metamask-connected', this.updateSelectedMetamaskInformation);
    document.addEventListener('wallet-metamask-disconnected', this.updateSelectedMetamaskInformation);
  },
  beforeDestroy() {
    document.removeEventListener('wallet-metamask-accountsChanged', this.updateSelectedMetamaskAddress);
    document.removeEventListener('wallet-metamask-chainChanged', this.updateSelectedMetamaskNetworkId);
    document.removeEventListener('wallet-metamask-chainChanged', this.updateSelectedMetamaskInformation);
    document.removeEventListener('wallet-metamask-chainChanged', this.updateSelectedMetamaskInformation);
  },
  methods: {
    isPositiveNumber(value, isInt) {
      value = Number(value);
      return this.product && value && value > 0 && Number.isFinite(value) && (!isInt || Number.isSafeInteger(value));
    },
    walletInitialized(event) {
      this.walletLoading = false;
      const result = event && event.detail;
      if (!result || result.error) {
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
      if (this.drawer && this.loading) {
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
            this.loading = false;
            this.drawer = false;
          })
          .catch(e => {
            console.error('Error saving refund order', e);
            this.loading = false;
            this.drawer = false;
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

      if (!this.$refs.form.validate()) {
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
      
      if (this.isInternalWallet) {
        // simulate saving order before sending transaction to blockchain
        return createOrder({
          productId: this.product.id,
          quantity: this.quantity,
          receiver: this.order.sender,
        }, true)
          .then(() => {
            document.dispatchEvent(new CustomEvent('exo-wallet-send-tokens', {'detail': {
              amount: this.amount,
              receiver: this.order.sender,
              sender: this.order.receiver,
              password: this.walletPassword,
              label: message,
              message: message,
            }}));
          })
          .catch(e => {
            console.error('Checking order availability error', e);
            this.loading = false;
            this.showAlert(
              'error', 
              this.$t('exoplatform.wallet.metamask.error.transactionFailed'), 
            );
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
                from: this.walletAddress, 
                data: this.contractDetails.contract.methods.transfer(this.order.sender.id, this.DecimalsAmount).encodeABI(),
              };
              window.ethereum.request({
                method: 'eth_sendTransaction',
                params: [transactionParameters],
              })
                .then(transactionHash => 
                  this.transactionUtils.saveTransactionDetails({
                    'contractAddress': this.contractDetails.address,
                    'contractAmount': this.amount,
                    'contractMethodName': 'transfer',
                    'from': wallet.address,
                    'label': message,
                    'message': message,
                    'pending': true,
                    'hash': transactionHash,
                    'timestamp': Date.now(),
                    'to': this.order.sender.id,
                  })
                )
                .then((savedTransaction)=> {
                  document.dispatchEvent(new CustomEvent('exo-wallet-send-tokens-pending', {
                    detail: savedTransaction
                  }));
                  this.showAlert(
                    'success', 
                    this.$t('exoplatform.wallet.metamask.message.transactionSent'), 
                    savedTransaction.hash,
                  );
                  this.$emit('close');
                  this.close();
                })
                .catch(e => {
                  this.$emit('opened-transaction', false);
                  if (e && e.code === 4001) {
                    // User rejected transaction from Metamask popin
                    return;
                  }
                  console.error('Checking order availability error', e);
                  this.showAlert(
                    'error', 
                    this.$t('exoplatform.wallet.metamask.error.transactionFailed'), 
                  );
                })
                .finally(() => this.loading = false);
            });
        }
      }
    },
    showAlert(alertType, alertMessage, alertTransactionHash) {
      this.$root.$emit('wallet-notification-alert', {
        type: alertType,
        message: alertMessage,
        transactionHash: alertTransactionHash,
      });
    },
    open() {
      if (this.isMetamaskWallet) {
        this.updateSelectedMetamaskInformation();
      }
      this.drawer = true;
      this.$refs.refundForm.open();
    },
    close(){
      this.$refs.refundForm.close();
    
    },
    updateSelectedMetamaskNetworkId() {
      this.metamaskNetworkId = window.walletSettings.metamask?.networkId;
    },
    updateSelectedMetamaskAddress() {
      this.metamaskAddress = window.walletSettings.metamask?.address;
    },
    updateSelectedMetamaskInformation() {
      this.metamaskInstalled = window.walletSettings.metamask?.installed;
      this.metamaskConnected = window.walletSettings.metamask?.connected;
      this.updateSelectedMetamaskNetworkId();
      this.updateSelectedMetamaskAddress();
    },
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
    walletAddress() {
      return window.walletSettings.wallet.address;
    },
    invalidMetamaskAccount() {
      return !this.metamaskAddress || (this.metamaskAddress || '').toLowerCase() !== (this.walletAddress || '').toLowerCase();
    },
    invalidMetamaskNetwork() {
      return this.metamaskNetworkId !== window.walletSettings?.network?.id;
    },
    displayMetamaskWarnings() {
      return this.isMetamaskWallet && (this.invalidMetamaskNetwork || this.invalidMetamaskAccount || !this.metamaskConnected);
    },
    metamaskAddressPreview() {
      return this.walletAddress && `${this.walletAddress.substring(0,5)}...${this.walletAddress.substring(this.walletAddress.length-4,this.walletAddress.length)}`;
    },
    isInternalWallet() {
      return window.walletSettings.wallet?.provider === 'INTERNAL_WALLET';
    },
    isMetamaskWallet() {
      return window.walletSettings.wallet?.provider === 'METAMASK';
    },
    contractDetails() {
      return this.tokenUtils.getContractDetails(this.walletAddress);
    },
    DecimalsAmount() {
      return this.walletUtils.convertTokenAmountToSend(this.amount, this.contractDetails.decimals);
    },
  },
  watch: {
    quantity() {
      if (this.quantity) {
        this.amount = (this.product && this.product.price && this.quantity && toFixed(this.quantity * this.product.price)) || 0;
      } else {
        this.amount = 0;
      }
    },
    error() {
      if (this.error) {
        this.loading = false;
      }
    },
    drawer() {
      if (this.drawer) {
        this.walletLoading = true;
        this.walletEnabled = false;
        document.dispatchEvent(new CustomEvent('exo-wallet-init', {'detail': {sender: this.order.receiver}}));
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
};
</script>