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
  <v-layout
    row
    class="border-box-sizing me-0 ms-0">
    <perk-store-orders-filter
      ref="productOrdersFilter"
      :filter="ordersFilter"
      @search="searchOrders" />
    <v-container
      class="productOrdersParent border-box-sizing mt-0"
      fluid
      grid-list-md>
      <v-card
        v-if="!selectedOrderId && displayFilterDetails && filterDescriptionLabels && filterDescriptionLabels.length"
        class="transparent mb-0 mt-0 pt-0"
        flat>
        <v-card-title class="mt-0 pt-0">
          <v-spacer />
          <div class="no-wrap text-truncate">
            {{ $t('exoplatform.perkstore.label.selectedFilter') }}:
            <v-chip v-for="filterDescription in filterDescriptionLabels" :key="filterDescription">
              {{ $t(`exoplatform.perkstore.label.status.${filterDescription}`) }}
            </v-chip>
          </div>
          <v-spacer />
        </v-card-title>
      </v-card>
      <a id="downloadOrders" class="hidden">{{ $t('exoplatform.perkstore.button.download') }}</a>
      <v-row class="OrdersListParent">
        <template v-if="filteredOrders && filteredOrders.length">
          <v-col
            v-for="item in filteredOrders"
            :key="item.id"
            xs="12"
            sm="6"
            md="3"
            xl="3">
            <perk-store-order-detail
              :ref="`orderDetail${item.id}`"
              :order="item"
              :product="product"
              :symbol="symbol"
              @init-wallet="$emit('init-wallet')"
              @display-product="$emit('display-product', $event)"
              @changed="updateOrder(item, $event)"
              @loading="$emit('loading', $event)"
              @error="$emit('error', $event)" />
          </v-col>
        </template>
        <div v-else class="ma-auto">
          {{ search ? $t('exoplatform.perkstore.button.noOrdersSearchResultFor', {0: search}) : $t('exoplatform.perkstore.button.noOrdersFound') }}
        </div>
      </v-row>
      <v-flex
        v-if="loading || displayLoadMoreButton"
        slot="footer"
        class="mt-2 me-6 text-center elevation-2"
        dense
        flat>
        <v-btn
          v-if="displayLoadMoreButton"
          class="primary--text"
          text
          :loading="loading"
          :disabled="loading"
          @click="loadMore">
          {{ $t('exoplatform.perkstore.button.loadMore') }}
        </v-btn>
        <v-progress-circular
          v-else-if="loading"
          color="primary"
          class="mb-2"
          indeterminate />
      </v-flex>
      <perk-store-order-notification
        :orders="newAddedOrders"
        @refresh-list="addNewOrdersToList" />
    </v-container>
  </v-layout>
</template>

<script>
import {getOrderList} from '../../js/PerkStoreProductOrder.js';
import {getDefaultOrderFilter, formatDate, formatDateTime} from '../../js/PerkStoreSettings.js';

