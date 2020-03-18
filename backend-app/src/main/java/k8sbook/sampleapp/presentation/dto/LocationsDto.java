package k8sbook.sampleapp.presentation.dto;

import java.util.ArrayList;
import java.util.List;

public class LocationsDto {

    private List<LocationDto> locationList = new ArrayList<>();

    public LocationsDto() {
    }

    public LocationsDto(List<LocationDto> locationDtoList) {
        this.locationList.addAll(locationDtoList);
    }

    public List<LocationDto> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<LocationDto> locationList) {
        this.locationList = locationList;
    }
}
