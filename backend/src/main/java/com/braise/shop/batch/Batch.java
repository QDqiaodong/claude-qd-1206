package com.braise.shop.batch;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 卤制批次：记录每一锅卤味的配方、起止时间与封装状态。 */
@Entity
@Table(name = "batch")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String batchNo;
    public String recipe;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public String status;
    public Integer outputQty;
    public String note;
    public LocalDateTime createdAt = LocalDateTime.now();
}
