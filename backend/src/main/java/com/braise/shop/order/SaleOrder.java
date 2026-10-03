package com.braise.shop.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 销售订单：堂食 / 外卖 / 预订，含渠道、取餐号、金额与状态。 */
@Entity
@Table(name = "sale_order")
public class SaleOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String channel;
    public String takeNo;
    public BigDecimal amount;
    public String status;
    public Boolean paid;
    public LocalDateTime createdAt = LocalDateTime.now();
}
