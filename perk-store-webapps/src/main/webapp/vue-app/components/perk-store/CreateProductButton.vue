<template>
  <v-flex class="productDetailContent border-box-sizing xs12 sm6 md4 lg3 xl2 d-none d-sm-flex my-1 clickable" @click="$emit('create-product')">
    <v-hover>
      <v-layout
        slot-scope="{ hover }"
        :class="`elevation-${hover ? 9 : 3} productDetailContentCard text-center`"
        column>
        <v-flex>
          <div class="addProductButton mt-4">
            <v-icon size="182" class="addProductIcon">mdi-plus</v-icon>
          </div>
        </v-flex>
        <v-flex class="addProductTitle headline">
          {{ $t('exoplatform.perkstore.title.createNewProduct') }}
        </v-flex>
        <v-flex class="wrap addProductDescription">
          {{ $t('exoplatform.perkstore.title.createNewProductDescription') }}
        </v-flex>
      </v-layout>
    </v-hover>
  </v-flex>
</template>