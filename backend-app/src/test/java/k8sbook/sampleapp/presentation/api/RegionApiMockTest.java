package k8sbook.sampleapp.presentation.api;

import k8sbook.sampleapp.persistence.entity.RegionEntity;
import k8sbook.sampleapp.persistence.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RegionApiMockTest {

    @MockBean
    private RegionRepository repository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllRegions() throws Exception {
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

        mockMvc.perform(get("/region").accept(MediaType.ALL_VALUE))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(readJsonFromFile("RegionApiMockTest_testGetAllRegions.json")));
    }

    private String readJsonFromFile(String fileName) {
        try (var bis = new BufferedInputStream(getClass().getResourceAsStream(fileName))) {
            String jsonString = new String(bis.readAllBytes(), StandardCharsets.UTF_8);
            return jsonString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
