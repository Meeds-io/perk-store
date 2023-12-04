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
  <v-flex :id="id" class="contactAutoComplete">
    <v-autocomplete
      ref="selectAutoComplete"
      v-model="selectedValue"
      :rules="rules"
      :items="items"
      :loading="isLoadingSuggestions"
      :search-input.sync="searchTerm"
      :label="inputLabel"
      :disabled="disabled"
      :placeholder="inputPlaceholder"
      :content-class="`contactAutoCompleteContent ${bigField && 'bigContactAutoComplete'}`"
      :filter="filterIgnoredItems"
      :multiple="multiple"
      :required="required"
      class="contactAutoComplete ma-0 pa-0"
      max-width="100%"
      item-text="name"
      item-value="id_type"
      hide-details
      hide-selected
      chips
      cache-items
      dense
      flat
      @update:search-input="searchTerm = $event"
      @click.stop>
      <template slot="no-data">
        <v-list-item>
          <v-list-item-title v-if="noDataLabel">
            {{ noDataLabel }}
          </v-list-item-title>
          <v-list-item-title v-else>
            {{ $t('exoplatform.perkstore.label.autocompletePlaceholder') }}
          </v-list-item-title>
        </v-list-item>
      </template>

      <template slot="selection" slot-scope="{item, selected}">
        <v-chip
          :input-value="selected"
          :title="addressLoad === 'error' ? $t('exoplatform.perkstore.warning.recipientDoesntHaveWallet') : ''"
          class="autocompleteSelectedItem"
          @update:active="selectSingleItem(item)">
          <v-progress-circular
            v-if="addressLoad === 'loading'"
            indeterminate
            color="white"
            class="me-2" />
          <v-icon
            v-else-if="addressLoad === 'error'"
            alt="Invalid address"
            class="me-2"
            color="red"
            size="15">
            warning
          </v-icon>
          <span>
            {{ item.name }}
            <a
              href="#"
              :title="$t('exoplatform.perkstore.button.delete')"
              class="remove"
              @click="remove(item)">
              <i class="uiIconClose uiIconLightGray"></i>
            </a>
          </span>
        </v-chip>
      </template>

      <template slot="item" slot-scope="data">
        <template v-if="typeof data.item !== 'object'">
          <v-list-item-content v-text="data.item" />
        </template>
        <v-list-item-avatar
          v-else-if="data.item.avatar"
          size="20">
          <img :src="data.item.avatar" :alt="$t('exoplatform.perkstore.img.alt', {0: data.item.name})">
        </v-list-item-avatar>
        <v-list-item-title v-text="data.item.name" />
      </template>
    </v-autocomplete>
  </v-flex>
</template>

