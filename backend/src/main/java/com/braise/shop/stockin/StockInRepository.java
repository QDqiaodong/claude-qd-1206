package com.braise.shop.stockin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockInRepository extends JpaRepository<StockIn, Long> {
    List<StockIn> findByMaterial(String material);
    List<StockIn> findByAcceptStatus(String acceptStatus);
}
