<template>
  <v-app 
    id="PerkStoreApp" 
    color="transaprent" 
    flat>
    <main>
      <v-layout justify-center>
        <v-flex xs12>
          <v-toolbar color="white">
            <v-toolbar-title>Perk store</v-toolbar-title>
            <v-spacer />
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search in products"
              single-line
              solo
              hide-details
              class="searchProductsInput mr-3" />
            <v-btn
              id="perkStoreAppMenuRefreshButton"
              icon
              flat
              title="Refresh store"
              class="mr-0"
              @click="$emit('refresh')">
              <v-icon size="20px">
                refresh
              </v-icon>
            </v-btn>
            <v-btn
              v-if="settings.canAddProduct"
              id="perkStoreAppMenuAddButton"
              icon
              flat
              title="Add product"
              class="mr-0"
              @click="$emit('add-product')">
              <v-icon size="20px">
                add
              </v-icon>
            </v-btn>
            <v-btn
              v-if="settings.isAdministrator"
              id="perkStoreAppMenuSettingsButton"
              class="mr-0 ml-0"
              icon
              flat
              title="Settings"
              @click="$emit('modify-settings')">
              <v-icon size="17px">
                fa-cog
              </v-icon>
            </v-btn>
          </v-toolbar>
          <products-list :products="filteredProducts" />
        </v-flex>
      </v-layout>
    </main>
  </v-app>
</template>

<script>
import ProductsList from './perk-store/ProductsList.vue';

export default {
  components: {
    ProductsList,
  },
  data: () => ({
    search: null,
    settings: {
      isAdministrator: true,
      canAddProduct: true,
    },
    products: [
      {
        id: 1,
        title: 'Pre-fab homes',
        description: "Plusieurs variations de Lorem Ipsum peuvent être trouvées ici ou là, mais la majeure partie d'entre elles a été altérée par l'addition d'humour ou de mots aléatoires qui ne ressemblent pas une seconde à du texte standard. Si vous voulez utiliser un passage du Lorem Ipsum, vous devez être sûr qu'il n'y a rien d'embarrassant caché dans le texte. Tous les générateurs de Lorem Ipsum sur Internet tendent à reproduire le même extrait sans fin, ce qui fait de lipsum.com le seul vrai générateur de Lorem Ipsum. Iil utilise un dictionnaire de plus de 200 mots latins, en combinaison de plusieurs structures de phrases, pour générer un Lorem Ipsum irréprochable.",
        stockType: 'QUANTITY',
        bought: 50,
        totalSupply: 100,
        price: 10,
        symbol: '$',
        canEdit: true,
        maxOrdersPerUser: 2,
        orderPeriodicity: 'WEEK',
        orderPeriodicityLabel: 'Week',
        userOrders: {
          orderedInCurrentPeriod: 2,
          totalOrders: 3,
          notDeliveredOrders: 2,
        },
      },
      {
        id: 2,
        title: 'Favorite road trips',
        description: "On sait depuis longtemps que travailler avec du texte lisible et contenant du sens est source de distractions, et empêche de se concentrer sur la mise en page elle-même. L'avantage du Lorem Ipsum sur un texte générique comme 'Du texte. Du texte. Du texte.' est qu'il possède une distribution de lettres plus ou moins normale, et en tout cas comparable avec celle du français standard.",
        img: 'https://cdn.vuetifyjs.com/images/cards/road.jpg',
        stockType: 'ILLIMITED',
        price: 20,
        symbol: '$',
        canEdit: false,
      },
      {
        id: 3,
        title: 'Best airlines',
        description: "Le Lorem Ipsum est simplement du faux texte employé dans la composition et la mise en page avant impression. Le Lorem Ipsum est le faux texte standard de l'imprimerie depuis les années 1500, quand un imprimeur anonyme assembla ensemble des morceaux de texte pour réaliser un livre spécimen de polices de texte.",
        img: 'https://cdn.vuetifyjs.com/images/cards/plane.jpg',
        stockType: 'QUANTITY',
        bought: 70,
        totalSupply: 100,
        price: 5,
        symbol: '$',
        canEdit: true,
      }
    ]
  }),
  computed: {
    filteredProducts() {
      if(this.search && this.search.trim().length) {
        const searchTerm = this.search.trim().toLowerCase();
        return this.products.slice().filter(product => (product.title && product.title.toLowerCase().indexOf(searchTerm)) >= 0 || (product.description && product.description.toLowerCase().indexOf(searchTerm) >= 0));
      } else {
        return this.products.slice();
      }
    }
  }
};
</script>