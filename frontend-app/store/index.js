export const state = () => ({
  regions: null,
  currentRegion: null,
  locations: null
})

export const mutations = {
  regions(state, regions) {
    state.regions = regions
  },
  currentRegion(state, region) {
    state.currentRegion = region
  },
  clearCurrentRegion(state) {
    state.currentRegion = null
  },
  locations(state, locations) {
    state.locations = locations
  }
}
