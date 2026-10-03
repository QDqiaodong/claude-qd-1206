package com.braise.shop.order;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** 订单明细：一条销售订单包含若干明细，关联商品与数量。 */
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long orderId;
    public Long goodsId;
    public Integer qty;
    public BigDecimal unitPrice;
}
