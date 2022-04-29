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
        @ordered="$emit('ordered', $event)"
        @close="close" />
    </template>
    <template slot="footer">
      <div class="d-flex mr-2">
        <v-spacer />
        <button
          :loading="loadingAction()"
          class="ignore-vuetify-classes btn me-1"
          @click="close">
          {{ $t('exoplatform.perkstore.button.cancel') }}
        </button>
        <v-btn
          :disabled="disableButton && (!isSameNetworkVersion || !isSameAddress)"
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
  data() {
    return {
      dialog: false,
      isSameNetworkVersion: true,
      isSameAddress: true,
    };
  },
  methods: {
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
      if (window.walletSettings?.wallet?.provider !== 'INTERNAL_WALLET') {
        this.isSameNetworkVersion = parseInt(window.ethereum?.networkVersion) === window.walletSettings?.network?.id;
        this.isSameAddress = window.ethereum?.selectedAddress && window.ethereum?.selectedAddress === window.walletSettings?.wallet?.address || false;
      }
      await this.$refs.BuyModalDrawer.open();
      this.$refs.buyForm.init();
      if (!this.isSameNetworkVersion){
        this.$root.$emit('show-alert', {type: 'warning',message: `${this.$t('exoplatform.perkstore.warn.networkVersion')}<br>${this.walletUtils.getNetworkLink()}`});
      }
      if (!this.isSameAddress){
        this.$root.$emit('show-alert', {type: 'warning',message: this.$t('exoplatform.perkstore.warn.selectedAddress')});
      }
    },
    onCloseDrawer() {
      this.$emit('closeProductDetails');
    },
    close() {
      this.$emit('closeProductDetails');
      this.$refs.BuyModalDrawer.close();
    },
  },
};
</script>
