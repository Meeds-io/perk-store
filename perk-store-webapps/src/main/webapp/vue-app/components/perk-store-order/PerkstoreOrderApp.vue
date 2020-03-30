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
          style="cursor: pointer"
          @click="navigateTo('perkstore?notProcessedOrders=true')">
          <v-flex
            d-flex
            sx12>
            <v-layout
              row
              ma-0
              class="white">
              <v-flex d-flex xs12>
                <v-card flat>
                  <v-card-text class="subtitle-2 grey--text pa-2">{{ this.$t('exoplatform.perkstore.title.myOrders') }}</v-card-text>
                </v-card>
              </v-flex>
              <v-flex
                d-flex
                xs12
                justify-center>
                <v-card flat>
                  <v-card-text class="display-1 font-weight-bold pa-2 big-number">{{ pendingOrdersSize }}<span class="mt-4 product-label">{{ this.$t('exoplatform.perkstore.title.orders') }}</span></v-card-text>
                </v-card>
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
        pendingOrdersSize:''
      }
    },
    created() {
      this.getPendingOrdersSize();
    },
    methods: {
      getPendingOrdersSize() {
        getPendingOrdersSize().then(
                (orders) => {
                  this.pendingOrdersSize = orders.size;
                }
        )
      },
      navigateTo(pagelink) {
        location.href=`${ eXo.env.portal.context }/${ eXo.env.portal.portalName }/${ pagelink }` ;
      },
    }
  }
</script> 
