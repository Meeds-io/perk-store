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
      <div class="popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="close">
        </a>
        <span class="PopupTitle popupTitle ellipsis">
          Perk store settings
        </span>
      </div>
      <v-card-text>
        <div v-if="error && !loading" class="alert alert-error v-content">
          <i class="uiIconError"></i>
          {{ error }}
        </div>

        <auto-complete
          ref="applicationAccessPermissionAutocomplete"
          input-label="Application access permissions (Optional)"
          input-placeholder="Select one or multiple entries"
          no-data-label="Search for a user or a space"
          multiple
          no-address
          big-field
          @item-selected="selectValue('accessPermissionsProfiles', $event)"
          @clear-selection="selectValue('accessPermissionsProfiles')" />

        <auto-complete
          ref="applicationManagersAutocomplete"
          input-label="Store managers (Optional)"
          input-placeholder="Select one or multiple entries"
          no-data-label="Search for a user or a space"
          multiple
          no-address
          big-field
          @item-selected="selectValue('managersProfiles', $event)"
          @clear-selection="selectValue('managersProfiles')" />

        <auto-complete
          ref="applicationProductCreationPermissionAutocomplete"
          input-label="Product creation permissions (Optional)"
          input-placeholder="Select one or multiple entries"
          no-data-label="Search for a user or a space"
          multiple
          no-address
          big-field
          @item-selected="selectValue('productCreationPermissionsProfiles', $event)"
          @clear-selection="selectValue('productCreationPermissionsProfiles')" />

        <v-text-field
          v-model="settingsToSave.symbol"
          :disabled="loading"
          :label="symbolInputLabel"
          name="symbol"
          placeholder="Select the used currency symbol (default wallet pricipal contract symbol)" />
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          :disabled="disablePayButton"
          :loading="loading"
          class="primary mr-1"
          @click="saveSettings">
          Save
        </v-btn>
        <button
          class="btn"
          :disabled="loading"
          @click="close">
          Close
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import AutoComplete from '../AutoComplete.vue';

import {saveSettings} from '../../js/PerkStoreSettings.js';

export default {
  components: {
    AutoComplete,
  },
  props: {
    walletSymbol: {
      type: String,
      default: function() {
        return null;
      },
    },
  },
  data() {
    return {
      dialog: false,
      loading: false,
      settingsToSave: {},
      error: null,
    };
  },
  computed: {
    symbolInputLabel() {
      return (this.walletSymbol && `Currency symbol (Wallet principal currency symbol: ${this.walletSymbol})`) || 'Currency symbol';
    },
  },
  watch: {
    error() {
      if(this.error) {
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
      if(!this.loading) {
        this.dialog = false;
      }
    },
    init() {
      if(this.settingsToSave) {
        this.$refs.applicationAccessPermissionAutocomplete.clear();
        if(this.settingsToSave.accessPermissionsProfiles) {
          this.$refs.applicationAccessPermissionAutocomplete.selectItems(this.settingsToSave.accessPermissionsProfiles);
        }
        this.$refs.applicationManagersAutocomplete.clear();
        if(this.settingsToSave.managersProfiles) {
          this.$refs.applicationManagersAutocomplete.selectItems(this.settingsToSave.managersProfiles);
        }
        this.$refs.applicationProductCreationPermissionAutocomplete.clear();
        if(this.settingsToSave.productCreationPermissionsProfiles) {
          this.$refs.applicationProductCreationPermissionAutocomplete.selectItems(this.settingsToSave.productCreationPermissionsProfiles);
        }
      }
    },
    selectValue(fieldName, identity) {
      if(!this.settingsToSave[fieldName]) {
        this.settingsToSave[fieldName] = [];
      }
      if(identity) {
        this.settingsToSave[fieldName].push(identity);
      } else if(this.settingsToSave[fieldName].length) {
        this.settingsToSave[fieldName] = [];
      }
    },
    saveSettings() {
      this.error = null;
      this.loading = true;

      return saveSettings(this.settingsToSave)
        .then(() => {
          this.$emit("saved");
          this.dialog = false;
          this.loading = false;
        })
        .catch(e => {
          console.debug("Save settings error", e);
          this.loading = false;
          this.error = e && e.message ? e.message : String(e);
        });
    },
  },
};
</script>
