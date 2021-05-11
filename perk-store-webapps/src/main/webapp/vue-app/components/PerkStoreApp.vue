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
    <main>
      <template>
        <v-app class="application-toolbar">
          <v-tabs v-model="tab">
            <v-tab @click="displayProductForm ? displayProduct(selectedProduct) : closeDetails()">{{ $t('exoplatform.perkstore.label.Catalogue') }}</v-tab>
            <v-tab @click="displayMyOrdersList">{{ $t('exoplatform.perkstore.label.MyOrders') }}</v-tab>
          </v-tabs>
          <v-tabs-items v-model="tab">
            <v-tab-item class="product-list" eager>
              <div class="d-flex toolbarProduct">
                <div class="addProduct">
                  <create-product-button
                    v-if="userSettings.canAddProduct"
                    :balance="balance"
                    :symbol="symbol"
                    class="xs12 sm4 md3 d-none d-sm-flex my-1"
                    @create-product="newProduct" />
                </div>
                <div class="spacer">
                </div>
                <div class="filter">
                  <v-text-field
                    v-model="search"
                    :placeholder="$t('exoplatform.perkstore.label.productSearchPlaceholder')"
                    prepend-inner-icon="fa-filter"
                    single-line
                    hide-details
                    class="pa-0 ml-3 mr-3 my-auto" />
                </div>
                <div class="filter_menu">
                  <div class="menuList">
                    <v-menu
                      v-model="menu"
                      offset-y
                      attach
                      left
                      min-width="auto">
                      <template v-slot:activator="{ on, attrs }">
                        <button
                          class="btn ignore-vuetify-classes me-1"
                          v-bind="attrs"
                          style="white-space: nowrap;"
                          @click="menu =true"
                          @blur="closeMenu()">
                          View All
                        </button>
                      </template>
                      <v-list dense class="pt-0 pb-0">
                        <v-list-item @mousedown="$event.preventDefault()" @click="filterProducts">
                          <v-list-item-title>
                            <v-checkbox
                              v-model="productsFilters.disabled"
                              :label="$t('exoplatform.perkstore.label.productFiltersDisabledProducts')"
                              class="pt-0 pb-0" />
                          </v-list-item-title>
                        </v-list-item>
                        <v-list-item @mousedown="$event.preventDefault()" @click="filterProducts">
                          <v-list-item-title>
                            <v-checkbox
                              v-model="productsFilters.soldOut"
                              :label="$t('exoplatform.perkstore.label.productFiltersSoldOutProducts')"
                              class="pt-0 pb-0" />
                          </v-list-item-title>
                        </v-list-item>
                        <v-list-item @mousedown="$event.preventDefault()" @click="filterProducts">
                          <v-list-item-title>
                            <v-checkbox
                              v-model="productsFilters.mine"
                              :label="$t('exoplatform.perkstore.label.createdByMe')"
                              class="pt-0 pb-0" />
                          </v-list-item-title>
                        </v-list-item>
                      </v-list>
                    </v-menu>
                  </div>
                </div>
                <div :class="userSettings.administrator ? 'settingsIconAdmin':''">
                  <v-btn
                    v-if="userSettings.administrator"
                    id="perkStoreAppMenuSettingsButton"
                    :title="$t('exoplatform.perkstore.button.settings')"
                    class="toolbarButton"
                    text
                    small
                    @click="displaySettingsModal">
                    <v-icon class="me-2">
                      fa-cog
                    </v-icon>
                  </v-btn>
                </div>
              </div>
              <v-divider />
              <v-toolbar
                v-if="perkStoreEnabled && !walletLoading && walletWarning"
                color="transparent"
                flat>
                <v-spacer />
                <v-flex class="text-center">
                  <div class="alert alert-warning">
                    <i class="uiIconWarning"></i>
                    {{ walletWarning }}
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
              <product-form
                v-if="displayProductForm"
                ref="productForm"
                :product="selectedProduct"
                @saved="displayProduct"
                @error="error=$event"
                @close="displayProduct" />
              <products-list
                v-else-if="!error || (filteredProducts && filteredProducts.length)"
                ref="productsList"
                :products="filteredProducts"
                :selected-product="displayProductDetails && selectedProduct"
                :symbol="symbol"
                :loading="loading"
                :need-password="walletNeedPassword"
                :can-add-product="userSettings.canAddProduct"
                :wallet-loading="walletLoading"
                :wallet-enabled="walletEnabled && walletAddonInstalled"
                @product-details="displayProduct"
                @create-product="newProduct"
                @orders-list="displayProductOrdersList"
                @edit="editProduct"
                @buy="buyProduct"
                @close="closeDetails" />
              <buy-modal
                ref="buyModal"
                :product="selectedProduct"
                :symbol="symbol"
                :need-password="walletNeedPassword"
                :wallet-loading="walletLoading"
                :wallet-enabled="walletEnabled" />
              <settings-modal
                ref="settingsModal"
                @saved="init()" />
              <product-notification
                :products="modifiedProducts"
                @refresh-list="addNewProductsToList" />
            </v-tab-item>
            <v-tab-item class="orders-list" eager>
              <div class="d-flex toolbarOrders">
                <div class="boxTitle">
                  <div v-if="displayProductOrders && selectedProduct && selectedOrderId" class="p-3">
                    <span class="ms-2">{{ $t('exoplatform.perkstore.title.order') }} #{{ selectedOrderId }}</span> : <span class="ms-2">{{ selectedProduct.title }}</span>
                  </div>
                  <div v-else-if="displayProductOrders && selectedProduct" class="p-3">
                    <span class="ms-2">{{ $t('exoplatform.perkstore.title.myOrdersListOf') }} {{ selectedProduct.title }}</span>
                  </div>
                </div>
                <div class="spacer">
                </div>
                <div class="filterOrders">
                  <div v-if="displayProductOrders && canEditSelectedProduct">
                    <v-text-field
                      v-model="searchOrder"
                      :placeholder="$t('exoplatform.perkstore.label.orderSearchPlaceholder')"
                      prepend-inner-icon="fa-filter"
                      single-line
                      hide-details
                      class="pa-0 ml-3 mr-3 my-auto" />
                    <v-progress-circular
                      v-show="searchLoading"
                      color="primary"
                      class="mb-2 ma-auto"
                      indeterminate />
                  </div>
                </div>
                <div class="download">
                  <v-btn
                    v-if="displayProductOrders"
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
                <div class="filterList">
                  <v-btn
                    id="perkStoreAppMenuDisplayFilterButton"
                    :title="$t('exoplatform.perkstore.button.displayFilters')"
                    icon
                    text
                    @click="showFilters">
                    <v-icon color="primary">
                      fa-filter
                    </v-icon>
                  </v-btn>
                </div>
              </div>
              <v-divider />
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
                    {{ walletWarning }}
                  </div>
                </v-flex>
                <v-spacer />
              </v-toolbar>
              <orders-list
                ref="ordersList"
                :product="selectedProduct"
                :selected-order-id="selectedOrderId"
                :orders-filter="ordersFilter"
                :symbol="symbol"
                :search="searchOrder"
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
import SettingsModal from './perk-store/SettingsModal.vue';
import ProductsList from './perk-store/ProductsList.vue';
import OrdersList from './perk-store/OrdersList.vue';
import ProductForm from './perk-store/ProductForm.vue';
import BuyModal from './perk-store/BuyModal.vue';
import ProductNotification from './perk-store/ProductNotification.vue';

