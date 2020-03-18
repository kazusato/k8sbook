package k8sbook.sampleapp.domain.service;

import k8sbook.sampleapp.domain.model.Region;
import k8sbook.sampleapp.persistence.repository.RegionRepository;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class RegionService {

    private final RegionRepository regionRepository;

    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    public List<Region> getAllRegions() {
        var regionEntities = regionRepository.findAll();
        var regionList = new ArrayList<Region>();
        regionEntities.forEach(entity -> regionList.add(new Region(entity)));

        return regionList;
    }

}
