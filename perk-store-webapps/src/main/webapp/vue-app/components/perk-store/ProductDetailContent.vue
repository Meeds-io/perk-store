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
          {{ $t('exoplatform.perkstore.label.maxOrdersIsReached', {0: product.maxOrdersPerUser, }) }}
        </strong>
      </v-list-tile-content>
    </v-list-tile>
    <v-expand-transition>
      <div v-if="hover">
        <v-list-tile v-if="product.receiverMarchand">
          <v-list-tile-content><strong>{{ $t('exoplatform.perkstore.label.offeredBy') }}:</strong></v-list-tile-content>
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
          <v-list-tile-content><strong>{{ $t('exoplatform.perkstore.label.sold') }}:</strong></v-list-tile-content>
          <v-list-tile-content class="align-end">{{ product.purchased }}</v-list-tile-content>
        </v-list-tile>
        <v-list-tile v-else-if="!product.unlimited">
          <v-list-tile-content><strong>{{ $t('exoplatform.perkstore.label.available') }}:</strong></v-list-tile-content>
          <v-list-tile-content v-if="userData.canEdit" class="align-end">{{ available }} / {{ product.totalSupply }}</v-list-tile-content>
          <v-list-tile-content v-else class="align-end">{{ available }}</v-list-tile-content>
        </v-list-tile>
        <template v-if="!maxOrdersReached && product.maxOrdersPerUser">
          <v-divider />
          <v-list-tile>
            <v-list-tile-content>
              <strong>{{ $t('exoplatform.perkstore.label.myOrders') }}:</strong>
            </v-list-tile-content>
            <template v-if="product.orderPeriodicity">
              <v-list-tile-content v-if="userData.purchasedInCurrentPeriod" class="align-end">
                {{ $t(`exoplatform.perkstore.label.ordersInThis${product.orderPeriodicity}`, {0: userData.purchasedInCurrentPeriod, 1: product.maxOrdersPerUser}) }}
              </v-list-tile-content>
              <v-list-tile-content v-else class="align-end">
                {{ $t(`exoplatform.perkstore.label.ordersPerUserPer${product.orderPeriodicity}`, {0: product.maxOrdersPerUser}) }}
              </v-list-tile-content>
            </template>
            <template v-else>
              <v-list-tile-content v-if="userData.totalPurchased" class="align-end">
                {{ userData.totalPurchased }}/{{ product.maxOrdersPerUser }}
              </v-list-tile-content>
              <v-list-tile-content v-else class="align-end">
                {{ $t('exoplatform.perkstore.label.ordersPerUser', {0: product.maxOrdersPerUser}) }}
              </v-list-tile-content>
            </template>
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
  }
}
</script>