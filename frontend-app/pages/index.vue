<template>
  <v-layout
    row
    justify-center
    align-center
  >
    <v-flex
      xs12
      sm8
      md6
    >
      <div class="display-2 font-weight-black">全国観光スポット情報</div>
      <div
              v-for="region in $store.state.regions"
              :key="region.regionId">
        <a @click="moveTo(region)"><div class="display-1 region-link">{{region.regionName}}</div></a>
      </div>
    </v-flex>
  </v-layout>
</template>

<script>
  import axios from '~/plugins/axios'

  export default {
    async fetch({ store, params }) {
      const resp = await axios.get('/region')
      store.commit('regions', resp.data.regionList)
    },
    data: function () {
      return {
      }
    },
    methods: {
      moveTo: function (region) {
        this.$store.commit('currentRegion', region)
        this.$router.push('/regionDetail')
      }
    }
  }
</script>

<style>
  .region-link {
    color: black;
    padding-top: 0.2em;
    padding-bottom: 0.2em;
    padding-left: 0.2em;
  }
</style>