<template>
  <v-list dense transparent>
    <v-list-tile v-if="!product.enabled">
      <v-list-tile-content><strong class="red--text">Disabled product</strong></v-list-tile-content>
    </v-list-tile>
    <v-list-tile>
      <v-list-tile-content><strong>Price:</strong></v-list-tile-content>
      <v-list-tile-content class="align-end">{{ product.price }} {{ symbol }}</v-list-tile-content>
    </v-list-tile>
    <v-list-tile v-if="!product.illimited">
      <v-list-tile-content><strong>Available:</strong></v-list-tile-content>
      <v-list-tile-content class="align-end">{{ available }}</v-list-tile-content>
    </v-list-tile>
    <v-list-tile v-if="!product.illimited">
      <v-list-tile-content><strong>Total supply:</strong></v-list-tile-content>
      <v-list-tile-content class="align-end">{{ product.totalSupply }}</v-list-tile-content>
    </v-list-tile>
    <v-list-tile v-if="product.maxOrdersPerUser">
      <v-list-tile-content><strong>Max orders:</strong></v-list-tile-content>
      <v-list-tile-content class="align-end">{{ maxOrdersLabel }}</v-list-tile-content>
    </v-list-tile>
    <v-divider v-if="product.userOrders" />
    <v-list-tile v-if="product.userOrders && product.userOrders.orderedInCurrentPeriod">
      <v-list-tile-content><strong>My orders in current period:</strong></v-list-tile-content>
      <v-list-tile-content class="align-end">{{ product.userOrders.orderedInCurrentPeriod }}</v-list-tile-content>
    </v-list-tile>
    <v-list-tile v-if="product.userOrders && product.userOrders.totalOrders">
      <v-list-tile-content><strong>Total orders:</strong></v-list-tile-content>
      <v-list-tile-content class="align-end">{{ product.userOrders.totalOrders }}</v-list-tile-content>
    </v-list-tile>
    <v-list-tile v-if="product.userOrders && product.userOrders.notDeliveredOrders">
      <v-list-tile-content><strong>Orders not delivered yet:</strong></v-list-tile-content>
      <v-list-tile-content class="align-end">{{ product.userOrders.notDeliveredOrders }}</v-list-tile-content>
    </v-list-tile>
  </v-list>
</template>

<script>
export default {
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
    available: {
      type: Number,
      default: function() {
        return 0;
      },
    },
  },
  computed: {
    maxOrdersLabel() {
      if(!this.product.maxOrdersPerUser) {
        return '';
      }
      if(this.product.orderPeriodicity) {
        return `${this.product.maxOrdersPerUser} per user per ${this.product.orderPeriodicityLabel}`;
      } else {
        return `${this.product.maxOrdersPerUser} per user`;
      }
    }
  }
}
</script>