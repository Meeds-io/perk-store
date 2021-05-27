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
  <v-dialog
    v-model="dialog"
    content-class="uiPopup with-overflow"
    class="settingsModal"
    width="600px"
    max-width="100vw"
    persistent
    @keydown.esc="close">
    <v-card class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="close">
        </a>
        <span class="ignore-vuetify-classes PopupTitle popupTitle text-truncate">
          {{ $t('exoplatform.perkstore.title.settingsModal') }}
        </span>
      </div>
      <v-card-text>
        <div v-if="error && !loading" class="alert alert-error v-content">
          <i class="uiIconError"></i>
          {{ error }}
        </div>

        <perk-store-auto-complete
          ref="applicationAccessPermissionAutocomplete"
          :input-label="$t('exoplatform.perkstore.label.applicationAccessPermissions')"
          :input-placeholder="$t('exoplatform.perkstore.label.applicationAccessPermissionsPlaceholder')"
          :no-data-label="$t('exoplatform.perkstore.label.applicationAccessPermissionsSearchPlaceholder')"
          multiple
          no-address
          big-field
          @item-selected="selectValue('accessPermissionsProfiles', $event)"
          @clear-selection="selectValue('accessPermissionsProfiles')" />

        <perk-store-auto-complete
          ref="applicationManagersAutocomplete"
          :input-label="$t('exoplatform.perkstore.label.applicationManagersPermissions')"
          :input-placeholder="$t('exoplatform.perkstore.label.applicationManagersPermissionsPlaceholder')"
          :no-data-label="$t('exoplatform.perkstore.label.applicationManagersPermissionsSearchPlaceholder')"
          multiple
          no-address
          big-field
          @item-selected="selectValue('managersProfiles', $event)"
          @clear-selection="selectValue('managersProfiles')" />

        <perk-store-auto-complete
          ref="applicationProductCreationPermissionAutocomplete"
          :input-label="$t('exoplatform.perkstore.label.applicationCreationPermissions')"
          :input-placeholder="$t('exoplatform.perkstore.label.applicationCreationPermissionsPlaceholder')"
          :no-data-label="$t('exoplatform.perkstore.label.applicationCreationPermissionsSearchPlaceholder')"
          multiple
          no-address
          big-field
          @item-selected="selectValue('productCreationPermissionsProfiles', $event)"
          @clear-selection="selectValue('productCreationPermissionsProfiles')" />
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          :disabled="disablePayButton"
          :loading="loading"
          class="primary me-1"
          @click="saveSettings">
          {{ $t('exoplatform.perkstore.button.save') }}
        </v-btn>
        <button
          class="ignore-vuetify-classes btn"
          :disabled="loading"
          @click="close">
          {{ $t('exoplatform.perkstore.button.close') }}
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import {saveSettings} from '../../js/PerkStoreSettings.js';

export default {
  data() {
    return {
      dialog: false,
      loading: false,
      settingsToSave: {},
      error: null,
    };
  },
  watch: {
    error() {
      if (this.error) {
        this.loading = false;
      }
    },
    dialog() {
      if (this.dialog) {
        this.error = null;
        this.loading = false;
        this.settingsToSave = Object.assign({}, window.perkStoreSettings);
        this.$nextTick().then(this.init);
      } else {
        this.$emit('closed');
      }
    },
  },
  methods: {
    open() {
      this.dialog = true;
    },
    close() {
      if (!this.loading) {
        this.dialog = false;
      }
    },
    init() {
      if (this.settingsToSave) {
        this.$refs.applicationAccessPermissionAutocomplete.clear();
        if (this.settingsToSave.accessPermissionsProfiles) {
          this.$refs.applicationAccessPermissionAutocomplete.selectItems(this.settingsToSave.accessPermissionsProfiles);
        }
        this.$refs.applicationManagersAutocomplete.clear();
        if (this.settingsToSave.managersProfiles) {
          this.$refs.applicationManagersAutocomplete.selectItems(this.settingsToSave.managersProfiles);
        }
        this.$refs.applicationProductCreationPermissionAutocomplete.clear();
        if (this.settingsToSave.productCreationPermissionsProfiles) {
          this.$refs.applicationProductCreationPermissionAutocomplete.selectItems(this.settingsToSave.productCreationPermissionsProfiles);
        }
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
    saveSettings() {
      this.error = null;
      this.loading = true;

      return saveSettings(this.settingsToSave)
        .then(() => {
          this.$emit('saved');
          this.dialog = false;
          this.loading = false;
        })
        .catch(e => {
          console.error('Save settings error', e);
          this.loading = false;
          this.error = e && e.message ? e.message : String(e);
        });
    },
  },
};
</script>
