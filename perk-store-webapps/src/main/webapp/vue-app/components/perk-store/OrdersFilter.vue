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
  <v-navigation-drawer 
    v-show="display"
    v-model="display"
    class="elevation-3 productOrderFilter"
    temporary
    absolute
    width="380">
    <v-toolbar dense>
      <v-toolbar-title class="text-truncate">{{ $t('exoplatform.perkstore.label.filterOrders') }}</v-toolbar-title>
      <v-spacer />
      <v-btn
        :title="$t('exoplatform.perkstore.button.search')"
        icon
        @click="searchOrders()">
        <v-icon dark>search</v-icon>
      </v-btn>
      <v-btn
        :title="$t('exoplatform.perkstore.button.save')"
        icon
        @click="saveOrderFilter">
        <v-icon>fa-save</v-icon>
      </v-btn>
    </v-toolbar>
    <v-container grid-list-xl class="border-box-sizing pr-1 productOrderFilterContent">
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
  </v-navigation-drawer>
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
      this.showFilters();
    },
    saveOrderFilter() {
      saveOrderFilter(this.filter);
      this.searchOrders();
    },
    showFilters() {
      this.display = !this.display;
    },
  },
};
</script>
