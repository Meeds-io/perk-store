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
  <v-app flat dark>
    <main>
      <v-container pa-0>
        <v-layout
          row
          wrap
          mx-0
          style="cursor: pointer">
          <v-flex
            d-flex
            sx12>
            <v-layout
              row
              ma-0
              class="white">
              <v-flex d-flex xs12>
                <v-card flat>
                  <v-card-text class="subtitle-2 text-color text-sub-title pa-2">
                    {{ $t('exoplatform.perkstore.title.myOrders') }}
                  </v-card-text>
                </v-card>
              </v-flex>
              <v-flex
                d-flex
                xs12
                justify-center
                pa-2>
                <a :href="url" class="display-1 font-weight-bold big-number">
                  {{ pendingOrdersSize }}<span class="mt-4 ms-1 product-label">{{ $t('exoplatform.perkstore.title.orders') }}</span>
                </a>
              </v-flex>
            </v-layout>
          </v-flex>
        </v-layout>
      </v-container>
    </main>
  </v-app>
</template>

<script>
import {getPendingOrdersSize} from '../../js/persktoreOrderAPI.js';

export default {
  data() {
    return {
      pendingOrdersSize: 0,
      perkstoreUrl: `${ eXo.env.portal.context }/${ eXo.env.portal.portalName }/perkstore?notProcessedOrders=true`,
    };
  },
  created() {
    this.retrievePendingOrdersCount();
  },
  methods: {
    retrievePendingOrdersCount() {
      return getPendingOrdersSize()
        .then(orders => {
          this.pendingOrdersSize = orders && orders.size || 0;
          return this.$nextTick();
        })
        .finally(() => this.$root.$emit('application-loaded'));
    }
  },
};
</script> 
