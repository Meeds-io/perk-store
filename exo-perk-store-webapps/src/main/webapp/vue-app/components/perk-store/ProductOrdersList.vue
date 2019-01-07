<template>
  <v-container
    class="productOrdersParent border-box-sizing"
    fluid
    grid-list-md>
    <v-data-iterator
      :items="orders"
      content-tag="v-layout"
      row
      wrap>
      <v-flex
        slot="item"
        slot-scope="props"
        class="border-box-sizing"
        xs12
        sm6
        md4
        lg3>
        <product-order-detail
          :order="props.item"
          @changed="updateOrder(props.item, $event)"
          @loading="$emit('loading', $event)"
          @error="$emit('error', $event)" />
      </v-flex>
    </v-data-iterator>
  </v-container>
</template>

<script>
import ProductOrderDetail from './ProductOrderDetail.vue';

import {getOrderList} from '../../js/PerkStoreProductOrder.js';

export default {
  components: {
    ProductOrderDetail
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
      orders: [],
    };
  },
  watch: {
    product(value) {
      if(value) {
        this.init();
      } else {
        this.orders = [];
      }
    }
  },
  methods: {
    init() {
      if(!this.product) {
        return;
      }
      this.$emit('error', null);
      this.$emit('loading', true);
      return getOrderList(this.product.id)
      .then((orders) => this.orders = orders || [])
      .catch(e => this.$emit('error', e))
      .finally(() => this.$emit('loading', false));
    },
    updateOrder(order, newOrder) {
      Object.keys(order).forEach(key => order[key] = newOrder[key]);
    },
  },
}
</script>