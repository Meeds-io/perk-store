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
  <exo-drawer
    ref="ordersFilterDrawer"
    class="productOrderFilter"
    :right="!$vuetify.rtl"
    width="380">
    <template slot="title">
      <span class="mx-2"> {{ $t('exoplatform.perkstore.label.filterOrders') }} </span>
    </template>
    <template slot="content">
      <v-container grid-list-xl class="border-box-sizing pe-1 productOrderFilterContent">
        <v-layout wrap column>
          <h4><v-checkbox v-model="filter.searchInDates" :label="$t('exoplatform.perkstore.label.searchInDates')" /></h4>
          <template v-if="filter.searchInDates">
            <v-divider />
            <v-date-picker
              v-model="selectedDate"
              type="date"
              class="border-box-sizing" />
          </template>
          <v-checkbox v-model="filter.notProcessed" :label="$t('exoplatform.perkstore.label.notCompletelyProcessed')" />
          <template v-if="!filter.notProcessed">
            <v-checkbox
              v-model="filter.ordered"
              :label="$t('exoplatform.perkstore.label.status.ordered')" />
            <v-checkbox
              v-model="filter.canceled"
              :label="$t('exoplatform.perkstore.label.status.canceled')" />
            <v-checkbox
              v-model="filter.error"
              :label="$t('exoplatform.perkstore.label.status.error')" />
            <v-checkbox
              v-model="filter.paid"
              :label="$t('exoplatform.perkstore.label.status.paid')" />
            <v-checkbox
              v-model="filter.partial"
              :label="$t('exoplatform.perkstore.label.status.partial')" />
            <v-checkbox
              v-model="filter.delivered"
              :label="$t('exoplatform.perkstore.label.status.delivered')" />
            <v-checkbox
              v-model="filter.refunded"
              :label="$t('exoplatform.perkstore.label.status.refunded')" />
            <v-checkbox
              v-model="filter.fraud"
              :label="$t('exoplatform.perkstore.label.status.fraud')" />
          </template>
        </v-layout>
      </v-container>
    </template>
    <template slot="footer">
      <div class="VuetifyApp flex d-flex">
        <v-spacer />
        <v-btn
          :title="$t('exoplatform.perkstore.button.cancel')"
          class="btn mx-1"
          @click="closeFilters">
          <span>{{ $t('exoplatform.perkstore.button.cancel') }}</span>
        </v-btn>

        <v-btn
          :title="$t('exoplatform.perkstore.button.save')"
          class="btn btn-primary"
          @click="saveFilter">
          <span>{{ $t('exoplatform.perkstore.button.save') }}</span>
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
import {saveOrderFilter} from '../../js/PerkStoreSettings.js';

export default {
  props: {
    filter: {
      type: Object,
      default: function() {
        return {};
      },
    },
  },
  data() {
    return {
      display: false,
      selectedDate: null,
    };
  },
  watch: {
    selectedDate() {
      if (this.selectedDate) {
        this.filter.selectedDate = new Date(this.selectedDate).getTime();
      } else {
        this.filter.selectedDate = 0;
      }
    },
  },
  methods: {
    searchOrders() {
      this.$emit('search');
      this.closeFilters();
    },
    saveFilter() {
      saveOrderFilter(this.filter);
      this.searchOrders();
    },
    showFilters() {
      this.$refs.ordersFilterDrawer.open();
    },
    closeFilters() {
      this.$refs.ordersFilterDrawer.close();
    },
  },
};
</script>
