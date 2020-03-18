package k8sbook.sampleapp;

import com.opencsv.CSVReader;
import k8sbook.sampleapp.aws.S3FileHandler;
import k8sbook.sampleapp.domain.model.Location;
import k8sbook.sampleapp.domain.model.Region;
import k8sbook.sampleapp.domain.service.LocationService;
import k8sbook.sampleapp.persistence.repository.BatchProcessingFileRepository;
import k8sbook.sampleapp.persistence.repository.RegionRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LocationFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationFileProcessor.class);

    @Value("${sample.app.batch.bucket.name}")
    private String bucketName;

    private final LocationService service;

    private final RegionRepository regionRepository;

    private final BatchProcessingFileRepository batchProcessingFileRepository;

    private final S3FileHandler handler;

    public LocationFileProcessor(LocationService service,
                                 RegionRepository regionRepository,
                                 BatchProcessingFileRepository batchProcessingFileRepository,
                                 S3FileHandler handler) {
        this.service = service;
        this.regionRepository = regionRepository;
        this.batchProcessingFileRepository = batchProcessingFileRepository;
        this.handler = handler;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processFile(Resource workFile) throws IOException {
        LOGGER.info("Start processing file: " + workFile.getFilename());

        try (var isr = new InputStreamReader(workFile.getInputStream(), StandardCharsets.UTF_8)) {
            var csvReader = new CSVReader(isr);
            String[] nextLine;
            var dataList = new ArrayList<String[]>();
            for (int i = 0; (nextLine = csvReader.readNext()) != null; i++) {
                checkFormatAndAddToList(workFile, nextLine, dataList, i);
            }
            registerAndDeleteFile(workFile, dataList);
        }
        batchProcessingFileRepository.deleteByFileName(new File(workFile.getFilename()).getName());
        LOGGER.info("End processing file: " + workFile.getFilename());
    }

    private void registerAndDeleteFile(Resource file, ArrayList<String[]> dataList) {
        service.registerLocations(convertToLocationList(dataList));
        handler.deleteFile(bucketName, file.getFilename());
        LOGGER.info("File deleted: " + file.getFilename());
    }

    private void checkFormatAndAddToList(Resource file, String[] lineData, ArrayList<String[]> dataList, int lineNumber) {
        if (lineData.length != 3) {
            LOGGER.warn("invalid format in line " + (lineNumber + 1) + " of " + file.getFilename()
                    + ": " + Arrays.toString(lineData));
        } else if (StringUtils.isEmpty(lineData[1])) {
            LOGGER.warn("empty string in line " + (lineNumber + 1) + " of " + file.getFilename()
                    + ": " + Arrays.toString(lineData));
        } else {
            dataList.add(lineData);
        }
    }

    private List<Location> convertToLocationList(List<String[]> dataList) {
        var regionMapByName = getRegionMapByName();
        return dataList.stream().filter(data -> regionMapByName.get(data[0]) != null).map(data -> {
            var regionName = data[0];
            var locationName = data[1];
            var note = data[2];

            return new Location(locationName, regionMapByName.get(regionName), note);
        }).collect(Collectors.toList());
    }

    private Map<String, Region> getRegionMapByName() {
        var regionEntityList = regionRepository.findAll();
        var regionList = regionEntityList.stream().map(regionEntity -> new Region(regionEntity)).collect(Collectors.toList());
        return regionList.stream().collect(Collectors.toMap(Region::getRegionName, Function.identity()));
    }

}
