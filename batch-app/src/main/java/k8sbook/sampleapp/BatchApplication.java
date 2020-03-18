package k8sbook.sampleapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class BatchApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchApplication.class);

    private static final String BATCH_RUN_PROP_KEY = "sample.app.batch.run";

    private final LocationDataLoader loader;

    private final Environment env;

    public BatchApplication(LocationDataLoader loader, Environment env) {
        this.loader = loader;
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (Boolean.parseBoolean(env.getProperty(BATCH_RUN_PROP_KEY))) {
            LOGGER.info("======== BATCH APPLICATION START ========");

            loader.lockAndUpdateLastExecutionDateTime();
            var files = loader.findTargetFiles();
            loader.processFiles(files);

            LOGGER.info("======== BATCH APPLICATION END ========");
        } else {
            LOGGER.info("======== SKIP BATCH APPLICATION ========");
        }
    }

}
