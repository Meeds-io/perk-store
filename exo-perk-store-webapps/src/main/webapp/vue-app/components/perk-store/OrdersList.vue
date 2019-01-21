<template>
  <v-layout row class="border-box-sizing mr-0 ml-0">
    <orders-filter
      ref="productOrdersFilter"
      :filter="ordersFilter"
      @search="searchOrders" />
    <v-container
      class="productOrdersParent border-box-sizing mt-0"
      fluid
      grid-list-md>
      <v-card
        v-if="!selectedOrderId && displayFilterDetails && filterDescriptionLabels && filterDescriptionLabels.length"
        class="transparent mb-0 mt-0 pt-0"
        flat>
        <v-card-title class="mt-0 pt-0">
          <v-spacer />
          <div class="no-wrap ellipsis">
            Selected filter:
            <v-chip v-for="filterDescription in filterDescriptionLabels" :key="filterDescription">
              {{ filterDescription }}
            </v-chip>
          </div>
          <v-spacer />
        </v-card-title>
      </v-card>
      <v-data-iterator
        :items="filteredOrders"
        :no-data-text="loading ? '': 'No orders'"
        content-tag="v-layout"
        content-class="mt-0 mb-0"
        hide-actions
        row
        wrap>
        <v-flex
          slot="item"
          slot-scope="props"
          class="border-box-sizing pt-0"
          xs12
          sm6
          md4
          lg3
          xl2>
          <order-detail
            :order="props.item"
            :product="product"
            :symbol="symbol"
            @init-wallet="$emit('init-wallet')"
            @display-product="$emit('display-product', $event)"
            @changed="updateOrder(props.item, $event)"
            @loading="loading = true"
            @error="$emit('error', $event)" />
        </v-flex>
        <v-flex
          v-if="loading || displayLoadMoreButton"
          slot="footer"
          class="mt-2 text-xs-center"
          dense
          flat>
          <v-btn
            v-if="displayLoadMoreButton"
            class="primary--text"
            flat
            :loading="loading"
            :disabled="loading"
            @click="loadMore">
            Load more
          </v-btn>
          <v-progress-circular
            v-else-if="loading"
            color="primary"
            class="mb-2"
            indeterminate />
        </v-flex>
      </v-data-iterator>
      <order-notification
        :orders="newAddedOrders"
        @refresh-list="addNewOrdersToList" />
    </v-container>
  </v-layout>
</template>

<script>
import OrderDetail from './OrderDetail.vue';
import OrdersFilter from './OrdersFilter.vue';
import OrderNotification from './OrderNotification.vue';

import {getOrderList} from '../../js/PerkStoreProductOrder.js';
import {getDefaultOrderFilter} from '../../js/PerkStoreSettings.js';

