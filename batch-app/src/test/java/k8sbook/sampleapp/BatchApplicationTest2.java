package k8sbook.sampleapp;

import com.amazonaws.services.s3.AmazonS3;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import io.findify.s3mock.S3Mock;
import k8sbook.sampleapp.aws.S3TestUtil;
import org.assertj.db.type.Table;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.ninja_squad.dbsetup.Operations.*;
import static org.assertj.db.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SpringBootTest
@ActiveProfiles("test")
public class BatchApplicationTest2 {

    private static final String WORK_FOLDER_SUFFIX = "_work";

    private static S3Mock s3Mock;

    @Autowired
    private BatchApplication batchApp;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private AmazonS3 amazonS3;

    /**
     * バッチアプリケーションの初期化時には、処理（BatchApplication#run）が動かないようにする。
     */
    @BeforeAll
    public static void prepareBatchNotToRun() {
        System.setProperty("sample.app.batch.run", "false");
    }

    @Value("${sample.app.batch.bucket.name}")
    private String bucketName;

    @Value("${sample.app.batch.folder.name}")
    private String folderName;

    public static Stream<Arguments> testPatterns() {
        return Stream.of(
                arguments(
                        List.of("a.csv", "b.csv"),
                        List.of(),
                        List.of(),
                        List.of("A", "B")
                ),
                arguments(
                        List.of("b.csv"),
                        List.of("a.csv"),
                        List.of("a.csv", "b.csv"),
                        List.of("A", "B")
                ),
                arguments(
                        List.of(),
                        List.of("a.csv", "b.csv"),
                        List.of("a.csv", "b.csv"),
                        List.of("A", "B")
                ),
                arguments(
                        List.of("b.csv"),
                        List.of("a.csv"),
                        List.of(),
                        List.of("B")
                )
        );
    }

    public static Stream<Arguments> testPatternsWithNewRevision() {
        return Stream.of(
                arguments(
                        List.of("a.csv", "b.csv"),
                        List.of("a.csv"),
                        List.of(),
                        List.of("A2", "B")
                ),
                arguments(
                        List.of("a.csv"),
                        List.of("a.csv"),
                        List.of("a.csv"),
                        List.of("A")
                )
        );
    }

    @BeforeAll
    public static void startS3Mock() {
        s3Mock = new S3Mock.Builder()
                .withPort(8001)
                .withInMemoryBackend()
                .build();
        s3Mock.start();
    }

    @AfterAll
    public static void shutdownS3Mock() {
        if (s3Mock != null) {
            s3Mock.shutdown();
        }
    }

    /**
     * テスト時にはバッチアプリケーションの処理（BatchApplication#run）が動くようにする。
     */
    @BeforeEach
    public void prepareBatchToRun() {
        System.setProperty("sample.app.batch.run", "true");
    }

    @ParameterizedTest
    @MethodSource("testPatterns")
    @Tag("DBRequired")
    public void testBatchRunFromVariousStates(List<String> files, List<String> processingFiles, List<String> dbFiles,
                                              List<String> expectedResults) throws Exception {
        S3TestUtil.deleteS3Files(amazonS3, bucketName, folderName, WORK_FOLDER_SUFFIX);
        S3TestUtil.createBucketIfNotExist(amazonS3, bucketName);

        prepareS3Files(files, processingFiles);
        prepareBatchProcessingInDatabase(dbFiles);

        batchApp.run();

        assertThat(new Table(dataSource, "location"))
                .hasNumberOfRows(expectedResults.size())
                .column("location_name")
                .containsValues(expectedResults.toArray());
        assertThat(new Table(dataSource, "batch_processing")).hasNumberOfRows(1)
                .column("last_execution_date_time").value().isNotNull();
        assertThat(new Table(dataSource, "batch_processing_file")).hasNumberOfRows(0);
    }

    @ParameterizedTest
    @MethodSource("testPatternsWithNewRevision")
    @Tag("DBRequired")
    public void testBatchRunWithNewRevision(List<String> files, List<String> processingFiles, List<String> dbFiles,
                                            List<String> expectedResults) throws Exception {
        S3TestUtil.deleteS3Files(amazonS3, bucketName, folderName, WORK_FOLDER_SUFFIX);
        S3TestUtil.createBucketIfNotExist(amazonS3, bucketName);

        prepareS3FilesForNewRevisionTest(files, processingFiles);
        prepareBatchProcessingInDatabase(dbFiles);

        batchApp.run();

        assertThat(new Table(dataSource, "location"))
                .hasNumberOfRows(expectedResults.size())
                .column("location_name")
                .containsValues(expectedResults.toArray());
        assertThat(new Table(dataSource, "batch_processing")).hasNumberOfRows(1)
                .column("last_execution_date_time").value().isNotNull();
        assertThat(new Table(dataSource, "batch_processing_file")).hasNumberOfRows(0);
    }


    private void prepareS3Files(List<String> files, List<String> processingFiles) throws IOException {
        for (String file : files) {
            putFileFromResource(file, folderName);
        }
        for (String processingFile : processingFiles) {
            putFileFromResource(processingFile, folderName + WORK_FOLDER_SUFFIX);
        }
    }

    private void prepareS3FilesForNewRevisionTest(List<String> files, List<String> processingFiles) throws IOException {
        for (String file : files) {
            if (file.equals("a.csv")) {
                putRev2FileFromResource(file, folderName);
            } else {
                putFileFromResource(file, folderName);
            }
        }
        for (String processingFile : processingFiles) {
            putFileFromResource(processingFile, folderName + WORK_FOLDER_SUFFIX);
        }
    }

    private void putFileFromResource(String file, String targetFolderName) throws IOException {
        amazonS3.putObject(bucketName, targetFolderName + "/" + file,
                resourceLoader.getResource("classpath:"
                        + getClass().getPackageName().replace('.', '/')
                        + "/" + file).getFile());
    }

    private void putRev2FileFromResource(String file, String targetFolderName) throws IOException {
        amazonS3.putObject(bucketName, targetFolderName + "/" + file,
                resourceLoader.getResource("classpath:"
                        + getClass().getPackageName().replace('.', '/')
                        + "/rev2_" + file).getFile());
    }

    @BeforeEach
    public void prepareDatabase() {
        var operations = sequenceOf(
                deleteAllFrom("location"),
                deleteAllFrom("region"),
                insertInto("region")
                        .columns("region_id", "region_name", "creation_timestamp")
                        .values(1, "地域1", LocalDateTime.now())
                        .build()
        );
        var dbSetup = new DbSetup(new DataSourceDestination(dataSource), operations);
        dbSetup.launch();
    }

    private void prepareBatchProcessingInDatabase(List<String> dbFiles) {
        var insertBuilderForFile = insertInto("batch_processing_file")
                .columns("batch_name", "file_name");
        for (String dbFile : dbFiles) {
            insertBuilderForFile.values("SAMPLE_APP_BATCH", dbFile);
        }
        var insertForFile = insertBuilderForFile.build();
        var operations = sequenceOf(
                deleteAllFrom("batch_processing"),
                deleteAllFrom("batch_processing_file"),
                insertInto("batch_processing")
                        .columns("batch_name")
                        .values("SAMPLE_APP_BATCH")
                        .build(),
                insertForFile
        );
        var dbSetup = new DbSetup(new DataSourceDestination(dataSource), operations);
        dbSetup.launch();
    }
}
