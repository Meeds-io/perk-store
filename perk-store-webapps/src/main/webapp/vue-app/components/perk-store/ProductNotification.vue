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
  <v-alert 
    v-if="hasNewProducts"
    v-model="alert"
    :timeout="120000" 
    type="success"
    dismissible>
      <template v-for="product in filteredProducts">
          <template v-if="product.lastModifiedDate">
            {{ $t('exoplatform.perkstore.info.productModified', {0: product.title}) }}
          </template>
          <template v-else>
            {{ $t('exoplatform.perkstore.info.productCreated', {0: product.title}) }}
          </template>
      </template>
  </v-alert>
</template>
<script>
export default {
  props: {
    products: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data () {
    return {
      alert: false,
      alertDisplayed: [],
    };
  },
  computed: {
    displayDivider() {
      return this.products && this.products.length > 1;
    },
    hasNewProducts() {
      return this.products && this.products.find(product => !product.lastModifiedDate);
    },
    filteredProducts() {
      return this.products.filter(product => this.alertDisplayed.indexOf(product.id) < 0).slice(0, Math.min(3, this.products.length));
    },
  },
  watch: {
    alert() {
      if (!this.alert) {
        this.$emit('refresh-list');
        this.products.forEach(product => this.alertDisplayed.push(product.id));
      }
    },
    products() {
      this.alert = this.products.filter(product => this.alertDisplayed.indexOf(product.id) < 0).length;
    },
  },
};
</script>
