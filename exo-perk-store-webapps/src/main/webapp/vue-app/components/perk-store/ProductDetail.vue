<template>
  <v-flex class="productDetailContent">
    <v-hover>
      <v-card
        slot-scope="{ hover }"
        class="mx-auto elevation-3"
        color="grey lighten-4"
        max-width="600">
        <v-img
          v-if="product.img"
          :aspect-ratio="16/9"
          :src="product.img">
          <v-expand-transition>
            <div
              v-if="hover"
              class="d-flex transition-fast-in-fast-out darken-2 v-card--reveal white--text productDetailHover"
              style="height: 100%;">
              <product-detail-content
                :product="product"
                :available="available"
                :is-quantity-stock-type="isQuantityStockType" />
            </div>
          </v-expand-transition>
        </v-img>
        <product-detail-content
          v-else
          :product="product"
          :available="available"
          :is-quantity-stock-type="isQuantityStockType" />
        <v-progress-linear
          v-if="isQuantityStockType"
          v-model="boughtPercentage"
          :title="`${boughtPercentage}% articles sold`"
          color="red"
          class="mb-0 mt-0" />
        <v-card-text
          :class="isQuantityStockType || 'mt-2'"
          class="pt-4"
          style="position: relative;">
          <v-btn
            v-if="product.canEdit"
            title="Commands list"
            absolute
            color="secondary"
            class="white--text detailsButton"
            fab
            right
            top
            @click="$emit('commands-list', product)">
            <v-icon>fa-list-ul</v-icon>
          </v-btn>
          <v-btn
            v-if="product.canEdit"
            title="Edit product"
            absolute
            color="secondary"
            class="white--text editButton"
            fab
            right
            top
            @click="$emit('edit', product)">
            <v-icon>fa-pen</v-icon>
          </v-btn>
          <v-btn
            title="Buy"
            absolute
            class="white--text primary"
            :disabled="disabledBuy"
            fab
            right
            top
            @click="$emit('buy', product)">
            <v-icon>fa-shopping-cart</v-icon>
          </v-btn>
          <h3 class="mb-2 primary--text text-xs-center">{{ product.title }}</h3>
          <div class="font-weight-light title mb-2 text-xs-center">
            {{ product.description }}
          </div>
        </v-card-text>
      </v-card>
    </v-hover>
  </v-flex>
</template>

<script>
import ProductDetailContent from './ProductDetailContent.vue';

export default {
  components: {
    ProductDetailContent
  },
  props: {
    product: {
      type: Object,
      default: function() {
        return {};
      },
    },
  },
  data() {
    return {
      isAdministrator: true,
    };
  },
  computed: {
    disabledBuy() {
      return !this.product.enabled || (this.isQuantityStockType && !this.available) || (this.product.userOrders && this.product.userOrders.orderedInCurrentPeriod && this.product.userOrders.orderedInCurrentPeriod >= this.product.maxOrdersPerUser);
    },
    boughtPercentage() {
      return this.isQuantityStockType ? ((this.product.bought * 100) /this.product.totalSupply) : 0;
    },
    available() {
      return this.isQuantityStockType ? (this.product.totalSupply - this.product.bought) : 100;
    },
    isQuantityStockType() {
      return this.product.stockType === 'QUANTITY';
    }
  }
}
</script>