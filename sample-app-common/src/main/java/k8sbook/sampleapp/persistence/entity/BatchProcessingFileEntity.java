package k8sbook.sampleapp.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "BATCH_PROCESSING_FILE")
public class BatchProcessingFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long batchProcessingFileId;

    @ManyToOne
    @JoinColumn(name = "BATCH_NAME", nullable = false)
    private BatchProcessingEntity batchProcessing;

    @Column(name = "FILE_NAME", length = 300, nullable = false)
    private String fileName;

    public Long getBatchProcessingFileId() {
        return batchProcessingFileId;
    }

    public void setBatchProcessingFileId(Long batchProcessingFileId) {
        this.batchProcessingFileId = batchProcessingFileId;
    }

    public BatchProcessingEntity getBatchProcessing() {
        return batchProcessing;
    }

    public void setBatchProcessing(BatchProcessingEntity batchProcessing) {
        this.batchProcessing = batchProcessing;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
