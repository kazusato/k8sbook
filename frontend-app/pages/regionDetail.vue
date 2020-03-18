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
      <div class="display-2 font-weight-black">全国観光スポット情報 ＞ {{$store.state.currentRegion.regionName}}</div>
      <div
              v-for="location in $store.state.locations"
              :key="location.locationId">
        <div class="title location-title">{{location.locationName}}</div>
        <div class="body-2 location-text">{{location.note}}</div>
      </div>
      <div>
        <nuxt-link to="/">トップページに戻る</nuxt-link>
      </div>
    </v-flex>
  </v-layout>
</template>

<script>
  import axios from '~/plugins/axios'

  export default  {
    async fetch({ store, params }) {
      console.log(store.state)
      const resp = await axios.get(`/location/region/${store.state.currentRegion.regionId}`)
      store.commit('locations', resp.data.locationList)
    }
  }
</script>

<style>
  .location-title {
    padding-top: 0.5em;
    padding-bottom: 0.5em;
    text-decoration: underline;
  }
  .location-text {
    padding: 0.3em;
  }
</style>