<script>
export default {
  props: {
    rules: {
      type: Array,
      default: function() {
        return [];
      },
    },
    required: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    inputLabel: {
      type: String,
      default: function() {
        return null;
      },
    },
    inputPlaceholder: {
      type: String,
      default: function() {
        return null;
      },
    },
    noDataLabel: {
      type: String,
      default: function() {
        return null;
      },
    },
    noAddress: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    onlyUsers: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    multiple: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    disabled: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    bigField: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    ignoreItems: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data() {
    return {
      items: [],
      id: `AutoComplete${parseInt(Math.random() * 10000)
        .toString()
        .toString()}`,
      selectedValue: null,
      searchTerm: null,
      address: null,
      isLoadingSuggestions: false,
      addressLoad: '',
      currentUserItem: null,
      error: null,
    };
  },
  watch: {
    searchTerm(value) {
      if (value && value.length) {
        this.isLoadingSuggestions = true;
        return this.$perkStoreIdentityRegistry.searchContact(value, this.onlyUsers, true).then((data) => {
          this.items = data;
          if (!this.items) {
            if (this.currentUserItem) {
              this.items = [this.currentUserItem];
            } else {
              this.items = [];
            }
          } else if (this.currentUserItem) {
            this.items.push(this.currentUserItem);
          }
        })
          .finally(() => {
            this.isLoadingSuggestions = false;
          });
      } else if (this.currentUserItem) {
        this.items = [this.currentUserItem];
      } else {
        this.items = [];
      }
    },
    selectedValue() {
      this.$refs.selectAutoComplete.isFocused = false;
      this.addressLoad = 'loading';
      const selectedValue = this.selectedValue;
      this.$emit('clear-selection');
      if (selectedValue && selectedValue.length) {
        if (this.multiple) {
          selectedValue.forEach((value) => this.emitSelectedValue(value));
        } else {
          this.emitSelectedValue(selectedValue);
        }
      }
    },
  },
  created() {
    this.$perkStoreIdentityRegistry.searchUserOrSpaceObject(eXo.env.portal.userName, 'user').then((item) => {
      if (item) {
        item.id_type = `${item.type}_${item.id}`;
        this.currentUserItem = item;
      }
    });
  },
  mounted() {
    window.addEventListener('click',() => {
      if (this.$refs.selectAutoComplete) {
        this.$refs.selectAutoComplete.blur();
      }
    });
  },
  methods: {
    emitSelectedValue(selectedValue) {
      const isAddress = selectedValue.indexOf('_') < 0;
      const type = isAddress ? null : selectedValue.substring(0, selectedValue.indexOf('_'));
      const id = isAddress ? selectedValue : selectedValue.substring(selectedValue.indexOf('_') + 1);
      if (this.noAddress) {
        this.addressLoad = 'success';
        this.$emit('item-selected', {
          id: id,
          type: type,
          address: id,
        });
      } else if (isAddress) {
        return this.$perkStoreIdentityRegistry.searchFullName(selectedValue)
          .then(details => {
            if (details && details.type) {
              this.addressLoad = 'success';
              this.$emit('item-selected', {
                id: details.id,
                type: details.type,
                address: details.address,
                id_type: `${details.type}_${details.id}`,
              });
            } else {
              this.addressLoad = 'success';
              this.$emit('item-selected', {
                id: id,
                type: type,
                address: id,
              });
            }
          });
      } else {
        return this.$perkStoreIdentityRegistry.searchAddress(id, type)
          .then((address) => {
            if (address && address.length) {
              this.addressLoad = 'success';
              this.$emit('item-selected', {
                id: id,
                type: type,
                address: address,
              });
            } else {
              this.addressLoad = 'error';
              this.$emit('item-selected', {
                id: id,
                type: type,
                address: null,
              });
            }
          })
          .catch((error) => {
            console.error('searchAddress method - error', error);
            this.addressLoad = 'error';
          });
      }
    },
    clear() {
      if (this.currentUserItem) {
        this.items = [this.currentUserItem];
      } else {
        this.items = [];
      }
      this.selectedValue = null;
      this.searchTerm = null;
      this.address = null;
      this.isLoadingSuggestions = false;
      this.addressLoad = '';
      this.error = null;
    },
    canAddItem(item) {
      return !item || !item.id || this.ignoreItems.indexOf(item.id) < 0;
    },
    filterIgnoredItems(item, queryText, itemText) {
      if (queryText && itemText.toLowerCase().indexOf(queryText.toLowerCase()) < 0) {
        return false;
      }
      if (this.ignoreItems && this.ignoreItems.length) {
        return this.canAddItem(item);
      }
      return true;
    },
    selectItems(items) {
      if (items) {
        if (items.splice) {
          items.forEach((item) => {
            if (item && item.id && item.type) {
              this.selectSingleItem(item.id, item.type);
            }
          });
        } else if (items.id && items.type) {
          this.selectSingleItem(items.id, items.type);
        }
      }
    },
    selectSingleItem(id, type) {
      if (!id) {
        this.$refs.selectAutoComplete.selectItem(null);
      } else if (type === 'group') {
        return this.$perkStoreIdentityRegistry.getGroupIdentity(id)
          .then((identity) => {
            const item = {
              avatar: null,
              name: identity.profile.fullname,
              id: identity.remoteId,
              id_type: `group_${identity.remoteId}`,
              technicalId: identity.id,
            };
            this.items.push(item);
            if (this.$refs.selectAutoComplete) {
              this.$refs.selectAutoComplete.selectItem(item);
            }
          });
      } else if (type) {
        return this.$perkStoreIdentityRegistry.searchUserOrSpaceObject(id, type).then((item) => {
          item.id_type = item.type && item.id ? `${item.type}_${item.id}` : null;
          this.items.push(item);
          if (this.$refs.selectAutoComplete) {
            this.$refs.selectAutoComplete.selectItem(item);
          }
        });
      } else {
        const item = {id_type: id, name: id};
        this.items.push(item);
        if (this.$refs.selectAutoComplete) {
          this.$refs.selectAutoComplete.selectItem(item);
        }
      }
    },
    remove(item) {
      if (this.selectedValue) {
        if (this.selectedValue.splice) {
          const index = this.selectedValue.indexOf(item.id_type);
          if (index >= 0){
            this.selectedValue.splice(index, 1);
          }
        } else {
          this.selectedValue = null;
        }
      }
    },
  },
};
</script>
