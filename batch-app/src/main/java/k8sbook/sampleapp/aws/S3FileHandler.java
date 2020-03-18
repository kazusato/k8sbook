package k8sbook.sampleapp.aws;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.cloud.aws.core.io.s3.PathMatchingSimpleStorageResourcePatternResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class S3FileHandler {

    private final AmazonS3 amazonS3;

    private final ResourcePatternResolver resolver;

    public S3FileHandler(AmazonS3 amazonS3, ApplicationContext context) {
        this.amazonS3 = amazonS3;
        this.resolver = new PathMatchingSimpleStorageResourcePatternResolver(amazonS3, context);
    }

    public Resource[] listFilesInFolder(String bucketName, String folderPath, String filePattern) {
        var s3FolderUrl = "s3://" + bucketName + "/" + folderPath;
        var searchPath = s3FolderUrl + "/" + filePattern;
        try {
            return resolver.getResources(searchPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(String bucketName, String filePath) {
        amazonS3.deleteObject(bucketName, filePath);
    }

    public void copyFile(String fromBucketName, String fromFilePath, String toBucketName, String toFilePath) {
        amazonS3.copyObject(fromBucketName, fromFilePath, toBucketName, toFilePath);
    }

    public Resource moveFileToWorkFolder(String bucketName, String filePath, String folderName, String workFolderSuffix) {
        var newFilePath = filePath.replaceFirst(folderName, folderName + workFolderSuffix);
        copyFile(bucketName, filePath, bucketName, newFilePath);
        deleteFile(bucketName, filePath);
        return resolver.getResource("s3://" + bucketName + "/" + newFilePath);
    }

    public void deleteAllFilesInFolder(String bucketName, String folderPath) {
        var searchPath = "s3://" + bucketName + "/" + folderPath + "/*";
        try {
            var files = resolver.getResources(searchPath);
            for (var file : files) {
                deleteFile(bucketName, file.getFilename());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllFilesInFolderExcept(String bucketName, String folderPath, List<String> excludeList) {
        var searchPath = "s3://" + bucketName + "/" + folderPath + "/*";
        try {
            var files = resolver.getResources(searchPath);
            for (var file : files) {
                var shortFileName = new File(file.getFilename()).getName();
                if (!excludeList.contains(shortFileName)) {
                    deleteFile(bucketName, file.getFilename());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
