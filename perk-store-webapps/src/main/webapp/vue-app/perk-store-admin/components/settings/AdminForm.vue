<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2023 Meeds Association contact@meeds.io

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
  <div class="perkStoreSettings">
    <div class="subtitle-1 mt-4 mb-2 text-color">
      {{ $t('perkstore.administration.accessLabel') }}
    </div>
    <perk-store-auto-complete
      ref="applicationAccessPermissionAutocomplete"
      :input-placeholder="$t('perkstore.administration.anyone')"
      :no-data-label="$t('exoplatform.perkstore.label.applicationAccessPermissionsSearchPlaceholder')"
      multiple
      no-address
      big-field
      @item-selected="selectValue('accessPermissionsProfiles', $event)"
      @clear-selection="selectValue('accessPermissionsProfiles')" />
    <div class="subtitle-1 mt-4 mb-2 text-color">
      {{ $t('perkstore.administration.manageLabel') }}
    </div>
    <perk-store-auto-complete
      ref="applicationManagersAutocomplete"
      :input-placeholder="$t('perkstore.administration.anyone')"
      :no-data-label="$t('exoplatform.perkstore.label.applicationManagersPermissionsSearchPlaceholder')"
      multiple
      no-address
      big-field
      @item-selected="selectValue('managersProfiles', $event)"
      @clear-selection="selectValue('managersProfiles')" />
    <div class="subtitle-1 mt-4 mb-2 text-color">
      {{ $t('perkstore.administration.createLabel') }}
    </div>
    <perk-store-auto-complete
      ref="applicationProductCreationPermissionAutocomplete"
      :input-placeholder="$t('perkstore.administration.anyone')"
      :no-data-label="$t('exoplatform.perkstore.label.applicationCreationPermissionsSearchPlaceholder')"
      multiple
      no-address
      big-field
      @item-selected="selectValue('productCreationPermissionsProfiles', $event)"
      @clear-selection="selectValue('productCreationPermissionsProfiles')" />
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      settingsToSave: {},
      originalSettings: {},
    };
  },
  computed: {
    modified() {
      return JSON.stringify(this.originalSettings) !== JSON.stringify(this.settingsToSave);
    },
  },
  watch: {
    loading() {
      this.$emit('loading', this.loading);
    },
    modified() {
      this.$emit('modified', this.modified);
    },
    etherAmount() {
      this.$emit('ether-amount', this.etherAmount);
    },
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      return this.$perkStoreSettings.initSettings()
        .then(() => {
          this.settingsToSave = JSON.parse(JSON.stringify(window.perkStoreSettings));
          this.originalSettings = JSON.parse(JSON.stringify(this.settingsToSave));
          this.setSettings(this.settingsToSave);
          return this.$nextTick();
        })
        .then(() => {
          window.setTimeout(() => {
            this.originalSettings = JSON.parse(JSON.stringify(this.settingsToSave));
          }, 200);
        });
    },
    setSettings(settings) {
      if (settings) {
        //
        this.$refs.applicationAccessPermissionAutocomplete.clear();
        this.$refs.applicationAccessPermissionAutocomplete.selectItems(settings.accessPermissionsProfiles || []);
        //
        this.$refs.applicationManagersAutocomplete.clear();
        this.$refs.applicationManagersAutocomplete.selectItems(settings.managersProfiles || []);
        //
        this.$refs.applicationProductCreationPermissionAutocomplete.clear();
        this.$refs.applicationProductCreationPermissionAutocomplete.selectItems(settings.productCreationPermissionsProfiles || []);
      }
    },
    selectValue(fieldName, identity) {
      if (!this.settingsToSave[fieldName]) {
        this.settingsToSave[fieldName] = [];
      }
      if (identity) {
        this.settingsToSave[fieldName].push(identity);
      } else if (this.settingsToSave[fieldName].length) {
        this.settingsToSave[fieldName] = [];
      }
    },
    reset() {
      this.setSettings({});
    },
    save() {
      this.loading = true;
      return this.$perkStoreSettings.saveSettings(this.settingsToSave)
        .then(() => {
          this.$emit('saved');
          this.$root.$emit('alert-message', this.$t('exoplatform.perkstore.admin.settings.success'), 'success');
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('exoplatform.perkstore.admin.settings.error', 'error')))
        .finally(() => this.loading = false);
    },
  },
};
</script>
