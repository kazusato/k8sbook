package k8sbook.sampleapp.presentation.dto;

import k8sbook.sampleapp.domain.model.Location;

public class LocationDto {

    private Long locationId;

    private String locationName;

    private RegionDto region;

    private String note;

    public LocationDto() {
    }

    public LocationDto(Location location) {
        this.locationId = location.getLocationId();
        this.locationName = location.getLocationName();
        this.region = new RegionDto(location.getRegion());
        this.note = location.getNote();
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public RegionDto getRegion() {
        return region;
    }

    public void setRegion(RegionDto region) {
        this.region = region;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
