<template>
  <v-card flat class="transparent">
    <v-expand-transition name="fade" appear>
      <v-flex v-show="selectedProduct" class="productDetailContainer">
        <product-detail-maximized
          :product="selectedProduct || {}"
          :symbol="symbol"
          :need-password="needPassword"
          :wallet-loading="walletLoading"
          :wallet-enabled="walletEnabled"
          class="border-box-sizing"
          @ordered="$emit('ordered', $event)"
          @close="$emit('close')" />
      </v-flex>
    </v-expand-transition>
    <template v-if="!selectedProduct">
      <v-container
        v-if="products && products.length"
        class="border-box-sizing productsListParentContainer"
        fluid
        grid-list-md>
        <v-layout
          row
          wrap
          class="productsListParent">
          <create-product-button @create-product="$emit('create-product')" />
          <template v-for="product in products">
            <product-detail
              :key="product.id"
              :product="product"
              :symbol="symbol"
              :wallet-loading="walletLoading"
              :wallet-enabled="walletEnabled"
              class="border-box-sizing xs12 sm6 md4 lg3 xl2"
              @product-details="$emit('product-details', $event)"
              @orders-list="displayOrdersList"
              @edit="$emit('edit', $event)"
              @buy="$emit('buy', $event)" />
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
import ProductDetail from './ProductDetail.vue';
import CreateProductButton from './CreateProductButton.vue';
import ProductDetailMaximized from './ProductDetailMaximized.vue';

export default {
  components: {
    ProductDetail,
    CreateProductButton,
    ProductDetailMaximized,
  },
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
  },
  methods: {
    displayOrdersList(productId, orderId, currentUserOrders) {
      this.$emit('orders-list', productId, orderId, currentUserOrders);
    }
  },
}
</script>