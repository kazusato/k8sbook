package k8sbook.sampleapp.persistence.repository;

import k8sbook.sampleapp.persistence.entity.BatchProcessingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Optional;

public interface BatchProcessingRepository extends JpaRepository<BatchProcessingEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "javax.persistence.lock.timeout", value = "0"))
    @Query("select bp from BatchProcessingEntity bp where bp.batchName = :batchName")
    Optional<BatchProcessingEntity> findByIdWithLock(String batchName);

}
