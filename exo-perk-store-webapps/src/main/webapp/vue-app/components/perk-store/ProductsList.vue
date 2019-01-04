<template>
  <v-card flat transparent>
    <v-container
      class="border-box-sizing"
      fluid
      grid-list-md>
      <v-layout
        row
        wrap
        class="productsListParent">
        <product-detail
          v-for="product in products"
          :key="product.id"
          :product="product"
          class="border-box-sizing" />
      </v-layout>
    </v-container>
  </v-card>
</template>

<script>
import ProductDetail from './ProductDetail.vue';

export default {
  components: {
    ProductDetail
  },
  props: {
    products: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data() {
    return {
      isAdministrator: true,
    };
  }
}
</script>