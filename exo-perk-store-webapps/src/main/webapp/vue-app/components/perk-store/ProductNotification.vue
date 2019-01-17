<template>
  <v-snackbar
    v-model="snackbar"
    :timeout="10000"
    class="productNotificationParent"
    color="black">
    <v-card
      flat
      dark
      class="transparent">
      <template v-for="product in products">
        <v-card-text
          :key="product.id"
          class="ellipsis productNotificationContent"
          dark>
          <template v-if="product.lastModifiedDate">
            Product modified: {{ product.title }}
          </template>
          <template v-else>
            Product added: {{ product.title }}
          </template>
        </v-card-text>
        <v-divider
          v-if="displayDivider"
          :key="product.id"
          dark />
      </template>
      <v-card-actions v-if="hasNewProducts">
        <v-spacer />
        <v-btn
          dark
          flat
          title="Refresh"
          @click="refreshList">
          Refresh
        </v-btn>
        <v-spacer />
      </v-card-actions>
    </v-card>
    <v-btn
      dark
      flat
      icon
      class="ml-0"
      @click="close">
      <v-icon dark>close</v-icon>
    </v-btn>
  </v-snackbar>
</template>
<script>
export default {
  props: {
    products: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data () {
    return {
      snackbar: false,
    }
  },
  computed: {
    displayDivider() {
      return this.products && this.products.length > 1;
    },
    hasNewProducts() {
      return this.products && this.products.find(product => !product.lastModifiedDate);
    },
  },
  watch: {
    snackbar() {
      if(!this.snackbar) {
        this.products.splice(0, this.products.length);
      }
    },
    products() {
      this.snackbar = this.products && this.products.length;
    },
  },
  methods: {
    refreshList() {
      this.$emit('refresh-list');
    },
    close() {
      this.products.splice(0, this.products.length);
    }
  },
}
</script>
