<template>
  <v-layout>
    <v-flex>
      <v-card flat class="pb-1 pt-1 pl-1 pr-1">
        <v-img
          :src="selectedImage.src"
          max-height="50vh"
          height="50vh"
          contain />
        <v-card-title>
          <v-spacer />
          <v-card flat class="imageMiniPreviewParent">
            <v-flex
              v-for="(image, i) in images"
              :key="image.id"
              class="clickable"
              @click="selectedImageIndex = i">
              <v-card class="imageMiniPreview">
                <v-img
                  :src="image.src"
                  aspect-ratio="1"
                  max-height="50px"
                  max-width="50px"
                  contain>
                  <v-layout
                    slot="placeholder"
                    fill-height
                    align-center
                    justify-center
                    ma-0>
                    <v-progress-circular indeterminate color="grey lighten-5" />
                  </v-layout>
                </v-img>
              </v-card>
            </v-flex>
          </v-card>
          <v-spacer />
        </v-card-title>
      </v-card>
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