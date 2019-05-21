<template>
  <v-list
    class="grey lighten-4 pt-0 pb-0"
    dense
    light
    transparent>
    <v-list-tile v-if="!product.enabled" light>
      <v-list-tile-content class="align-center"><strong class="red--text">Disabled product</strong></v-list-tile-content>
    </v-list-tile>
    <v-list-tile v-else-if="!product.unlimited && !available" class="soldOutBlock red">
      <v-list-tile-content class="align-center"><strong class="white--text">Sold out</strong></v-list-tile-content>
    </v-list-tile>
    <v-list-tile v-else-if="maxOrdersReached">
      <v-list-tile-content class="align-center">
        <strong class="red--text">
          Maximum {{ product.maxOrdersPerUser }} orders{{ periodicityLabelContext1 }} has been reached
        </strong>
      </v-list-tile-content>
    </v-list-tile>
    <v-expand-transition>
      <div v-if="hover">
        <v-list-tile v-if="product.receiverMarchand">
          <v-list-tile-content><strong>Offered By:</strong></v-list-tile-content>
          <v-list-tile-content class="productDetailText align-end">
            <div class="ellipsis">
              <profile-link
                :id="product.receiverMarchand.id"
                :space-id="product.receiverMarchand.spaceId"
                :url-id="product.receiverMarchand.spaceURLId"
                :type="product.receiverMarchand.type"
                :display-name="product.receiverMarchand.displayName"
                display-avatar />
            </div>
          </v-list-tile-content>
        </v-list-tile>
        <v-list-tile v-if="product.unlimited && userData.canEdit">
          <v-list-tile-content><strong>Sold:</strong></v-list-tile-content>
          <v-list-tile-content class="align-end">{{ product.purchased }}</v-list-tile-content>
        </v-list-tile>
        <v-list-tile v-else-if="!product.unlimited">
          <v-list-tile-content><strong>Available:</strong></v-list-tile-content>
          <v-list-tile-content v-if="userData.canEdit" class="align-end">{{ available }} / {{ product.totalSupply }}</v-list-tile-content>
          <v-list-tile-content v-else class="align-end">{{ available }}</v-list-tile-content>
        </v-list-tile>
        <template v-if="!maxOrdersReached && product.maxOrdersPerUser">
          <v-divider />
          <v-list-tile v-if="product.orderPeriodicity && product.userData.purchasedInCurrentPeriod">
            <v-list-tile-content><strong>My orders:</strong></v-list-tile-content>
            <v-list-tile-content class="align-end">{{ product.userData.purchasedInCurrentPeriod }}/{{ product.maxOrdersPerUser }}{{ periodicityLabelContext2 }}</v-list-tile-content>
          </v-list-tile>
          <v-list-tile v-else-if="!product.orderPeriodicity && product.userData.totalPurchased">
            <v-list-tile-content><strong>My orders:</strong></v-list-tile-content>
            <v-list-tile-content class="align-end">{{ product.userData.totalPurchased }}/{{ product.maxOrdersPerUser }}</v-list-tile-content>
          </v-list-tile>
          <v-list-tile v-else>
            <v-list-tile-content><strong>Max orders:</strong></v-list-tile-content>
            <v-list-tile-content class="align-end">{{ maxOrdersLabel }}</v-list-tile-content>
          </v-list-tile>
        </template>
      </div>
    </v-expand-transition>
  </v-list>
</template>

<script>
import ProfileLink from '../ProfileLink.vue';

export default {
  components: {
    ProfileLink,
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
    hover: {
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
    periodicityLabelContext1() {
      if(this.product.orderPeriodicity) {
        return ` per ${this.product.orderPeriodicityLabel}`;
      } else {
        return '';
      }
    },
    periodicityLabelContext2() {
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