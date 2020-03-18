package k8sbook.sampleapp.presentation.api;

import k8sbook.sampleapp.domain.service.RegionService;
import k8sbook.sampleapp.presentation.dto.RegionDto;
import k8sbook.sampleapp.presentation.dto.RegionsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("region")
@CrossOrigin(origins = "*")
public class RegionApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegionApi.class);

    @Autowired
    private RegionService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public RegionsDto getAllRegions() {
        LOGGER.info("REGION GET ALL API");

        var allRegions = service.getAllRegions();
        var dtoList = new ArrayList<RegionDto>();
        allRegions.forEach(region -> {
            var dto = new RegionDto(region);
            dtoList.add(dto);
        });
        var regionsDto = new RegionsDto(dtoList);
        return regionsDto;
    }

}