import {initSettings, getOrderFilter, getProductFilter, storeProductFilter} from '../js/PerkStoreSettings.js';
import {toFixed} from '../js/PerkStoreProductOrder.js';
import {getProductList, getProduct} from '../js/PerkStoreProduct.js';
import CreateProductButton from './perk-store/CreateProductButton.vue';

export default {
  components: {
    SettingsModal,
    ProductsList,
    CreateProductButton,
    OrdersList,
    ProductForm,
    BuyModal,
    ProductNotification,
  },
  data: () => ({
    tab: null,
    menu: false,
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
    selectedProduct: null,
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
  }),
  computed: {
    balance() {
      return (this.wallet && this.wallet.tokenBalance && toFixed(this.wallet.tokenBalance)) || 0;
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
    selectedProduct() {
      this.error = null;
    }
  },
  created() {
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
    return this.init(parameters && parameters.productId, parameters && parameters.orderId, parameters && parameters.notProcessedOrders && parameters.notProcessedOrders === 'true');
  },
  methods: {
    closeMenu(){
      this.menu = false;
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
      this.menu = false;
      return this.refreshProductList().finally(() => this.loading = false);
    },
    refreshProductList(selectedProductId, selectedOrderId) {
      return getProductList()
        .then((products) => {
          this.products = (products && products.filter(product => (!this.productsFilters.mine || (product.creator && product.creator.type === 'user' && product.creator.id === eXo.env.portal.userName)) && (product.enabled || this.productsFilters.disabled) && (product.unlimited || product.totalSupply > product.purchased  || this.productsFilters.soldOut))) || [];

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
    displayProductOrdersList(product, orderId, currentUserOrders) {
      this.tab = 1;
      if (!product) {
        return;
      }
      this.closeDetails();
      this.selectedProduct = product;
      this.selectedOrderId = orderId;
      this.searchOrder = null;
      this.displayProductOrders = true;
      return this.$nextTick().then(() => this.$refs.ordersList && this.$refs.ordersList.init(currentUserOrders));
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
      return this.$nextTick().then(() => this.$refs.productForm && this.$refs.productForm.init());
    },
    editProduct(product) {
      this.closeDetails();
      this.displayProductForm = true;
      this.selectedProduct = Object.assign({}, product);
      this.selectedProduct.imageFiles = this.selectedProduct.imageFiles || [];
      return this.$nextTick().then(() => this.$refs.productForm && this.$refs.productForm.init());
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
      this.$refs.buyModal.open();
    },
    displaySettingsModal() {
      this.$refs.settingsModal.open();
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