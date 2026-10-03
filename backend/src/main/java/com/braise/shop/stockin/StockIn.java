package com.braise.shop.stockin;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 原料进货：记录供应商、材料类别、数量与验收状态。 */
@Entity
@Table(name = "stock_in")
public class StockIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String supplier;
    public String material;
    public BigDecimal qty;
    public String unit;
    public String acceptStatus;
    public String batchNo;
    public LocalDate arrivalDate;
    public LocalDateTime createdAt = LocalDateTime.now();
}
