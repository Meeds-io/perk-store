<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2022 Meeds Association
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
  <v-flex class="searchNoResults justify-center d-flex border-box-sizing emptyChallenges">
    <div class="d-flex flex-column">
      <v-icon
        size="120"
        color="primary">
        {{ icon }}
      </v-icon>
      <div class="d-flex flex-column text-center text-sub-title">
        <span v-if="info" class="text-header-title text-light-color py-5">{{ info }}</span>
        <span
          v-if="infoMessage"
          v-html="emptyPerkstoreSummaryText"
          class="text-sub-title subtitle-1 text-light-color pb-2"></span>
      </div>
    </div>
  </v-flex>
</template>
<script>

export default {
  data() {
    return {
      emptyProductsActionName: 'perk-store-products-no-results',
    };
  },
  props: {
    icon: {
      type: String,
      default: ''  
    },
    info: {
      type: String,
      default: ''
    },
    infoMessage: {
      type: String,
      default: 'search.connector.label.products'
    },
    injectedLabelParam: {
      type: String,
      default: ''
    },
    clickCondition: {
      type: Boolean,
      default: false
    },
  },
  created() {
    document.addEventListener(this.emptyProductsActionName, this.noResultEvent);
  },
  beforeDestroy() {
    document.removeEventListener(this.emptyProductsActionName, this.noResultEvent);
  },
  computed: {
    emptyPerkstoreSummaryText() {
      const labelKey = this.clickCondition && this.infoMessage;
      if (this.injectedLabelParam.length > 0) {
        return this.$t(labelKey, {
          0: this.injectedLabelParam,
          1: `<a class="primary--text font-weight-bold" href="javascript:void(0)" onclick="document.dispatchEvent(new CustomEvent('${this.emptyProductsActionName}'))">`,
          2: '</a>',
        });
      } else {
        return this.$t(labelKey, {
          0: `<a class="primary--text font-weight-bold" href="javascript:void(0)" onclick="document.dispatchEvent(new CustomEvent('${this.emptyProductsActionName}'))">`,
          1: '</a>',
        });

      }

    },
  },
  methods: {
    noResultEvent() {
      this.$emit('no-result-event');
    },
  },
};
</script>