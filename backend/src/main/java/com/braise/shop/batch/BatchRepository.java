package com.braise.shop.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findByStatus(String status);
    boolean existsByBatchNo(String batchNo);
}
