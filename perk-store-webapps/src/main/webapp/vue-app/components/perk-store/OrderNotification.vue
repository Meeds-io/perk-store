<template>
  <v-snackbar
    v-model="snackbar"
    :timeout="120000"
    class="notificationParent"
    color="black">
    <v-card
      flat
      dark
      class="transparent">
      <template v-for="order in filteredOrders">
        <v-card-text
          :key="order.id"
          class="ellipsis notificationContent"
          dark>
          New order #{{ order.id }} from {{ order.sender.displayName }}
        </v-card-text>
        <v-divider
          v-if="displayDivider"
          :key="order.id"
          dark />
      </template>
      <v-card-actions>
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
    orders: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data () {
    return {
      snackbar: false,
      snackbarDisplayed: [],
    }
  },
  computed: {
    displayDivider() {
      return this.orders && this.orders.length > 1;
    },
    filteredOrders() {
      return this.orders.filter(order => this.snackbarDisplayed.indexOf(order.id) < 0).slice(0, Math.min(3, this.orders.length));
    },
  },
  watch: {
    snackbar() {
      if(!this.snackbar) {
        this.orders.forEach(order => this.snackbarDisplayed.push(order.id));
      }
    },
    orders() {
      this.snackbar = !!this.orders.filter(order => this.snackbarDisplayed.indexOf(order.id) < 0).length;
    },
  },
  methods: {
    refreshList() {
      this.$emit('refresh-list');
    },
    close() {
      this.snackbar = false;
    }
  },
}
</script>
