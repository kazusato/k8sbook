package k8sbook.sampleapp.presentation.api;

import k8sbook.sampleapp.domain.service.RegionService;
import k8sbook.sampleapp.persistence.entity.RegionEntity;
import k8sbook.sampleapp.persistence.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {RegionApi.class, RegionService.class, RegionRepository.class})
public class RegionApiTest {

    @Autowired
    private RegionApi api;

    @MockBean
    private RegionRepository repository;

    @Test
    public void testGetAllRegions() {

        var region1 = new RegionEntity();
        region1.setRegionId(1);
        region1.setRegionName("地域1");
        region1.setCreationTimestamp(LocalDateTime.now());

        var region2 = new RegionEntity();
        region2.setRegionId(2);
        region2.setRegionName("地域2");
        region2.setCreationTimestamp(LocalDateTime.now());

        var regionList = List.of(region1, region2);

        given(repository.findAll()).willReturn(regionList);
        var result = api.getAllRegions();
        assertThat(result.getRegionList()).hasSize(2);
    }

}
