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
  <v-card flat>
    <v-expand-transition name="fade" appear>
      <v-flex v-show="selectedProduct" class="productDetailContainer">
        <perk-store-product-detail-maximized
          :product="selectedProduct || {}"
          :symbol="symbol"
          :need-password="needPassword"
          :wallet-loading="walletLoading"
          :wallet-enabled="walletEnabled"
          :wallet-deleted="walletDeleted"
          class="border-box-sizing"
          @ordered="$emit('ordered', $event)"
          @close="$emit('close')" />
      </v-flex>
    </v-expand-transition>
    <template v-if="!selectedProduct">
      <v-container
        v-if="canAddProduct || (products && products.length)"
        class="border-box-sizing productsListParentContainer"
        fluid
        grid-list-md>
        <v-layout
          row
          wrap
          class="productsListParent">
          <template v-if="products && products.length">
            <template v-for="product in products">
              <perk-store-product-detail
                :key="product.id"
                :product="product"
                :symbol="symbol"
                :wallet-loading="walletLoading"
                :wallet-enabled="walletEnabled"
                :wallet-deleted="walletDeleted"
                class="border-box-sizing"
                @product-details="$emit('product-details', $event)"
                @orders-list="displayOrdersList"
                @edit="$emit('edit', $event)"
                @buy="$emit('buy', $event)"
                @product-deleted="$emit('product-deleted', $event)" />
            </template>
          </template>
        </v-layout>
      </v-container>
      <v-container v-else-if="!loading" class="text-center">
        <div class="alert alert-info" style="display: inline-block">
          <i class="uiIconInfo"></i>
          {{ $t('exoplatform.perkstore.info.noAvailableProduct') }}
        </div>
      </v-container>
    </template>
  </v-card>
</template>

<script>
export default {
  props: {
    products: {
      type: Array,
      default: function() {
        return [];
      },
    },
    selectedProduct: {
      type: Object,
      default: function() {
        return null;
      },
    },
    symbol: {
      type: String,
      default: function() {
        return '';
      },
    },
    loading: {
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
    walletLoading: {
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
    walletDeleted: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    canAddProduct: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
  },
  methods: {
    displayOrdersList(productId, orderId) {
      this.$emit('orders-list', productId, orderId);
    }
  },
};
</script>