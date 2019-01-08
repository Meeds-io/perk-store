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
          :src="product.img"
          class="productCardHeader">
          <v-expand-transition>
            <div
              v-if="hover"
              class="d-flex transition-fast-in-fast-out darken-2 v-card--reveal white--text productDetailHover"
              style="height: 100%;">
              <product-detail-content
                :product="product"
                :symbol="symbol"
                :available="available" />
            </div>
          </v-expand-transition>
        </v-img>
        <product-detail-content
          v-else
          :product="product"
          :symbol="symbol"
          :available="available"
          class="productCardHeader" />
        <v-progress-linear
          v-if="!product.unlimited"
          v-model="boughtPercentage"
          :title="`${boughtPercentage}% articles sold`"
          color="red"
          class="mb-0 mt-0" />
        <v-card-text
          :class="product.unlimited && 'mt-2'"
          class="pt-2"
          style="position: relative;">
          <v-btn
            v-if="product.canEdit"
            title="Orders list"
            absolute
            color="secondary"
            class="white--text orderListButton"
            fab
            right
            top
            @click="$emit('orders-list', product)">
            <v-badge
              color="red"
              right
              overlap>
              <span
                v-if="product.notTreatedOrders"
                slot="badge"
                class="orderListBadge">
                {{ product.notTreatedOrders }}
              </span>
              <v-icon>fa-list-ul</v-icon>
            </v-badge>
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
        </v-card-text>
        <v-card-title class="pt-0 pb-0">
          <h3 class="mb-2 primary--text">
            {{ product.title }}
          </h3>
          <v-spacer />
          <h3 class="mb-2">
            {{ product.price }} {{ symbol }}
          </h3>
        </v-card-title>
        <v-card-text class="productCardFooter">
          <div :title="product.description" class="font-weight-light title mb-2 text-xs-center truncate8">
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
    symbol: {
      type: String,
      default: function() {
        return '';
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
      return !this.product.enabled || (!this.product.unlimited && !this.available) || (this.product.userOrders && this.product.userOrders.orderedInCurrentPeriod && this.product.userOrders.orderedInCurrentPeriod >= this.product.maxOrdersPerUser);
    },
    boughtPercentage() {
      return !this.product.unlimited ? ((this.product.bought * 100) /this.product.totalSupply) : 0;
    },
    available() {
      return !this.product.unlimited ? (this.product.totalSupply - this.product.bought) : 100;
    },
  }
}
</script>