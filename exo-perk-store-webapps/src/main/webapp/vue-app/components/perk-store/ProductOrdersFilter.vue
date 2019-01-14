<template>
  <v-navigation-drawer 
    v-show="display"
    v-model="display"
    class="elevation-3 productOrderFilter"
    width="380">
    <v-toolbar flat>
      <v-toolbar-title>
        <h3>Filter orders</h3>
      </v-toolbar-title>
      <v-spacer />
      <v-toolbar-items class="hidden-sm-and-down">
        <v-btn
          class="mr-1 primary"
          icon
          title="Search"
          @click="$emit('search')">
          <v-icon>search</v-icon>
        </v-btn>
        <v-btn
          class="mr-1 secondary"
          title="Save"
          icon
          @click="saveOrderFilter">
          <v-icon>fa-save</v-icon>
        </v-btn>
      </v-toolbar-items>
    </v-toolbar>
    <v-container grid-list-xl class="border-box-sizing pr-1">
      <v-layout wrap column>
        <h4><v-checkbox v-model="filter.searchInDates" label="Search in dates" /></h4>
        <template v-if="filter.searchInDates">
          <v-divider />
          <v-date-picker
            v-model="selectedDate"
            type="date" />
        </template>
        <v-checkbox v-model="filter.notProcessed" label="Not completely processed" />
        <v-checkbox
          v-if="!filter.notProcessed"
          v-model="filter.ordered"
          label="Ordered" />
        <v-checkbox
          v-if="!filter.notProcessed"
          v-model="filter.canceled"
          label="Canceled" />
        <v-checkbox
          v-if="!filter.notProcessed"
          v-model="filter.error"
          label="Order went wrong" />
        <v-checkbox
          v-if="!filter.notProcessed"
          v-model="filter.paid"
          label="Paid" />
        <v-checkbox
          v-if="!filter.notProcessed"
          v-model="filter.delivered"
          label="Delivered" />
        <v-checkbox
          v-if="!filter.notProcessed"
          v-model="filter.refunded"
          label="Refunded" />
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
      if(this.selectedDate) {
        this.filter.selectedDate = new Date(this.selectedDate).getTime();
      } else {
        this.filter.selectedDate = 0;
      }
    },
  },
  methods: {
    saveOrderFilter() {
      saveOrderFilter(this.filter);
    },
    showFilters() {
      this.display = !this.display;
    },
  },
};
</script>
