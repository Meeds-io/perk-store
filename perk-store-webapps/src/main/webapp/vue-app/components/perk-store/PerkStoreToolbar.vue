<template>
  <v-toolbar 
    id="perkStoreToolbar"
    flat>
    <div class="d-flex justify-space-between align-center flex-grow-1">
      <v-toolbar-title 
        v-if="canAddProduct" 
        class="d-flex align-center flex-shrink-0">
        <v-icon
          v-if="menuHeaderChanged"
          @click="changeHeaderMenu">
          fas fa-arrow-left
        </v-icon>
        <div v-else class="d-flex align-center">
          <v-btn
            v-if="displayAddButton"
            id="addNewProductButton"
            class="btn btn-primary mx-2 mx-md-0 px-md-2 px-0"
            :small="isMobile"
            :large="!isMobile"
            @click="$emit('create-product')">
            <v-icon dark>mdi-plus</v-icon>
            <span class="d-none d-lg-inline">
              {{ $t('exoplatform.perkstore.button.addProduct') }}
            </span>
          </v-btn>
          <div class="ms-2 text-sub-title body-2">
            {{ $t('exoplatform.perkstore.label.balance') }} :
            <span class="symbol"> {{ symbol }} </span>
            <span class="balance">{{ balance }}  </span>
          </div>
        </div>
      </v-toolbar-title>
      <div class="d-flex justify-end align-center flex-sm-grow-0 flex-grow-1">
        <v-text-field
          v-if="!isMobile"
          v-model="keyword"
          :placeholder="$t('exoplatform.perkstore.label.productSearchPlaceholder')"
          prepend-inner-icon="fa-filter"
          class="pa-0 me-3 mb-n1 my-auto" />
        <v-text-field
          v-else-if="isMobile && menuHeaderChanged"
          v-model="keyword"
          :placeholder="$t('exoplatform.perkstore.label.productSearchPlaceholder')"
          prepend-inner-icon="fa-filter"
          class="pa-0 mx-4 mb-n1 my-auto"
          clearable />
        <v-scale-transition>
          <select
            v-model="filter"
            class="my-auto me-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
            <option
              v-for="filterItem in filters"
              :key="filterItem.value"
              :value="filterItem.value">
              {{ filterItem.text }}
            </option>
          </select>
        </v-scale-transition>
        <v-scale-transition v-if="displayFilterButton">
          <v-btn
            v-show="isMobile && mobileFilter || !isMobile"
            class="btn px-2 btn-primary"
            :min-width="iconWidth"
            outlined
            @click="$emit('show-filter')">
            <v-icon small class="primary--text me-lg-1">fa-sliders-h</v-icon>
            <span class="d-none font-weight-regular caption d-lg-inline me-1">
              {{ $t('exoplatform.perkstore.button.filter') }} </span>
          </v-btn>
        </v-scale-transition>
        <div class="d-sm-none">
          <v-icon
            v-if="!menuHeaderChanged"
            @click="changeHeaderMenu">
            fa-filter
          </v-icon>
          <v-icon
            v-else
            @click="openBottomMenu">
            fa-sliders-h
          </v-icon>
          <v-btn
            v-if="!menuHeaderChanged"
            class="btn px-2 btn-primary"
            :min-width="iconWidth"
            outlined
            @click="$emit('show-filter')">
            <v-icon small class="primary--text me-lg-1">fa-sliders-h</v-icon>
          </v-btn>
        </div>
        <v-bottom-sheet v-model="bottomMenu" class="pa-0">
          <v-sheet class="text-center">
            <v-toolbar
              color="primary"
              dark
              class="border-box-sizing">
              <v-btn text @click="bottomMenu = false">
                {{ $t('exoplatform.perkstore.label.cancel') }}
              </v-btn>
              <v-spacer />
              <v-toolbar-title>
                <v-icon>fa-filter</v-icon>
                {{ $t('exoplatform.perkstore.button.filter') }}
              </v-toolbar-title>
              <v-spacer />
              <v-btn text @click="changeFilterSelection">
                {{ $t('exoplatform.perkstore.label.confirm') }}
              </v-btn>
            </v-toolbar>
            <v-list>
              <v-list-item
                v-for="filterItem in filters"
                :key="filterItem"
                @click="filterToChange = filterItem.value">
                <v-list-item-title class="align-center d-flex">
                  <v-icon v-if="filterToChange === filterItem.value">fa-check</v-icon>
                  <span v-else class="me-6"></span>
                  <v-spacer />
                  <div>
                    {{ filterItem.text }}
                  </div>
                  <v-spacer />
                  <span class="me-6"></span>
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </v-sheet>
        </v-bottom-sheet>
      </div>
    </div>
  </v-toolbar>
</template>

<script>
export default {
  props: {
    canAddProduct: {
      type: Boolean,
      default: false,
    },
    symbol: {
      type: String,
      default: null,
    },
    balance: {
      type: Number,
      default: 0,
    },
    keyword: {
      type: String,
      default: null,
    },
    filters: {
      type: Array,
      default: null
    },
    displayFilterButton: {
      type: Boolean,
      default: false
    },
    displayAddButton: {
      type: Boolean,
      default: false
    },
    filter: {
      type: String,
      default: ''
    }
  },
  data: () => ({
    menuHeaderChanged: false,
    bottomMenu: false,
    filterToChange: null,
    iconWidth: '24px',
    startSearchAfterInMilliseconds: 300,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    typing: false,
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.sm;
    },
  },
  watch: {
    filter() {
      this.$emit('filter-changed', this.filter);
    },
    keyword() {
      this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
      if (!this.typing) {
        this.typing = true;
        this.waitForEndTyping();
      }
    },
  },
  methods: {
    changeHeaderMenu() {
      this.menuHeaderChanged = !this.menuHeaderChanged;
    },
    changeFilterSelection() {
      this.bottomMenu = false;
      this.filter = this.filterToChange;
      this.changeHeaderMenu();
    },
    openBottomMenu() {
      this.filterToChange = this.filter;
      this.bottomMenu = true;
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.typing = false;
          this.$emit('keyword-changed', this.keyword);
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  }
};
</script>

