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
  <v-app
    id="PerkStoreApp"
    class="transparent VuetifyApp"
    flat>
    <perk-store-buy-alert />
    <main v-if="loading">
      <v-toolbar color="transparent" flat>
        <v-spacer />
        <v-progress-circular
          color="primary"
          class="mb-2"
          indeterminate />
        <v-spacer />
      </v-toolbar>
    </main>
    <main v-else>
      <template>
        <v-app class="application-toolbar">
          <v-tabs
            v-model="tab"
            slider-size="4">
            <v-tab @click="displayProductForm ? displayProduct(selectedProduct) : closeDetails()">{{ $t('exoplatform.perkstore.label.Catalogue') }}</v-tab>
            <v-tab @click="displayMyOrdersList">{{ $t('exoplatform.perkstore.label.MyOrders') }}</v-tab>
          </v-tabs>
          <v-tabs-items v-model="tab" class="tabs-content">
            <v-tab-item class="product-list pa-4" eager>
              <v-row
                class="toolbarListProduct toolbarProduct"
                color="transparent"
                flat>
                <v-flex class="addProduct">
                  <perk-store-create-product-button
                    v-if="userSettings.canAddProduct"
                    :balance="balance"
                    :symbol="symbol"
                    @create-product="newProduct" />
                </v-flex>
                <v-spacer class="spacerProduct" />
                <v-row class="filter">
                  <v-flex class="perkStoreFilterField">
                    <v-text-field
                      v-model="search"
                      :placeholder="$t('exoplatform.perkstore.label.productSearchPlaceholder')"
                      prepend-inner-icon="fa-filter"
                      single-line
                      hide-details
                      class="pa-0 ml-3 mr-3 mb-2 perkStoreTextField" />
                  </v-flex>
                  <v-flex class="filter_menu pt-1">
                    <v-menu
                      v-model="showMenu"
                      offset-y>
                      <template #activator="{ on }">
                        <button
                          class="btn "
                          v-on="on"
                          @blur="closeMenu">
                          {{ newsStatusLabel }}
                          <i class="uiIconMiniArrowDown uiIconLightGray"></i>
                        </button>
                      </template>
                      <v-list>
                        <v-list-item @mousedown="$event.preventDefault()">
                          <v-list-item-title class="filterLabel" @click="filterProduct ='all'">{{ $t('exoplatform.perkstore.label.all') }}</v-list-item-title>
                        </v-list-item>
                        <v-list-item @mousedown="$event.preventDefault()">
                          <v-list-item-title class="filterLabel" @click="filterProduct ='activeProducts'">{{ $t('exoplatform.perkstore.label.activeProducts') }}</v-list-item-title>
                        </v-list-item>
                        <v-list-item @mousedown="$event.preventDefault()">
                          <v-list-item-title class="filterLabel" @click="filterProduct ='productFiltersDisabledProducts'">{{ $t('exoplatform.perkstore.label.productFiltersDisabledProducts') }}</v-list-item-title>
                        </v-list-item>
                        <v-list-item @mousedown="$event.preventDefault()">
                          <v-list-item-title
                            class="filterLabel"
                            @click="filterProduct ='productFiltersSoldOutProducts'">
                            {{ $t('exoplatform.perkstore.label.productFiltersSoldOutProducts') }}
                          </v-list-item-title>
                        </v-list-item>
                        <v-list-item @mousedown="$event.preventDefault()">
                          <v-list-item-title class="filterLabel" @click="filterProduct ='createdByMe'">{{ $t('exoplatform.perkstore.label.createdByMe') }}</v-list-item-title>
                        </v-list-item>
                      </v-list>
                    </v-menu>
                  </v-flex>
                </v-row>
              </v-row>
              <v-toolbar
                v-if="perkStoreEnabled && !walletLoading && walletWarning"
                color="transparent"
                flat>
                <v-spacer />
                <v-flex class="text-center">
                  <div class="alert alert-warning">
                    <i class="uiIconWarning"></i>
                    {{ walletWarning }}.
                    <a :href="walletUri"> {{ $t('exoplatform.perkstore.label.clickHere') }} </a>
                  </div>
                </v-flex>
                <v-spacer />
              </v-toolbar>
              <v-toolbar
                v-if="error"
                color="transparent"
                flat>
                <v-spacer />
                <v-flex class="text-center">
                  <div class="alert alert-error">
                    <i class="uiIconError"></i> {{ error }}
                  </div>
                </v-flex>
                <v-spacer />
              </v-toolbar>
              <perk-store-product-form
                ref="productForm"
                :product="selectedProduct"
                @error="error=$event"
                @refreshProduct="refreshProductList"
                @closeDrawer="refreshProductList" />
              <perk-store-products-list
                v-if="!error || (filteredProducts && filteredProducts.length)"
                ref="productsList"
                :products="filteredProducts"
                :selected-product="displayProductDetails && selectedProduct"
                :symbol="symbol"
                :loading="loading"
                :need-password="walletNeedPassword"
                :can-add-product="userSettings.canAddProduct"
                :wallet-loading="walletLoading"
                :wallet-enabled="walletEnabled && walletAddonInstalled"
                :wallet-deleted="walletDeleted"
                @create-product="newProduct"
                @orders-list="displayProductOrdersList"
                @edit="editProduct"
                @buy="buyProduct"
                @close="closeDetails" />
              <perk-store-buy-modal
                ref="buyModal"
                :product="selectedProduct"
                :symbol="symbol"
                :need-password="walletNeedPassword"
                :wallet-loading="walletLoading"
                :wallet-enabled="walletEnabled"
                @closeProductDetails="closeProduct()" />
              <perk-store-product-notification
                :products="modifiedProducts"
                @refresh-list="addNewProductsToList" />
            </v-tab-item>
            <v-tab-item class="orders-list" eager>
              <v-row
                color="transparent"
                flat
                class="px-8 toolbarOrders">
                <div class="mt-4 boxTitle">
                  <div v-if="displayProductOrders && selectedProduct && selectedOrderId" class="titleOrders">
                    <span class="ms-2">{{ $t('exoplatform.perkstore.title.order') }} #{{ selectedOrderId }}</span> : <span class="ms-2">{{ selectedProduct.title }}</span>
                  </div>
                  <div v-else-if="displayProductOrders && selectedProduct" class="titleOrders">
                    <span class="ms-2">{{ $t('exoplatform.perkstore.title.myOrdersListOf') }} {{ selectedProduct.title }}</span>
                  </div>
                  <div v-else>
                    <span class="textBalance titleOrders">{{ $t('exoplatform.perkstore.label.balance') }}: {{ symbol }} {{ balance }}</span>
                  </div>
                </div>
                <div class="spacer spacerOrder">
                </div>
                <v-text-field
                  v-model="searchOrder"
                  :placeholder="$t('exoplatform.perkstore.label.orderSearchPlaceholder')"
                  prepend-inner-icon="fa-filter"
                  single-line
                  hide-details
                  class="pa-0 ml-3 mr-3 mb-2 perkStoreTextField filterOrders" />
                <v-progress-circular
                  v-show="searchLoading"
                  color="primary"
                  class="mb-2 ma-auto"
                  indeterminate />
                <div class="orderMenu px-2">
                  <v-menu
                    v-model="showOrderMenu"
                    offset-y>
                    <template #activator="{ on }">
                      <button
                        class="btn"
                        v-on="on"
                        @blur="closeOrderMenu">
                        {{ filterOrderLabel }}
                        <i class="uiIconMiniArrowDown uiIconLightGray"></i>
                      </button>
                    </template>
                    <v-list>
                      <v-list-item @mousedown="$event.preventDefault()">
                        <v-list-item-title class="filterLabel" @click="filterOrder ='ALL'">{{ $t('exoplatform.perkstore.label.all') }}</v-list-item-title>
                      </v-list-item>
                      <v-list-item @mousedown="$event.preventDefault()">
                        <v-list-item-title class="filterLabel" @click="filterOrder ='SENT'">{{ $t('exoplatform.perkstore.label.purchase') }}</v-list-item-title>
                      </v-list-item>
                      <v-list-item @mousedown="$event.preventDefault()">
                        <v-list-item-title class="filterLabel" @click="filterOrder ='RECEIVED'"> {{ $t('exoplatform.perkstore.label.sale') }}</v-list-item-title>
                      </v-list-item>
                    </v-list>
                  </v-menu>
                </div>
                <div class="filterList px-2">
                  <button
                    class="btn filterOrdersSetting"
                    v-on="on"
                    outlined
                    @click="showFilters">
                    <i class="uiIcon uiIconFilterSetting pr-3"></i>
                    <span>
                      {{ $t('exoplatform.perkstore.button.filter') }}
                    </span>
                  </button>
                </div>
                <div class="download" v-if="displayProductOrders">
                  <v-btn
                    id="perkStoreAppMenuDownloadButton"
                    :title="$t('exoplatform.perkstore.button.exportAsCSV')"
                    icon
                    text
                    @click="exportOrders">
                    <v-icon>
                      fa-download
                    </v-icon>
                  </v-btn>
                </div>
              </v-row>
              <v-toolbar
                v-if="error"
                color="transparent"
                flat>
                <v-spacer />
                <v-flex class="text-center">
                  <div class="alert alert-error">
                    <i class="uiIconError"></i> {{ error }}
                  </div>
                </v-flex>
                <v-spacer />
              </v-toolbar>
              <v-toolbar
                v-if="perkStoreEnabled && !walletLoading && walletWarning"
                color="transparent"
                flat>
                <v-spacer />
                <v-flex class="text-center">
                  <div class="alert alert-warning">
                    <i class="uiIconWarning"></i>
                    {{ walletWarning }}.
                    <a :href="walletUri"> {{ $t('exoplatform.perkstore.label.clickHere') }} </a>
                  </div>
                </v-flex>
                <v-spacer />
              </v-toolbar>
              <perk-store-orders-list
                ref="ordersList"
                :product="selectedProduct"
                :selected-order-id="selectedOrderId"
                :orders-filter="ordersFilter"
                :symbol="symbol"
                :search="searchOrder"
                :filter-order="filterOrder"
                @search-loading="searchLoading = true"
                @end-search-loading="searchLoading = false"
                @init-wallet="initWalletAPI(true)"
                @display-product="displayProduct($event)"
                @loading="loading = $event"
                @error="error = $event"
                @close="closeDetails" />
            </v-tab-item>
          </v-tabs-items>
        </v-app>
      </template>
    </main>
  </v-app>
