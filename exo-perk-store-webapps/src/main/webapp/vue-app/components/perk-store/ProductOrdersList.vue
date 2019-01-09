<template>
  <v-layout row class="border-box-sizing mr-0 ml-0">
    <product-orders-filter
      ref="productOrdersFilter"
      :filter="ordersFilter"
      @search="searchOrders" />
    <v-container
      class="productOrdersParent border-box-sizing mt-0"
      fluid
      grid-list-md>
      <v-data-iterator
        :items="orders"
        content-tag="v-layout"
        hide-actions
        row
        wrap>
        <v-flex
          slot="item"
          slot-scope="props"
          class="border-box-sizing"
          xs12
          sm6
          md4
          lg3
          xl2>
          <product-order-detail
            :order="props.item"
            :product="product"
            @changed="updateOrder(props.item, $event)"
            @loading="$emit('loading', $event)"
            @error="$emit('error', $event)" />
        </v-flex>
        <v-flex
          v-if="displayLoadMoreButton"
          slot="footer"
          class="mt-2 text-xs-center"
          dense
          flat>
          <v-btn
            class="primary--text"
            flat
            @click="loadMore">
            Load more
          </v-btn>
        </v-flex>
      </v-data-iterator>
    </v-container>
  </v-layout>
</template>

<script>
import ProductOrderDetail from './ProductOrderDetail.vue';
import ProductOrdersFilter from './ProductOrdersFilter.vue';

import {getOrderList} from '../../js/PerkStoreProductOrder.js';

export default {
  components: {
    ProductOrderDetail,
    ProductOrdersFilter,
  },
  props: {
    product: {
      type: Object,
      default: function() {
        return {};
      },
    },
    ordersFilter: {
      type: Object,
      default: function() {
        return {};
      },
    },
  },
  data() {
    return {
      pageSize: 20,
      limit: 20,
      limitReached: false,
      orders: [],
    };
  },
  computed: {
    displayLoadMoreButton() {
      return !this.limitReached && this.orders.length && this.orders.length % this.pageSize === 0;
    }
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
      const initialOrdersLength = this.orders.length;
      return getOrderList(this.product.id, this.ordersFilter, this.limit)
        .then((orders) => {
          this.orders = orders || [];
          this.orders.forEach(order => {
            if(order.transactionHash) {
              order.transactionLink = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/wallet?hash=${order.transactionHash}&principal=true`;
            }
          });
          this.limitReached = this.orders.length <= initialOrdersLength || this.orders.length < this.limit;
        })
        .catch(e => this.$emit('error', e))
        .finally(() => this.$emit('loading', false));
    },
    searchOrders() {
      return this.init();
    },
    showFilters() {
      this.$refs.productOrdersFilter.showFilters();
    },
    updateOrder(order, newOrder) {
      Object.keys(order).forEach(key => order[key] = newOrder[key]);
    },
    loadMore() {
      this.limit += this.pageSize;
      return this.init();
    },
  },
}
</script>