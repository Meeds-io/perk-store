<template>
  <v-list
    v-if="!product.enabled"
    dense
    transparent>
    <v-list-tile>
      <v-list-tile-content class="align-center"><strong class="red--text">Disabled product</strong></v-list-tile-content>
    </v-list-tile>
  </v-list>
  <v-list
    v-else-if="!product.unlimited && !available"
    class="soldOutBlock"
    dense
    transparent>
    <v-list-tile>
      <v-list-tile-content class="align-center"><strong class="red--text"><strong class="red--text">Sold out</strong></strong></v-list-tile-content>
    </v-list-tile>
  </v-list>
  <v-list
    v-else-if="maxOrdersReached"
    dense
    transparent>
    <v-list-tile>
      <v-list-tile-content class="align-center"><strong class="red--text"><strong class="red--text">You maximum orders has been reached{{ periodicityLabel }}</strong></strong></v-list-tile-content>
    </v-list-tile>
  </v-list>
  <v-list
    v-else
    dense
    transparent>
    <v-list-tile v-if="!product.enabled">
      <v-list-tile-content><strong class="red--text">Disabled product</strong></v-list-tile-content>
    </v-list-tile>
    <v-list-tile>
      <v-list-tile-content><strong>Price:</strong></v-list-tile-content>
      <v-list-tile-content class="align-end">{{ product.price }} {{ symbol }}</v-list-tile-content>
    </v-list-tile>
    <v-list-tile v-if="!product.unlimited">
      <v-list-tile-content><strong>Available:</strong></v-list-tile-content>
      <v-list-tile-content v-if="userData.canEdit" class="align-end">{{ available }} / {{ product.totalSupply }}</v-list-tile-content>
      <v-list-tile-content v-else class="align-end">{{ available }}</v-list-tile-content>
    </v-list-tile>
    <v-list-tile v-if="product.maxOrdersPerUser">
      <v-list-tile-content><strong>Max orders:</strong></v-list-tile-content>
      <v-list-tile-content class="align-end">{{ maxOrdersLabel }}</v-list-tile-content>
    </v-list-tile>
    <template v-if="userData.purchasedInCurrentPeriod">
      <v-divider />
      <v-list-tile>
        <v-list-tile-content><strong>My orders in current period:</strong></v-list-tile-content>
        <v-list-tile-content class="align-end">{{ product.userData.purchasedInCurrentPeriod }}</v-list-tile-content>
      </v-list-tile>
    </template>
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
    maxOrdersReached: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
  },
  computed: {
    userData() {
      return (this.product && this.product.userData) || {};
    },
    periodicityLabel() {
      if(this.product.orderPeriodicity) {
        return ` this ${this.product.orderPeriodicityLabel}`;
      } else {
        return '';
      }
    },
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