<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-flex v-if="integrated" class="ms-4">
    <div class="headline mb-4">
      {{ product.title }}
      <div v-if="product.receiverMarchand" class="no-wrap caption">
        {{ $t('exoplatform.perkstore.label.offeredBy') }}
        <profile-link
          :id="product.receiverMarchand.id"
          :space-id="product.receiverMarchand.spaceId"
          :url-id="product.receiverMarchand.spaceURLId"
          :type="product.receiverMarchand.type"
          :display-name="product.receiverMarchand.displayName" />
      </div>
    </div>
    <div v-if="product.description" class="mb-4 me-4">
      {{ product.description }}
    </div>
  </v-flex>
  <v-list
    v-else
    class="grey lighten-4 pt-0 pb-0 productPreviewContent"
    dense
    light
    transparent>
    <v-list-item v-if="!product.enabled" light>
      <v-list-item-content class="align-center"><strong class="red--text">Disabled product</strong></v-list-item-content>
    </v-list-item>
    <v-list-item v-else-if="!product.unlimited && !available" class="soldOutBlock red">
      <v-list-item-content class="align-center"><strong class="white--text">Sold out</strong></v-list-item-content>
    </v-list-item>
    <v-list-item v-else-if="maxOrdersReached">
      <v-list-item-content class="align-center">
        <strong class="red--text">
          {{ $t('exoplatform.perkstore.label.maxOrdersIsReached', {0: product.maxOrdersPerUser, }) }}
        </strong>
      </v-list-item-content>
    </v-list-item>
    <v-expand-transition>
      <div v-if="hover">
        <v-list-item v-if="product.receiverMarchand">
          <v-list-item-content><strong>{{ $t('exoplatform.perkstore.label.offeredBy') }}:</strong></v-list-item-content>
          <v-list-item-content class="productDetailText align-end">
            <div class="text-truncate">
              <profile-link
                :id="product.receiverMarchand.id"
                :space-id="product.receiverMarchand.spaceId"
                :url-id="product.receiverMarchand.spaceURLId"
                :type="product.receiverMarchand.type"
                :display-name="product.receiverMarchand.displayName"
                display-avatar />
            </div>
          </v-list-item-content>
        </v-list-item>
        <v-list-item v-if="product.unlimited && userData.canEdit">
          <v-list-item-content><strong>{{ $t('exoplatform.perkstore.label.sold') }}:</strong></v-list-item-content>
          <v-list-item-content class="align-end">{{ product.purchased }}</v-list-item-content>
        </v-list-item>
        <v-list-item v-else-if="!product.unlimited">
          <v-list-item-content><strong>{{ $t('exoplatform.perkstore.label.available') }}:</strong></v-list-item-content>
          <v-list-item-content v-if="userData.canEdit" class="align-end">{{ available }} / {{ product.totalSupply }}</v-list-item-content>
          <v-list-item-content v-else class="align-end">{{ available }}</v-list-item-content>
        </v-list-item>
        <template v-if="!maxOrdersReached && product.maxOrdersPerUser">
          <v-divider />
          <v-list-item>
            <v-list-item-content>
              <strong>{{ $t('exoplatform.perkstore.label.myOrders') }}:</strong>
            </v-list-item-content>
            <template v-if="product.orderPeriodicity">
              <v-list-item-content v-if="userData.purchasedInCurrentPeriod" class="align-end">
                {{ $t(`exoplatform.perkstore.label.ordersInThis${product.orderPeriodicity}`, {0: userData.purchasedInCurrentPeriod, 1: product.maxOrdersPerUser}) }}
              </v-list-item-content>
              <v-list-item-content v-else class="align-end">
                {{ $t(`exoplatform.perkstore.label.ordersPerUserPer${product.orderPeriodicity}`, {0: product.maxOrdersPerUser}) }}
              </v-list-item-content>
            </template>
            <template v-else>
              <v-list-item-content v-if="userData.totalPurchased" class="align-end">
                {{ userData.totalPurchased }}/{{ product.maxOrdersPerUser }}
              </v-list-item-content>
              <v-list-item-content v-else class="align-end">
                {{ $t('exoplatform.perkstore.label.ordersPerUser', {0: product.maxOrdersPerUser}) }}
              </v-list-item-content>
            </template>
          </v-list-item>
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
    integrated: {
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