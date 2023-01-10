<template>
  <v-app>
    <v-card class="border-radius settingsModal" flat>
      <div class="ignore-vuetify-classes popupHeader ClearFix titleSettings mt-3">
        <span class="ignore-vuetify-classes PopupTitle popupTitle text-truncate titlePerkStoreAdmin">
          {{ $t('exoplatform.perkstore.title.settingsModal') }}
        </span>
      </div>
      <v-card-text class="perkStoreSettings">
        <exo-notification-alert
          v-if="alert"
          :alert="alert"
          @dismissed="clear" />

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
      <v-card-actions class="mb-2">
        <v-spacer />
        <v-btn
          :disabled="disablePayButton"
          :loading="loading"
          class="primary me-1"
          @click="saveSettings">
          {{ $t('exoplatform.perkstore.button.save') }}
        </v-btn>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-app>
</template>

<script>
import {initSettings, saveSettings} from '../../js/PerkStoreSettings.js';

export default {
  data() {
    return {
      dialog: false,
      loading: false,
      settingsToSave: {},
      alert: null
    };
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.alert = null;
        this.loading = false;
        this.$nextTick().then(this.init);
      } else {
        this.$emit('closed');
      }
    },
  },
  created() {
    this.dialog = true;
    this.alert = null;
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
      return initSettings()
        .then(() => {
          this.settingsToSave = Object.assign({}, window.perkStoreSettings);
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
        });
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
      this.loading = true;
      this.alert = null;
      return saveSettings(this.settingsToSave)
        .then(() => {
          this.$emit('saved');
          this.dialog = false;
          this.loading = false;
          this.alert = {
            message: this.$t('exoplatform.perkstore.admin.settings.success'),
            type: 'success'
          };
        })
        .catch(e => {
          console.error('Save settings error', e);
          this.loading = false;
          this.alert = {
            message: this.$t('exoplatform.perkstore.admin.settings.error', {0: e && e.message ? e.message : String(e),}),
            type: 'error'
          };
        });
    },
  },
};
</script>
