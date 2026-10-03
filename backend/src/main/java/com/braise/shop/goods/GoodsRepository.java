package com.braise.shop.goods;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoodsRepository extends JpaRepository<Goods, Long> {
    List<Goods> findByOnSale(Boolean onSale);
}
