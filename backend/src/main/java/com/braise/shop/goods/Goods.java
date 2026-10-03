package com.braise.shop.goods;

import com.braise.shop.common.Dict;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 卤味商品：登记在售卤味的品名、辣度、价格与库存。 */
@Entity
@Table(name = "goods")
public class Goods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;
    public String category;
    public String unit;
    public BigDecimal price;
    public String spicy;
    public BigDecimal memberPrice;
    public Boolean onSale;
    public Integer stock;
    public LocalDateTime createdAt = LocalDateTime.now();
}
