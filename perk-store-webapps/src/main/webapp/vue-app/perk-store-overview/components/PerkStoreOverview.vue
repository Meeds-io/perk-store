<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2022 Meeds Association
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
  <div
    v-if="hasProducts"
    class="mx-0 d-flex flex-row mt-2">
    <template v-for="(product, index) in productsToDisplay">
      <v-spacer v-if="index > 0 && justifyBorders" :key="index" />
      <v-hover v-slot="{hover}" :key="product.id">
        <v-card
          :key="product.id"
          :elevation="hover ? 3 : 0"
          max-width="33%"
          flat
          class="mx-auto"
          @click="openBuyModal(product)">
          <perk-store-product-detail
            :product="product"
            :overview-display="true"
            :wallet-deleted="walletDeleted"
            class="width-fit-content"
            hide-elevation
            wallet-enabled
            @buy="openBuyModal" />
        </v-card>
      </v-hover>
    </template>
    <perk-store-buy-modal
      v-if="selectedProduct"
      ref="buyModal"
      :product="selectedProduct"
      :symbol="symbol"
      :need-password="walletNeedPassword"
      wallet-enabled />
    <wallet-notification-alert />
  </div>
</template>
<script>
import {getProductList} from '../../js/PerkStoreProduct.js';
import {getSettings} from '../../js/PerkStoreSettings.js';
export default {
  data: () => ({
    loading: true,
    wallet: null,
    symbol: null,
    selectedProduct: null,
    walletNeedPassword: false,
    products: [],
  }),
  computed: {
    hasProducts() {
      return this.productsToDisplay?.length;
    },
    justifyBorders() {
      return this.productsToDisplay.length > 2;
    },
    enabledProducts() {
      return this.products?.filter(product => product.enabled && !product.deleted && (product.unlimited || product.totalSupply > product.purchased)) || [];
    },
    productsToDisplay() {
      return this.enabledProducts?.slice(0, 3) || [];
    },
    walletDeleted() {
      return this.wallet?.initializationState === 'DELETED';
    },
  },
  watch: {
    hasProducts() {
      if (this.hasProducts) {
        document.dispatchEvent(new CustomEvent('perk-store-products-loaded'));
      }
    },
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      this.getProductsList();
      this.wallet = window?.walletSettings?.wallet;
      this.symbol = window?.walletSettings?.contractDetail?.symbol;
      this.walletNeedPassword = window?.walletSettings?.browserWalletExists && !window?.walletSettings?.storedPassword;
    },
    openBuyModal(product) {
      this.selectedProduct = product;
      this.$nextTick().then(() => this.$refs.buyModal.open());
    },
    getProductsList() {
      this.loading = true;
      return getProductList()
        .then((products) => {
          this.products = products;
          if (this.productsToDisplay?.length) {
            document.dispatchEvent(new CustomEvent('perk-store-products-loaded', {detail: {
              hasProducts: true,
              canAddProduct: false,
            }}));
          } else {
            return getSettings()
              .then(settings => {
                document.dispatchEvent(new CustomEvent('perk-store-products-loaded', {detail: {
                  hasProducts: false,
                  canAddProduct: settings?.userSettings?.canAddProduct,
                }}));
              });
          }
        })
        .finally(() => this.loading = false);
    }
  }
};
</script>