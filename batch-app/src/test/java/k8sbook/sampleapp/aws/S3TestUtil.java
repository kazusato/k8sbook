package k8sbook.sampleapp.aws;

import com.amazonaws.services.s3.AmazonS3;

public class S3TestUtil {

    public static void createBucketIfNotExist(AmazonS3 amazonS3, String bucketName) {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            amazonS3.createBucket(bucketName);
        }
    }

    public static void deleteS3Files(AmazonS3 amazonS3, String bucketName, String folderName, String workFolderSuffix) {
        if (amazonS3.doesBucketExistV2(bucketName)) {
            for (var s3ObjectSummary : amazonS3.listObjects(bucketName, folderName).getObjectSummaries()) {
                amazonS3.deleteObject(bucketName, s3ObjectSummary.getKey());
            }
            for (var s3ObjectSummary : amazonS3.listObjects(bucketName, folderName + workFolderSuffix).getObjectSummaries()) {
                amazonS3.deleteObject(bucketName, s3ObjectSummary.getKey());
            }
        }
    }

}
