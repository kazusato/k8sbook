package k8sbook.sampleapp.aws;

import com.amazonaws.services.s3.AmazonS3;
import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class S3TestUtilTest {

    private static final String BUCKET_NAME = "test-bucket";

    private static final String FOLDER_NAME = "data";

    private static final String WORK_FOLDER_SUFFIX = "_work";

    private static S3Mock s3Mock;

    @BeforeAll
    public static void prepareBatchNotToRun() {
        System.setProperty("sample.app.batch.run", "false");
    }

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private AmazonS3 amazonS3;

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

    @Test
    @Tag("S3Test")
    public void testDeleteS3Files() throws Exception {
        amazonS3.createBucket(S3TestUtilTest.BUCKET_NAME);
        putFileFromResource("a.csv", FOLDER_NAME);
        S3TestUtil.deleteS3Files(amazonS3, S3TestUtilTest.BUCKET_NAME, FOLDER_NAME, WORK_FOLDER_SUFFIX);
        assertThat(amazonS3.listObjects(S3TestUtilTest.BUCKET_NAME).getObjectSummaries()).hasSize(0);
    }

    private void putFileFromResource(String file, String targetFolderName) throws IOException {
        amazonS3.putObject(BUCKET_NAME, targetFolderName + "/" + file,
                resourceLoader.getResource("classpath:"
                        + getClass().getPackageName().replace('.', '/')
                        + "/" + file).getFile());
    }

}
