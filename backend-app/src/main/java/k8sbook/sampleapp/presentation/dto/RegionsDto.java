package k8sbook.sampleapp.presentation.dto;

import java.util.ArrayList;
import java.util.List;

public class RegionsDto {

    private List<RegionDto> regionList = new ArrayList<>();

    public RegionsDto() {
    }

    public RegionsDto(List<RegionDto> regionDtoList) {
        this.regionList.addAll(regionDtoList);
    }

    public List<RegionDto> getRegionList() {
        return regionList;
    }

    public void setRegionList(List<RegionDto> regionList) {
        this.regionList = regionList;
    }
}
