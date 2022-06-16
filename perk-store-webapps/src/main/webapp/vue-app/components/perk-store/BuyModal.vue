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
    ref="BuyModalDrawer"
    right
    @closed="onCloseDrawer">
    <template slot="title">
      <div class="titleBuyProductDrawer">{{ product && product.title }}</div>
    </template>
    <template slot="content">
      <perk-store-buy-form
        ref="buyForm"
        :product="product"
        :symbol="symbol"
        :need-password="needPassword"
        :wallet-loading="walletLoading"
        :wallet-enabled="walletEnabled"
        @opened-transaction="openTransaction"
        @ordered="$emit('ordered', $event)"
        @close="close" />
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
          :disabled="openedTransaction"
          :loading="loadingAction()"
          class="ignore-vuetify-classes btn me-1"
          @click="close">
          {{ $t('exoplatform.perkstore.button.cancel') }}
        </button>
        <v-btn
          :disabled="(disableButton && (!isSameNetworkVersion || !isSameAddress)) || openedTransaction"
          :loading="loadingAction()"
          class="btn btn-primary me-1"
          large
          @click="buyProduct">
          {{ $t('exoplatform.perkstore.button.buy') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
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
  computed: {
    walletAddress() {
      return  window.walletSettings?.wallet?.address || false;
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
    isMetamaskWallet() {
      return window.walletSettings.wallet?.provider === 'METAMASK';
    },
  },
  created() {
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
  data() {
    return {
      dialog: false,
      isSameNetworkVersion: true,
      isSameAddress: true,
      openedTransaction: false,
      metamaskAddress: null,
      metamaskNetworkId: null,
      metamaskConnected: false,
    };
  },
  methods: {
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
    loadingAction() {
      return this.$refs.buyForm && this.$refs.buyForm.loading;
    },
    disableButton() {
      return this.$refs.buyForm && this.$refs.buyForm.disablePayButton;
    },
    buyProduct(event) {
      this.$refs.buyForm.payProduct(event);
    },
    async open() {
      if (this.isMetamaskWallet) {
        this.updateSelectedMetamaskInformation();
      }
      await this.$refs.BuyModalDrawer.open();
      this.$refs.buyForm.init();
    },
    onCloseDrawer() {
      this.$emit('closeProductDetails');
    },
    close() {
      this.$emit('closeProductDetails');
      this.$refs.BuyModalDrawer.close();
    },
    openTransaction(value) {
      this.openedTransaction = value;
    },
  },
};
</script>
