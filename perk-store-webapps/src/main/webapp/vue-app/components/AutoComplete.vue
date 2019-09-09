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
      :attach="`#${id}`"
      :placeholder="inputPlaceholder"
      :content-class="`contactAutoCompleteContent ${bigField && 'bigContactAutoComplete'}`"
      :filter="filterIgnoredItems"
      :multiple="multiple"
      :required="required"
      class="contactAutoComplete"
      max-width="100%"
      item-text="name"
      item-value="id_type"
      hide-details
      hide-selected
      chips
      cache-items
      dense
      flat
      @update:search-input="searchTerm = $event">
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
          v-if="item.avatar"
          :input-value="selected"
          :title="addressLoad === 'error' ? $t('exoplatform.perkstore.warning.recipientDoesntHaveWallet') : ''"
          class="autocompleteSelectedItem"
          @update:active="selectSingleItem(item)">
          <v-progress-circular
            v-if="addressLoad === 'loading'"
            indeterminate
            color="white"
            class="mr-2" />
          <v-icon
            v-else-if="addressLoad === 'error'"
            alt="Invalid address"
            class="mr-2"
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
        <v-label
          v-else
          :selected="selected"
          class="black--text"
          solo
          @input="selectSingleItem(item)">
          {{ item.name }}
          <a href="#" class="remove">
            <i class="uiIconClose uiIconLightGray"></i>
          </a>
        </v-label>
      </template>

      <template slot="item" slot-scope="data">
        <template v-if="typeof data.item !== 'object'">
          <v-list-item-content v-text="data.item" />
        </template>
        <v-list-item-avatar
          v-else-if="data.item.avatar"
          size="20">
          <img :src="data.item.avatar">
        </v-list-item-avatar>
        <v-list-item-title v-text="data.item.name" />
      </template>
    </v-autocomplete>
  </v-flex>
</template>

<script>
import {searchAddress, searchContact, searchFullName, searchUserOrSpaceObject} from '../js/PerkStoreIdentityRegistry.js';

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
        return searchContact(value, this.onlyUsers).then((data) => {
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
        if(this.multiple) {
          selectedValue.forEach((value) => this.emitSelectedValue(value));
        } else {
          this.emitSelectedValue(selectedValue);
        }
      }
    },
  },
  created() {
    searchUserOrSpaceObject(eXo.env.portal.userName, 'user').then((item) => {
      if (item) {
        item.id_type = `${item.type}_${item.id}`;
        this.currentUserItem = item;
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
        return searchFullName(selectedValue)
          .then(details => {
            if(details && details.type) {
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
        return searchAddress(id, type)
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
            console.debug('searchAddress method - error', error);
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
      if(items) {
        if(items.splice) {
          items.forEach((item) => {
            if(item && item.id && item.type) {
              this.selectSingleItem(item.id, item.type);
            }
          });
        } else if(items.id && items.type) {
          this.selectSingleItem(items.id, items.type);
        }
      }
    },
    selectSingleItem(id, type) {
      if (!id) {
        this.$refs.selectAutoComplete.selectItem(null);
      } else if (type) {
        return searchUserOrSpaceObject(id, type).then((item) => {
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
      if(this.selectedValue) {
        if(this.selectedValue.splice) {
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
