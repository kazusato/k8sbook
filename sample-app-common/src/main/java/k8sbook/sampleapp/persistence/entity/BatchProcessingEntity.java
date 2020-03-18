package k8sbook.sampleapp.persistence.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "BATCH_PROCESSING")
public class BatchProcessingEntity extends AbstractEntity {

    @Id
    @Column(name = "BATCH_NAME", length = 20, nullable = false)
    private String batchName;

    @Column(name = "LAST_EXECUTION_DATE_TIME")
    private LocalDateTime lastExecutionDateTime;

    @OneToMany(mappedBy = "batchProcessing", cascade = CascadeType.ALL)
    private List<BatchProcessingFileEntity> fileList;

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public LocalDateTime getLastExecutionDateTime() {
        return lastExecutionDateTime;
    }

    public void setLastExecutionDateTime(LocalDateTime lastExecutionDateTime) {
        this.lastExecutionDateTime = lastExecutionDateTime;
    }

    public List<BatchProcessingFileEntity> getFileList() {
        return fileList;
    }

    public void setFileList(List<BatchProcessingFileEntity> fileList) {
        this.fileList = fileList;
    }
}