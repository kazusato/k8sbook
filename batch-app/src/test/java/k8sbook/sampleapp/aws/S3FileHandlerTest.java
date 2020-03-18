package k8sbook.sampleapp.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
public class S3FileHandlerTest {

    private static final String BUCKET_NAME = "eks-work-batch";

    private static boolean needToInitializeS3 = true;

    private static S3Mock s3Mock;

    @BeforeAll
    public static void prepareBatchNotToRun() {
        System.setProperty("sample.app.batch.run", "false");
    }

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private S3FileHandler handler;

    @BeforeAll
    public static void startS3Mock() {
        s3Mock = new S3Mock.Builder()
                .withPort(8001)
                .withInMemoryBackend()
                .build();
        s3Mock.start();
    }

    /**
     * テスト時にバッチアプリケーションの処理（BatchApplication#run）が動かないようにする。
     */
    @BeforeAll
    public static void prepareBatchToRun() {
        System.setProperty("sample.app.batch.run", "false");
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
            amazonS3.createBucket(BUCKET_NAME);
            amazonS3.putObject(BUCKET_NAME, "_unittest/forListFiles/dummyfile.txt", "dummy");
            amazonS3.putObject(BUCKET_NAME, "_unittest/forListFiles/testfile", "dummy");
            amazonS3.putObject(BUCKET_NAME, "_unittest/forListFiles/テストファイル.CSV", "dummy");

            needToInitializeS3 = false;
        }
    }

    @Test
    @Tag("S3Test")
    public void testListFilesInFolder() {
        Resource[] files = handler.listFilesInFolder(BUCKET_NAME, "_unittest/forListFiles", "*");
        assertThat(files).filteredOn(Resource::isReadable)
                .extracting(Resource::getFilename)
                .hasSize(3)
                .contains("_unittest/forListFiles/dummyfile.txt")
                .contains("_unittest/forListFiles/testfile")
                .contains("_unittest/forListFiles/テストファイル.CSV");
    }

    @Test
    @Tag("S3Test")
    public void testListFilesInFolderAndRead() {
        Resource[] files = handler.listFilesInFolder(BUCKET_NAME, "_unittest/forListFiles", "*");
        Arrays.stream(files).filter(r -> r.getFilename().equals("_unittest/forListFiles/dummyfile.txt"))
                .forEach(r -> {
                    try (var br = new BufferedReader(new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8))) {
                        assertThat(br.readLine()).isEqualTo("dummy");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    @Tag("S3Test")
    public void testCopyFileAndDeleteFile() {
        Resource[] files = handler.listFilesInFolder(BUCKET_NAME, "_unittest/forListFiles", "*");
        Arrays.stream(files).filter(r -> r.getFilename().equals("_unittest/forListFiles/テストファイル.CSV"))
                .forEach(r -> {
                    handler.copyFile(BUCKET_NAME, "_unittest/forListFiles/テストファイル.CSV",
                            BUCKET_NAME, "_unittest/forCopyAndDelete/コピー_テストファイル.CSV");
                    Resource[] copies = handler.listFilesInFolder(BUCKET_NAME, "_unittest/forCopyAndDelete", "*");
                    assertThat(copies).extracting(Resource::getFilename)
                            .contains("_unittest/forCopyAndDelete/コピー_テストファイル.CSV");
                    handler.deleteFile(BUCKET_NAME, "_unittest/forCopyAndDelete/コピー_テストファイル.CSV");
                });
    }

    @Test
    @Tag("S3Test")
    public void testMoveFileToWorkFolder() {
        amazonS3.putObject(BUCKET_NAME, "_unittest/forMove/dummyfile.txt", "dummy");

        var newResource = handler.moveFileToWorkFolder(BUCKET_NAME, "_unittest/forMove/dummyfile.txt", "_unittest/forMove", "_work");
        assertThat(newResource.getFilename()).isEqualTo("_unittest/forMove_work/dummyfile.txt");
        assertThatThrownBy(() -> amazonS3.getObject(BUCKET_NAME, "_unittest/forMove/dummyfile.txt"))
                .isInstanceOfSatisfying(AmazonS3Exception.class, e ->
                        assertThat(e.getMessage()).startsWith("The resource you requested does not exist"));
        assertThat(amazonS3.getObject(BUCKET_NAME, "_unittest/forMove_work/dummyfile.txt")).isNotNull();
    }

    @Test
    @Tag("S3Test")
    public void testDeleteAllFilesInFolder() {
        amazonS3.putObject(BUCKET_NAME, "_unittest/forMove/a.txt", "dummy");
        amazonS3.putObject(BUCKET_NAME, "_unittest/forMove/b.txt", "dummy");
        amazonS3.putObject(BUCKET_NAME, "_unittest/forMove/c.txt", "dummy");

        handler.deleteAllFilesInFolder(BUCKET_NAME, "_unittest/forMove");

        var objectSummaries = amazonS3.listObjects(BUCKET_NAME, "_unittest/forMove").getObjectSummaries();
        assertThat(objectSummaries).hasSize(0);
    }

    @Test
    @Tag("S3Test")
    public void testDeleteAllFilesInFolderExcept() {
        amazonS3.putObject(BUCKET_NAME, "_unittest/forMove/a.txt", "dummy");
        amazonS3.putObject(BUCKET_NAME, "_unittest/forMove/b.txt", "dummy");
        amazonS3.putObject(BUCKET_NAME, "_unittest/forMove/c.txt", "dummy");
        amazonS3.putObject(BUCKET_NAME, "_unittest/forMove/d.txt", "dummy");
        amazonS3.putObject(BUCKET_NAME, "_unittest/forMove/e.txt", "dummy");

        handler.deleteAllFilesInFolderExcept(BUCKET_NAME, "_unittest/forMove",
                List.of("a.txt", "d.txt"));

        var objectSummaries = amazonS3.listObjects(BUCKET_NAME, "_unittest/forMove").getObjectSummaries();
        assertThat(objectSummaries).hasSize(2)
                .extracting(S3ObjectSummary::getKey)
                .containsOnly("_unittest/forMove/a.txt", "_unittest/forMove/d.txt");
    }

}