export default {
  props: {
    product: {
      type: Object,
      default: function() {
        return {};
      },
    },
    ordersFilter: {
      type: Object,
      default: function() {
        return {};
      },
    },
    selectedOrderId: {
      type: Number,
      default: function() {
        return 0;
      },
    },
    symbol: {
      type: String,
      default: function() {
        return '';
      },
    },
    search: {
      type: String,
      default: function() {
        return '';
      },
    },
  },
  data() {
    return {
      loading: false,
      currentUserOrders: false,
      pageSize: 12,
      limit: 12,
      limitReached: false,
      filterDescriptionLabels: [],
      displayFilterDetails: false,
      orders: [],
      newAddedOrders: [],
      initialLimit: 0,
    };
  },
  computed: {
    selectedOrdersFilter() {
      return (!this.selectedOrderId && this.ordersFilter) || getDefaultOrderFilter();
    },
    filteredOrders() {
      const order = this.selectedOrderId && this.orders.find(order => order && order.id === this.selectedOrderId);
      if (order) {
        return [order];
      } else if (this.search) {
        return this.orders.filter(order => order.sender && order.sender.displayName && order.sender.displayName.toLowerCase().indexOf(this.search.trim().toLowerCase()) >= 0).slice(0, this.initialLimit);
      } else {
        return this.orders.slice(0, this.limit);
      }
    },
    displayLoadMoreButton() {
      return !this.limitReached && this.orders.length && this.orders.length % this.pageSize === 0;
    }
  },
  watch: {
    product(value, oldValue) {
      if (value && (!oldValue || value.id !== oldValue.id)) {
        this.limit = 12;
        this.limitReached = false;
        this.newAddedOrders = [];
        this.init();
      }
    },
    search() {
      this.filterOrdersByFullTextSearchField();
    }
  },
  created() {
    this.init();
    document.addEventListener('exo.perkstore.order.createOrModify', this.updateOrderFromWS);
  },
  methods: {
    init(currentUserOrders) {
      this.$emit('error', null);

      this.computeDisplayFilterDetails();
      this.computeDescriptionLabels();
      this.currentUserOrders = currentUserOrders;

      const initialOrdersLength = this.orders.length > this.limit ? this.limit - 1 : this.orders.length;

      this.loading = true;

      return getOrderList(this.product && this.product.id, this.selectedOrdersFilter, this.selectedOrderId, this.currentUserOrders, this.limit)
        .then((orders) => {
          this.orders = orders || [];
          return this.$nextTick();
        })
        .then(() => {
          this.limitReached = this.orders.length <= initialOrdersLength || this.orders.length < this.limit;
        })
        .catch(e => {
          console.error('Error while listing orders', e);
          this.$emit('error', e && e.message ? e.message : String(e));
        }).finally(() => {
          this.loading = false;
        });
    },
    filterOrdersByFullTextSearchField() {
      if (this.loading) {
        return;
      }
      if (!this.initialLimit) {
        this.initialLimit = this.limit;
      } else if (!this.search) {
        this.limit = this.initialLimit;
        return this.init(this.currentUserOrders);
      }
      this.loading = true;
      this.$emit('search-loading');
      return this.searchOrdersFromServer()
        .finally(() => {
          this.$emit('end-search-loading');
          this.loading = false;
        });
    },
    searchOrdersFromServer() {
      return this.$nextTick()
        .then(() => {
          const searchMore = !this.limitReached && this.filteredOrders.length < this.initialLimit;
          if (searchMore) {
            return this.increaseLimitAndloadMore(true);
          }
        })
        .then(() => this.$nextTick())
        .then(() => {
          const searchMore = !this.limitReached && this.filteredOrders.length < this.initialLimit;
          if (searchMore) {
            return this.searchOrdersFromServer();
          }
        });
    },
    searchOrders() {
      return this.init(this.currentUserOrders);
    },
    computeDisplayFilterDetails() {
      this.displayFilterDetails = false;

      if (!this.selectedOrdersFilter) {
        this.displayFilterDetails = false;
        return;
      }
      if (this.selectedOrdersFilter.notProcessed) {
        this.displayFilterDetails = true;
        return;
      }
      if (this.selectedOrdersFilter.searchInDates && this.selectedOrdersFilter.selectedDate) {
        this.displayFilterDetails = true;
        return;
      }

      const selectedOrdersFilter = {
        ordered: this.selectedOrdersFilter.ordered,
        canceled: this.selectedOrdersFilter.canceled,
        paid: this.selectedOrdersFilter.paid,
        partial: this.selectedOrdersFilter.partial,
        delivered: this.selectedOrdersFilter.delivered,
        refunded: this.selectedOrdersFilter.refunded,
        fraud: this.selectedOrdersFilter.fraud,
      };

      // Check if all details are checked by default
      this.displayFilterDetails = !Object.values(selectedOrdersFilter).every(value => value);
    },
    computeDescriptionLabels() {
      this.filterDescriptionLabels = [];
      if (this.selectedOrdersFilter.searchInDates && this.selectedOrdersFilter.selectedDate) {
        const dateString = formatDate(this.selectedOrdersFilter.selectedDate);
        this.filterDescriptionLabels.push(`DATE: ${dateString}`);
      }
      if (this.selectedOrdersFilter.notProcessed) {
        this.filterDescriptionLabels.push('notProcessed');
      } else {
        if (this.selectedOrdersFilter.ordered) {
          this.filterDescriptionLabels.push('ordered');
        }
        if (this.selectedOrdersFilter.canceled) {
          this.filterDescriptionLabels.push('canceled');
        }
        if (this.selectedOrdersFilter.error) {
          this.filterDescriptionLabels.push('error');
        }
        if (this.selectedOrdersFilter.paid) {
          this.filterDescriptionLabels.push('paid');
        }
        if (this.selectedOrdersFilter.partial) {
          this.filterDescriptionLabels.push('partial');
        }
        if (this.selectedOrdersFilter.delivered) {
          this.filterDescriptionLabels.push('delivered');
        }
        if (this.selectedOrdersFilter.refunded) {
          this.filterDescriptionLabels.push('refunded');
        }
        if (this.selectedOrdersFilter.fraud) {
          this.filterDescriptionLabels.push('fraud');
        }
      }
    },
    showFilters() {
      this.$refs.productOrdersFilter.showFilters();
    },
    updateOrder(order, newOrder, orders) {
      if (!orders) {
        orders = this.orders;
      }
      Object.assign(order, newOrder);
    },
    loadMore() {
      if (this.search) {
        this.initialLimit += this.pageSize;
        this.limit = Math.max(this.limit, this.initialLimit);
        return this.filterOrdersByFullTextSearchField();
      } else {
        return this.increaseLimitAndloadMore();
      }
    },
    increaseLimitAndloadMore(usingSearch) {
      this.limit += this.pageSize;
      if (!usingSearch) {
        this.initalLimit = this.limit;
      }
      return this.init(this.currentUserOrders);
    },
    updateOrderFromWS(event) {
      const wsMessage = event.detail;
      if (this.orders && this.orders.length && wsMessage.productorder && wsMessage.productorder.id) {
        const order = this.orders.find(order => order && order.id === wsMessage.productorder.id);
        if (order) {
          this.updateOrder(order, wsMessage.productorder);
          this.updateOrder(order, wsMessage.productorder, this.newAddedOrders);
        } else if (this.product && wsMessage.productorder.productId === this.product.id) {
          if (this.product.userData && this.product.userData.canEdit) {
            if (wsMessage.productorder.status.toUpperCase() === 'ORDERED' && !this.newAddedOrders.find(order => order.id === wsMessage.productorder.id)) {
              this.newAddedOrders.unshift(wsMessage.productorder);
            }
          } else {
            this.orders.unshift(wsMessage.productorder);
          }
        }
      }
    },
    addNewOrdersToList() {
      this.newAddedOrders.reverse().forEach(order => {
        if (!this.orders.find(existingOrder => existingOrder.id === order.id)) {
          this.orders.unshift(order);
        }
      });
      this.newAddedOrders.splice(0, this.newAddedOrders.length);
    },
    exportOrders() {
      return getOrderList(this.product && this.product.id, this.selectedOrdersFilter, this.selectedOrderId, this.currentUserOrders, 0)
        .then((ordersToExport) => {
          if (!ordersToExport || !ordersToExport.length) {
            return;
          }
          ordersToExport.reverse();
          const csvHeader = {
            id: this.$t('exoplatform.perkstore.label.orderId'),
            sender: {
              displayName: this.$t('exoplatform.perkstore.label.buyer'),
            },
            receiver: {
              displayName: this.$t('exoplatform.perkstore.label.seller'),
            },
            status: this.$t('exoplatform.perkstore.label.status'),
            quantity: this.$t('exoplatform.perkstore.label.quantity'),
            deliveredQuantity: this.$t('exoplatform.perkstore.label.deliveredQuantity'),
            refundedQuantity: this.$t('exoplatform.perkstore.label.refundedQuantity'),
            amount: this.$t('exoplatform.perkstore.label.amount'),
            refundedAmount: this.$t('exoplatform.perkstore.label.refundedAmount'),
            createdDate: this.$t('exoplatform.perkstore.label.orderDate'),
            deliveredDate: this.$t('exoplatform.perkstore.label.deliveredDate'),
            refundedDate: this.$t('exoplatform.perkstore.label.refundedDate'),
          };

          ordersToExport.unshift(csvHeader);
          ordersToExport = ordersToExport.map(order =>
            [
              order.id,
              order.sender && order.sender.displayName,
              order.receiver && order.receiver.displayName,
              order.status,
              order.quantity,
              order.deliveredQuantity,
              order.refundedQuantity,
              `${order.amount} ${this.symbol}`,
              `${order.refundedAmount} ${this.symbol}`,
              order.createdDate && order.createdDate !== csvHeader.createdDate ? formatDateTime(order.createdDate) : (order.createdDate || '-'),
              order.deliveredDate && order.deliveredDate !== csvHeader.deliveredDate ? formatDateTime(order.deliveredDate) : (order.deliveredDate || '-'),
              order.refundedDate && order.refundedDate !== csvHeader.refundedDate ? formatDateTime(order.refundedDate) : (order.refundedDate || '-'),
            ]).map(e=>e.join(',')).join('\n');
          const csvContent = `data:text/csv;charset=utf-8,${ordersToExport}`;
          const downloadLink = document.getElementById('downloadOrders');
          downloadLink.setAttribute('href', csvContent);
          downloadLink.setAttribute('download', 'orders.csv');
          downloadLink.click();
        });
    },
  },
};
</script>