package k8sbook.sampleapp.domain.service;

import k8sbook.sampleapp.domain.model.Location;
import k8sbook.sampleapp.domain.model.Region;
import k8sbook.sampleapp.persistence.entity.LocationEntity;
import k8sbook.sampleapp.persistence.entity.RegionEntity;
import k8sbook.sampleapp.persistence.repository.LocationRepository;
import k8sbook.sampleapp.persistence.repository.RegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository locationRepository;

    private final RegionRepository regionRepository;

    public LocationService(LocationRepository locationRepository, RegionRepository regionRepository) {
        this.locationRepository = locationRepository;
        this.regionRepository = regionRepository;
    }

    public List<Location> getLocationListByRegionId(Integer regionId) {
        var region = regionRepository.findById(regionId);
        var locationList = new ArrayList<Location>();
        region.ifPresent(r -> {
            var locationEntityList = locationRepository.findByRegion(r);
            locationEntityList.forEach(entity -> locationList.add(new Location(entity)));
        });

        return locationList;
    }

    @Transactional
    public void registerLocations(List<Location> locationList) {
        var regionMap = new HashMap<Integer, RegionEntity>();
        locationList.forEach(location -> {
            try {
                var entity = new LocationEntity();
                entity.setLocationName(location.getLocationName());
                entity.setRegion(getRegionEntity(location.getRegion(), regionMap));
                entity.setNote(location.getNote());

                locationRepository.save(entity);
            } catch (Exception e) {
                LOGGER.warn("Skipped data. Error occurred in: " + location, e);
            }
        });
    }

    private RegionEntity getRegionEntity(Region region, Map<Integer, RegionEntity> regionMap) {
        if (regionMap.get(region.getRegionId()) == null) {
            regionRepository.findById(region.getRegionId()).ifPresent(e -> regionMap.put(region.getRegionId(), e));
        }
        return regionMap.get(region.getRegionId());
    }

}
