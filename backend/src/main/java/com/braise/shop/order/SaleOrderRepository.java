package com.braise.shop.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {
    List<SaleOrder> findByStatus(String status);
}
