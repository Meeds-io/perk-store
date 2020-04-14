<template>
  <v-app
    id="perkstoreOrderPortlet"
    flat
    dark>
    <main>
      <v-container pa-0>
        <v-layout
          row
          wrap
          mx-0
          style="cursor: pointer">
          <v-flex
            d-flex
            sx12>
            <v-layout
              row
              ma-0
              class="white">
              <v-flex d-flex xs12>
                <v-card flat>
                  <v-card-text class="subtitle-2 header-title-color pa-2">
                    <span :class="firstLoadingPerkStoreOrder && 'skeleton-text skeleton-background skeleton-header skeleton-border-radius'">
                      {{ $t('exoplatform.perkstore.title.myOrders') }}
                    </span>
                  </v-card-text>
                </v-card>
              </v-flex>
              <v-flex
                d-flex
                xs12
                justify-center
                pa-2>
                <a
                  :href="url"
                  class="display-1 font-weight-bold big-number"
                  :class="firstLoadingPerkStoreOrder && 'skeleton-text skeleton-background skeleton-border-radius'">
                  {{ pendingOrdersSize }}<span class="mt-4 product-label">{{ $t('exoplatform.perkstore.title.orders') }}</span>
                </a>
              </v-flex>
            </v-layout>
          </v-flex>
        </v-layout>
      </v-container>
    </main>
  </v-app>
</template>

<script>
  import {getPendingOrdersSize} from '../../js/persktoreOrderAPI.js'

  export default {
    data() {
      return {
        pendingOrdersSize:'',
        perkstoreUrl : `${ eXo.env.portal.context }/${ eXo.env.portal.portalName }/perkstore?notProcessedOrders=true`,
        firstLoadingPerkStoreOrder: true,
      }
    },
    created() {
      this.retrievePendingOrdersCount();
    },
    methods: {
      retrievePendingOrdersCount() {
        getPendingOrdersSize().then(
                (orders) => {
                  this.pendingOrdersSize = orders.size;
                  if (this.firstLoadingPerkStoreOrder) {
                    this.firstLoadingPerkStoreOrder = false;
                  }
                }
        )
      }
    },
  }
</script> 