</template>

<script>
import {initSettings, getOrderFilter, getProductFilter, storeProductFilter} from '../js/PerkStoreSettings.js';
import {toFixed} from '../js/PerkStoreProductOrder.js';
import {getProductList, getProduct} from '../js/PerkStoreProduct.js';
export default {
  data: () => ({
    showMenu: false,
    showOrderMenu: false,
    newsStatusLabel: '',
    filterOrderLabel: '',
    filterProduct: '',
    filterOrder: 'ALL',
    tab: null,
    searchOrder: null,
    searchLoading: false,
    productFilterMenu: false,
    walletWarning: null,
    error: null,
    walletAddonInstalled: false,
    walletLoading: false,
    walletEnabled: false,
    perkStoreEnabled: false,
    walletNeedPassword: false,
    loading: true,
    selectedProduct: {},
    selectedOrderId: null,
    displayMyOrders: false,
    displayProductDetails: false,
    displayProductForm: false,
    displayProductOrders: false,
    search: null,
    wallet: null,
    contractDetail: null,
    ordersFilter: {},
    productsFilters: {},
    settings: {},
    userSettings: {},
    createOrUpdateOrderEvent: 'exo.perkstore.order.createOrModify',
    createOrUpdateProductEvent: 'exo.perkstore.product.createOrModify',
    products: [],
    modifiedProducts: [],
    walletUri: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/wallet`,
  }),
  computed: {
    balance() {
      return (this.wallet && this.wallet.initializationState !== 'DELETED' && this.wallet.tokenBalance && toFixed(this.wallet.tokenBalance)) || 0;
    },
    walletDeleted() {
      return this.wallet && this.wallet.initializationState === 'DELETED';
    },
    symbol() {
      return this.contractDetail && this.contractDetail.symbol;
    },
    canEditSelectedProduct() {
      return  this.selectedProduct && this.selectedProduct.userData && this.selectedProduct.userData.canEdit;
    },
    filteredProducts() {
      let products = this.products.slice();
      if (this.search && this.search.trim().length) {
        const searchTerm = this.search.trim().toLowerCase();
        products = products.slice().filter(product => (product.title && product.title.toLowerCase().indexOf(searchTerm)) >= 0 || (product.description && product.description.toLowerCase().indexOf(searchTerm) >= 0));
      }
      products = products.filter(product => product.enabled || (product.userData && product.userData.canEdit));
      return products;
    },
  },
  watch: {
    tab() {
      if (this.tab === 0) {
        window.history.pushState('perkstore', 'My perkstore', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/catalog`);
      } else if (this.tab === 1) {
        window.history.pushState('perkstore', 'My perkstore', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/myorders`);
      }
    },
    loading() {
      if (!this.loading) {
        const urlPath = document.location.pathname;
        const productId = urlPath.split('catalog/')[1] ? urlPath.split('catalog/')[1].split(/[^0-9]/)[0] : null;
        if (urlPath === `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/catalog`) {
          this.tab = 0;
        } else if (urlPath === `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/myorders`) {
          this.tab = 1;
        } else if (productId) {
          setTimeout( () => {
            if (productId && this.$refs.buyModal) {
              getProduct(productId).then(freshProduct => {
                if (freshProduct) {
                  this.selectedProduct = freshProduct;
                  if (this.selectedProduct.creator.id === eXo.env.portal.userName) {
                    window.history.pushState('perkstore', 'My perkstore', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/catalog`);
                  } else {
                    window.history.pushState('perkstore', 'My perkstore', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/catalog/${this.selectedProduct.id}`);
                    this.$refs.buyModal.open();
                  }
                }
              }).catch(() => {
                this.tab = 0;
                window.history.pushState('perkstore', 'My perkstore', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/catalog`);
              });
            }
          }, 200);
        } else {
          this.tab = 0;
          window.history.pushState('perkstore', 'My perkstore', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/catalog`);
        }
      }
    },
    filterProduct() {
      if (this.filterProduct === 'productFiltersDisabledProducts') {
        this.productsFilters.disabled = true;
        this.productsFilters.soldOut = false;
        this.productsFilters.mine = false;
        this.productsFilters.activeProducts = false;
      } else if (this.filterProduct === 'productFiltersSoldOutProducts'){
        this.productsFilters.soldOut = true;
        this.productsFilters.disabled = false;
        this.productsFilters.mine = false;
        this.productsFilters.activeProducts = false;
      } else if (this.filterProduct === 'createdByMe') {
        this.productsFilters.mine = true;
        this.productsFilters.disabled = false;
        this.productsFilters.soldOut = false;
        this.productsFilters.activeProducts = false;
      }  else if (this.filterProduct === 'activeProducts') {
        this.productsFilters.mine = false;
        this.productsFilters.disabled = false;
        this.productsFilters.soldOut = false;
        this.productsFilters.activeProducts = true;
      } else {
        this.productsFilters.mine = false;
        this.productsFilters.disabled = false;
        this.productsFilters.soldOut = false;
        this.productsFilters.activeProducts = false;
      }
      this.newsStatusLabel = this.$t(`exoplatform.perkstore.label.${this.filterProduct}`);
      this.filterProducts();
      this.showMenu = false;
    },
    filterOrder() {
      if (this.filterOrder === 'ALL') {
        this.filterOrderLabel = this.$t('exoplatform.perkstore.label.all');
      } else if (this.filterOrder === 'SENT'){
        this.filterOrderLabel = this.$t('exoplatform.perkstore.label.purchase');
      } else if (this.filterOrder === 'RECEIVED') {
        this.filterOrderLabel = this.$t('exoplatform.perkstore.label.sale');
      }
      this.showOrderMenu = false;
    },
    selectedProduct() {
      this.error = null;
    }
  },
  created() {
    this.newsStatusLabel = this.$t('exoplatform.perkstore.label.activeProducts');
    this.filterOrderLabel = this.$t('exoplatform.perkstore.label.all');
    document.addEventListener('exo.perkstore.settings.modified', this.refreshSettings);

    document.addEventListener(this.createOrUpdateProductEvent, this.updateProduct);

    document.addEventListener(this.createOrUpdateOrderEvent, this.updateProduct);

    document.addEventListener('exo-wallet-settings-loaded', this.walletSettingsLoaded);
    document.addEventListener('exo-wallet-init-result', this.walletInitialized);
    document.addEventListener('exo-wallet-init-loading', this.walletIsLoading);
    if (window.walletAddonInstalled) {
      this.initWalletAPI();
    } else {
      document.addEventListener('exo-wallet-installed', this.initWalletAPI);
    }
    const search = document.location.search.substring(1);
    let parameters = null;
    if (search) {
      parameters = JSON.parse(
        `{"${decodeURI(search)
          .replace(/"/g, '\\"')
          .replace(/&/g, '","')
          .replace(/=/g, '":"')}"}`
      );
    }
    return this.init(parameters && parameters.productId, parameters &&
    parameters.orderId, parameters && parameters.notProcessedOrders && parameters.notProcessedOrders === 'true');
  },
  methods: {
    closeProduct() {
      const currentUrl = window.location.pathname;
      const defaultUrl = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/catalog`;
      if (currentUrl.includes(defaultUrl)) {
        window.history.pushState('perkstore', 'My perkstore', defaultUrl);
      }
    },
    closeMenu() {
      this.showMenu = false;
    },
    closeOrderMenu() {
      this.showOrderMenu = false;
    },
    refreshSettings(event) {
      if (!event || !event.detail || !event.detail.globalsettings) {
        return;
      }

      this.init();
      // refresh entire application if the access permission is changed
    },
    init(selectedProductId, selectedOrderId, notProcessedOrders) {
      this.products = [];
      this.productsFilters = getProductFilter() || {};
      this.loading = true;
      return initSettings()
        .then(() => {
          this.perkStoreEnabled = true;
          this.settings = window.perkStoreSettings;
          if (!this.settings) {
            this.settings = {};
          }
          this.userSettings = this.settings.userSettings;
          this.productsFilters.activeProducts = true;
          this.ordersFilter = getOrderFilter();
        })
        .then(() => this.refreshProductList(selectedProductId, selectedOrderId))
        .then(() => {
          if (notProcessedOrders) {
            this.ordersFilter = {notProcessed: true};
            this.displayMyOrders = true;
          }
        })
        .catch(e => {
          console.error('Error initializing application', e);
          this.error = e && e.message ? e.message : String(e);
        })
        .finally(() => {
          window.setTimeout(() => {
            if (window.walletAddonInstalled) {
              this.initWalletAPI();
            } else {
              this.walletWarning = this.$t('exoplatform.perkstore.warning.walletNotInstalled');
              this.loading = false;
            }
          }, 2000);
        });
    },
    filterProducts(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();

        storeProductFilter(this.productsFilters);
      }
      this.loading = true;
      return this.refreshProductList().finally(() => this.loading = false);
    },
    refreshProductList(selectedProductId, selectedOrderId) {
      return getProductList()
        .then((products) => {
          if (!this.productsFilters.mine && !this.productsFilters.disabled && !this.productsFilters.soldOut && !this.productsFilters.activeProducts) {
            this.products =products;
          } else if (this.productsFilters.activeProducts) {
            this.products = products.filter(product => product.enabled && (product.unlimited || product.totalSupply > product.purchased) && !(product.creator && product.creator.type === 'user' && product.creator.id === eXo.env.portal.userName) &&!(product.receiverMarchand && product.receiverMarchand.type === 'user' && product.receiverMarchand.id === eXo.env.portal.userName) );
          } else if (this.productsFilters.disabled) {
            this.products = products.filter(product =>!product.enabled);
          } else if (this.productsFilters.soldOut) {
            this.products = products.filter(product =>!(product.unlimited || product.totalSupply > product.purchased));
          } else if (this.productsFilters.mine) {
            this.products = products.filter(product => (product.creator && product.creator.type === 'user' && product.creator.id === eXo.env.portal.userName)
                || (product.receiverMarchand && product.receiverMarchand.type === 'user' && product.receiverMarchand.id === eXo.env.portal.userName));
          }
          if (this.products.length && selectedProductId) {
            const selectedProduct = this.products.find(product => product.id === Number(selectedProductId));
            if (selectedProduct) {
              if (selectedOrderId) {
                this.displayProductOrdersList(selectedProduct, Number(selectedOrderId));
              } else {
                this.displayProduct(selectedProduct);
              }
            }
          }
        });
    },
    walletIsLoading() {
      this.walletAddonInstalled = true;
      this.walletLoading = true;
      this.walletWarning = null;
    },
    initWalletAPI(forceReload) {
      if (forceReload || (!this.walletAddonInstalled && window.walletAddonInstalled)) {
        document.dispatchEvent(new CustomEvent('exo-wallet-init'));
      }
    },
    walletSettingsLoaded(event) {
      this.contractDetail = event && event.detail && event.detail.contractDetail;
      this.wallet = event && event.detail && event.detail.wallet;
    },
    walletInitialized(event) {
      const result = event && event.detail;
      if (!result || result.error) {
        this.walletWarning = `${result && result.error ? (`${  result.error}`) : this.$t('exoplatform.perkstore.warning.walletNotConfiguredProperly')}`;
        this.walletEnabled = false;
      } else {
        this.walletEnabled = true;
      }
      this.walletNeedPassword = result && result.needPassword;
      this.walletLoading = false;
      this.loading = false;
    },
    closeDetails() {
      this.displayProductDetails = false;
      this.displayProductForm = false;
      this.displayProductOrders = false;
      this.displayMyOrders = false;
      this.selectedProduct = null;
      this.selectedOrderId = 0;
    },
    displayProductOrdersList(product, orderId) {
      this.tab = 1;
      if (!product) {
        return;
      }
      this.closeDetails();
      this.selectedProduct = product;
      this.selectedOrderId = orderId;
      this.searchOrder = null;
      this.displayProductOrders = true;
      return this.$nextTick().then(() => this.$refs.ordersList && this.$refs.ordersList.init());
    },
    exportOrders() {
      return this.$nextTick().then(() => this.$refs.ordersList && this.$refs.ordersList.exportOrders());
    },
    displayMyOrdersList() {
      this.closeDetails();
      this.selectedProduct = null;
      this.displayMyOrders = true;
      return this.$nextTick().then(() => this.$refs.ordersList && this.$refs.ordersList.init());
    },
    newProduct() {
      this.closeDetails();
      this.displayProductForm = true;
      this.selectedProduct = {
        imageFiles: []
      };
      return this.$nextTick().then(() => this.$refs.productForm && this.$refs.productForm.open());
    },
    editProduct(product) {
      this.closeDetails();
      this.displayProductForm = true;
      this.selectedProduct = Object.assign({}, product);
      this.selectedProduct.imageFiles = this.selectedProduct.imageFiles || [];
      return this.$nextTick().then(() => this.$refs.productForm && this.$refs.productForm.open());
    },
    displayProduct(product) {
      if (product) {
        if (product.id) {
          const existingProduct = this.products.find(existingProduct => existingProduct.id === product.id);
          if (existingProduct) {
            product = existingProduct;
          } else {
            this.products.unshift(product);
          }
        } else {
          product = this.products.find(existingProduct => existingProduct.id === product);
        }
      }
      this.closeDetails();
      if (product) {
        this.displayProductDetails = true;
        this.selectedProduct = Object.assign({}, product);
      }
    },
    showFilters() {
      if (this.$refs.ordersList) {
        this.$refs.ordersList.showFilters();
      }
    },
    buyProduct(product) {
      this.selectedProduct = product;
      window.history.pushState('perkstore', 'My perkstore', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/perkstore/catalog/${product.id}`);
      this.$refs.buyModal.open();
    },
    updateProduct(event) {
      const wsMessage = event.detail;
      if (wsMessage.product && wsMessage.product.id) {
        let product = this.products && this.products.find(product => product && product.id === wsMessage.product.id);
        // Existing product
        if (product && product.id) {
          getProduct(product.id).then(freshProduct => {
            if (freshProduct && freshProduct.userData && freshProduct.userData.username === eXo.env.portal.userName) {
              // Additional check for user data target
              Object.assign(product, freshProduct);

              if (this.selectedProduct && this.selectedProduct.id === freshProduct.id) {
                Object.assign(this.selectedProduct, freshProduct);
              }
            }
          });
        } else {
          product = wsMessage.product;

          // New product added
          getProduct(product.id).then(freshProduct => {
            if (freshProduct && freshProduct.userData && freshProduct.userData.username === eXo.env.portal.userName) {
              // Additional check for user data target
              Object.assign(product, freshProduct);

              // Add notification to non last modifier
              if (event.type === this.createOrUpdateProductEvent && !this.modifiedProducts.find(modifiedProduct => modifiedProduct.id === product.id) && (product.lastModifier && product.lastModifier.id !== eXo.env.portal.userName) || (!product.lastModifier && product.creator && product.creator.id !== eXo.env.portal.userName)) {
                this.modifiedProducts.unshift(product);
              }
            }
          });
        }
      }
    },
    addNewProductsToList() {
      this.modifiedProducts.reverse().forEach(product => {
        if (!product.lastModifiedDate && !this.products.find(existingProduct => existingProduct.id === product.id)) {
          this.products.unshift(product);
        }
      });
      this.modifiedProducts.splice(0, this.modifiedProducts.length);
    },
  }
};
</script>