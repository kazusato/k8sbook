package k8sbook.sampleapp.domain.model;

import k8sbook.sampleapp.persistence.entity.RegionEntity;

import java.time.LocalDateTime;

public class Region {

    private Integer regionId;

    private String regionName;

    private LocalDateTime creationTimestamp;

    public Region(Integer regionId, String regionName, LocalDateTime creationTimestamp) {
        if (regionName == null) {
            throw new IllegalArgumentException("regionName cannot be null.");
        }
        this.regionId = regionId;
        this.regionName = regionName;
        this.creationTimestamp = creationTimestamp;
    }

    public Region(RegionEntity entity) {
        this(entity.getRegionId(), entity.getRegionName(), entity.getCreationTimestamp());
    }

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }
}
