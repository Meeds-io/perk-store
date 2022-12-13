<template>
  <v-layout
    row
    class="justify-space-between">
    <div
      v-for="product in products.slice(0, 3)"
      :key="product.id">
      <perk-store-product-detail
        :product="product"
        :overview-display="true"
        class="border-box-sizing ps-3"
        @buy="$emit('buy', $event)" />
    </div>
  </v-layout>
</template>

<script>
import {getProductList} from '../../js/PerkStoreProduct.js';
export default {
  data() {
    return {
      products: []
    };
  },
  created() {
    this.getProductsList();
  },
  methods: {
    getProductsList() {
      return getProductList()
        .then((products) => this.products = products);
    }
  }
};
</script>