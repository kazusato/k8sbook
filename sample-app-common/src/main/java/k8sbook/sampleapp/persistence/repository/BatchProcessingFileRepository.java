package k8sbook.sampleapp.persistence.repository;

import k8sbook.sampleapp.persistence.entity.BatchProcessingFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchProcessingFileRepository extends JpaRepository<BatchProcessingFileEntity, Long> {

    void deleteByFileName(String fileName);

}