export default {
  components: {
    OrderDetail,
    OrdersFilter,
    OrderNotification,
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
    selectedOrderId: {
      type: Number,
      default: function() {
        return 0;
      },
    },
    symbol: {
      type: String,
      default: function() {
        return "";
      },
    },
  },
  data() {
    return {
      loading: false,
      pageSize: 12,
      limit: 12,
      limitReached: false,
      filterDescriptionLabels: [],
      displayFilterDetails: false,
      orders: [],
      newAddedOrders: [],
    };
  },
  computed: {
    selectedOrdersFilter() {
      return (!this.selectedOrderId && this.ordersFilter) || getDefaultOrderFilter();
    },
    filteredOrders() {
      const order = this.selectedOrderId && this.orders.find(order => order && order.id === this.selectedOrderId);
      if (order) {
        return [order];
      } else {
        return this.orders;
      }
    },
    displayLoadMoreButton() {
      return !this.limitReached && this.orders.length && this.orders.length % this.pageSize === 0;
    }
  },
  watch: {
    product(value, oldValue) {
      if(value && (!oldValue || value.id !== oldValue.id)) {
        this.limit = 12;
        this.limitReached = false;
        this.newAddedOrders = [];
        this.init();
      }
    }
  },
  created() {
    document.addEventListener('exo.addons.perkstore.order.createOrModify', this.updateOrderFromWS);
  },
  methods: {
    init() {
      this.$emit('error', null);

      this.computeDisplayFilterDetails();
      this.computeDescriptionLabels();

      const initialOrdersLength = this.orders.length;

      this.loading = true;

      return getOrderList(this.product && this.product.id, this.selectedOrdersFilter, this.selectedOrderId, this.limit)
        .then((orders) => {
          this.orders = orders || [];
          this.limitReached = this.orders.length <= initialOrdersLength || this.orders.length < this.limit;
        })
        .catch(e => {
          console.debug("Error while listing orders", e);
          this.$emit('error', e && e.message ? e.message : String(e));
        }).finally(() => {
          this.loading = false;
        });
    },
    searchOrders() {
      return this.init();
    },
    computeDisplayFilterDetails() {
      this.displayFilterDetails = false;

      if(!this.selectedOrdersFilter) {
        this.displayFilterDetails = false;
        return;
      }
      if(this.selectedOrdersFilter.notProcessed) {
        this.displayFilterDetails = true;
        return;
      }
      if(this.selectedOrdersFilter.searchInDates && this.selectedOrdersFilter.selectedDate) {
        this.displayFilterDetails = true;
        return;
      }

      const selectedOrdersFilter = {
        ordered: this.selectedOrdersFilter.ordered,
        canceled: this.selectedOrdersFilter.canceled,
        paid: this.selectedOrdersFilter.paid,
        partial: this.selectedOrdersFilter.partial,
        delivered: this.selectedOrdersFilter.delivered,
        refunded: this.selectedOrdersFilter.refunded,
      };

      // Check if all details are checked by default
      this.displayFilterDetails = !Object.values(selectedOrdersFilter).every(value => value);
    },
    computeDescriptionLabels() {
      this.filterDescriptionLabels = [];
      if(this.selectedOrdersFilter.searchInDates && this.selectedOrdersFilter.selectedDate) {
        const dateString = new Date(this.selectedOrdersFilter.selectedDate).toLocaleString().substring(0, 10);
        this.filterDescriptionLabels.push(`DATE: ${dateString}`);
      }
      if(this.selectedOrdersFilter.notProcessed) {
        this.filterDescriptionLabels.push("NOT PROCESSED");
      } else {
        if (this.selectedOrdersFilter.ordered) {
          this.filterDescriptionLabels.push("ORDERED");
        }
        if (this.selectedOrdersFilter.canceled) {
          this.filterDescriptionLabels.push("CANCELED");
        }
        if (this.selectedOrdersFilter.error) {
          this.filterDescriptionLabels.push("ERROR");
        }
        if (this.selectedOrdersFilter.paid) {
          this.filterDescriptionLabels.push("PAID");
        }
        if (this.selectedOrdersFilter.partial) {
          this.filterDescriptionLabels.push("PARTIAL");
        }
        if (this.selectedOrdersFilter.delivered) {
          this.filterDescriptionLabels.push("DELIVERED");
        }
        if (this.selectedOrdersFilter.refunded) {
          this.filterDescriptionLabels.push("REFUNDED");
        }
      }
    },
    showFilters() {
      this.$refs.productOrdersFilter.showFilters();
    },
    updateOrder(order, newOrder, orders) {
      if (!orders) {
        orders = this.orders;
      }
      Object.assign(order, newOrder);
    },
    loadMore() {
      this.limit += this.pageSize;
      return this.init();
    },
    updateOrderFromWS(event) {
      const wsMessage = event.detail;
      if(this.orders && this.orders.length && wsMessage.productorder && wsMessage.productorder.id) {
        const order = this.orders.find(order => order && order.id === wsMessage.productorder.id);
        if(order) {
          this.updateOrder(order, wsMessage.productorder);
          this.updateOrder(order, wsMessage.productorder, this.newAddedOrders);
        } else if(wsMessage.productorder.productId === this.product.id) {
          if (this.product.userData && this.product.userData.canEdit) {
            if(wsMessage.productorder.status.toUpperCase() === 'ORDERED' && !this.newAddedOrders.find(order => order.id === wsMessage.productorder.id)) {
              this.newAddedOrders.unshift(wsMessage.productorder);
            }
          } else {
            this.orders.unshift(wsMessage.productorder);
          }
        }
      }
    },
    addNewOrdersToList() {
      this.newAddedOrders.reverse().forEach(order => {
        if (!this.orders.find(existingOrder => existingOrder.id === order.id)) {
          this.orders.unshift(order);
        }
      });
      this.newAddedOrders.splice(0, this.newAddedOrders.length);
    },
  },
}
</script>