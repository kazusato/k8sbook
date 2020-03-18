package k8sbook.sampleapp;

import k8sbook.sampleapp.aws.S3FileHandler;
import k8sbook.sampleapp.domain.service.LocationService;
import k8sbook.sampleapp.persistence.entity.BatchProcessingFileEntity;
import k8sbook.sampleapp.persistence.repository.BatchProcessingFileRepository;
import k8sbook.sampleapp.persistence.repository.BatchProcessingRepository;
import k8sbook.sampleapp.persistence.repository.RegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LocationDataLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationDataLoader.class);

    private static final String BATCH_NAME = "SAMPLE_APP_BATCH";

    private static final String WORK_FOLDER_SUFFIX = "_work";

    @Value("${sample.app.batch.bucket.name}")
    private String bucketName;

    @Value("${sample.app.batch.folder.name}")
    private String folderName;

    private final BatchProcessingRepository batchProcessingRepository;

    private final BatchProcessingFileRepository batchProcessingFileRepository;

    private final S3FileHandler handler;

    private final LocationFileProcessor fileProcessor;

    public LocationDataLoader(LocationService service,
                              RegionRepository regionRepository,
                              BatchProcessingRepository batchProcessingRepository,
                              BatchProcessingFileRepository batchProcessingFileRepository,
                              S3FileHandler handler,
                              LocationFileProcessor fileProcessor) {
        this.batchProcessingRepository = batchProcessingRepository;
        this.batchProcessingFileRepository = batchProcessingFileRepository;
        this.handler = handler;
        this.fileProcessor = fileProcessor;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockAndUpdateLastExecutionDateTime() {
        var batchProcessing = batchProcessingRepository.findByIdWithLock(BATCH_NAME);
        if (batchProcessing.isPresent()) {
            var batchProcessingEntity = batchProcessing.get();
            batchProcessingEntity.setLastExecutionDateTime(LocalDateTime.now());
            batchProcessingRepository.save(batchProcessingEntity);
        } else {
            throw new RuntimeException("No batch processing record for " + BATCH_NAME);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Resource> findTargetFiles() {
        String workFolderName = folderName + WORK_FOLDER_SUFFIX;

        var batchProcFiles = batchProcessingFileRepository.findAll().stream()
                .map(BatchProcessingFileEntity::getFileName).collect(Collectors.toList());
        if (batchProcFiles.isEmpty()) {
            handler.deleteAllFilesInFolder(bucketName, workFolderName);
        } else {
            handler.deleteAllFilesInFolderExcept(bucketName, workFolderName, batchProcFiles);
        }

        List<Resource> filesToProcess = new ArrayList<>();

        var filesToSkip = new ArrayList<>();
        var filesInWorkFolder = handler.listFilesInFolder(bucketName, workFolderName, "*");
        filesToProcess.addAll(Arrays.asList(filesInWorkFolder));
        for (var fileInWorkFolder : filesInWorkFolder) {
            var shortFileName = new File(fileInWorkFolder.getFilename()).getName();
            filesToSkip.add(shortFileName);
        }

        var filesInUploadFolder = handler.listFilesInFolder(bucketName, folderName, "*");
        for (var fileInUploadFolder : filesInUploadFolder) {
            var shortFileName = new File(fileInUploadFolder.getFilename()).getName();
            if (!filesToSkip.contains(shortFileName)) {
                var workFile = handler.moveFileToWorkFolder(bucketName, fileInUploadFolder.getFilename(), folderName, WORK_FOLDER_SUFFIX);
                filesToProcess.add(workFile);
                // regiseter file data into DB
                var fileEntity = new BatchProcessingFileEntity();
                var batchProcEntity = batchProcessingRepository.findById(BATCH_NAME);
                if (batchProcEntity.isPresent()) {
                    fileEntity.setBatchProcessing(batchProcEntity.get());
                    fileEntity.setFileName(shortFileName);
                    batchProcessingFileRepository.save(fileEntity);
                } else {
                    throw new RuntimeException("No batch processing record for: " + BATCH_NAME);
                }
                LOGGER.info("Moved file to work folder: " + workFile.getFilename());
            }
        }

        return filesToProcess;
    }

    @Transactional
    public void processFiles(List<Resource> files) {
        for (Resource file : files) {
            if (file.isReadable()) {
                try {
                    fileProcessor.processFile(file);
                } catch (IOException e) {
                    LOGGER.error("Error occurred during processing file: " + file.getFilename(), e);
                }
            }
        }

    }

    private void logFilenamesInUploadFolder(Resource[] files) {
        for (Resource file : files) {
            if (file.isReadable()) {
                LOGGER.info("Following files found in the upload folder: " + file.getFilename());
            }
        }
    }

}
