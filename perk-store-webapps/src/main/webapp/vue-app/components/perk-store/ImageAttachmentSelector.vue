<template>
  <v-layout class="no-wrap">
    <v-flex>
      <v-layout
        row
        wrap
        class="imageMiniPreviewParent mx-0">
        <v-flex
          v-for="(image, i) in images"
          :key="image.id"
          class="clickable"
          @click="selectedImageIndex = i">
          <v-card class="imageMiniPreview">
            <v-img
              :src="image.src"
              aspect-ratio="1"
              max-height="70px"
              max-width="70px" />
          </v-card>
        </v-flex>
      </v-layout>
    </v-flex>
    <v-flex class="productImageContainer">
      <v-img
        :src="selectedImage.src"
        max-height="100%"
        max-width="100%"
        width="100%"
        class="productImage"
        contain />
    </v-flex>
  </v-layout>
</template>
<script>
export default {
  props: {
    images: {
      type: Array,
      default: function() {
        return [];
      },
    },
  },
  data() {
    return {
      selectedImageIndex: 0
    };
  },
  computed: {
    selectedImage() {
      if (this.images && this.images.length) {
        return this.images[this.selectedImageIndex];
      }
      return null;
    },
  },
}
</script>