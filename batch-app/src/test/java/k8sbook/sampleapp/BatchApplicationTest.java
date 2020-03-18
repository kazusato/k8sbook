package k8sbook.sampleapp;

import com.amazonaws.services.s3.AmazonS3;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import io.findify.s3mock.S3Mock;
import k8sbook.sampleapp.BatchApplication;
import org.assertj.db.type.Table;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDateTime;

import static com.ninja_squad.dbsetup.Operations.*;
import static org.assertj.db.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class BatchApplicationTest {

    private static boolean needToInitializeS3 = true;

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

    @BeforeAll
    public static void startS3Mock() {
        s3Mock = new S3Mock.Builder()
                .withPort(8001)
                .withInMemoryBackend()
                .build();
        s3Mock.start();
    }

    /**
     * テスト時にはバッチアプリケーションの処理（BatchApplication#run）が動くようにする。
     */
    @BeforeEach
    public void prepareBatchToRun() {
        System.setProperty("sample.app.batch.run", "true");
    }

    @AfterAll
    public static void shutdownS3Mock() {
        if (s3Mock != null) {
            s3Mock.shutdown();
        }
    }

    @BeforeEach
    public void putTestFilesToS3() {
        if (needToInitializeS3) {
            amazonS3.createBucket(bucketName);
            try {
                amazonS3.putObject(bucketName, folderName + "/location1.csv",
                        resourceLoader.getResource("classpath:"
                                + getClass().getPackageName().replace('.', '/')
                                + "/location1.csv").getFile());
                amazonS3.putObject(bucketName, folderName + "/location2.csv",
                        resourceLoader.getResource("classpath:"
                                + getClass().getPackageName().replace('.', '/')
                                + "/location2.csv").getFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            needToInitializeS3 = false;
        }
    }

    @Tag("DBRequired")
    @Test
    public void testRun() throws Exception {
        batchApp.run();

        var locationTable = new Table(dataSource, "location");
        assertThat(locationTable).hasNumberOfRows(8); // original 4 + file1 3 + file2 1
    }

    @BeforeEach
    public void prepareDatabase() {
        var operations = sequenceOf(
                deleteAllFrom("location"),
                deleteAllFrom("region"),
                insertInto("region")
                        .columns("region_id", "region_name", "creation_timestamp")
                        .values(1, "地域1", LocalDateTime.now())
                        .values(2, "地域2", LocalDateTime.now())
                        .values(3, "地域3", LocalDateTime.now())
                        .values(4, "地域4", LocalDateTime.now())
                        .build(),
                insertInto("location")
                        .columns("location_id", "location_name", "region_id", "note")
                        .values(1, "地点1", 1, "地点1の詳細です。")
                        .values(2, "地点2", 1, "地点2の詳細です。")
                        .values(3, "地点3", 1, "地点3の詳細です。")
                        .values(4, "地点4", 1, "地点4の詳細です。")
                        .build()
        );
        var dbSetup = new DbSetup(new DataSourceDestination(dataSource), operations);
        dbSetup.launch();

    }

}
