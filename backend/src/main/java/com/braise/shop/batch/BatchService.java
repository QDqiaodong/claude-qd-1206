package com.braise.shop.batch;

import com.braise.shop.common.BizException;
import com.braise.shop.common.Dict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BatchService {

    private final BatchRepository repo;

    public BatchService(BatchRepository repo) {
        this.repo = repo;
    }

    public List<Batch> list() {
        return repo.findAll();
    }

    public Batch get(Long id) {
        return repo.findById(id).orElseThrow(() -> new BizException("批次不存在"));
    }

    public Batch create(Batch in) {
        if (in.batchNo == null || in.batchNo.isBlank()) {
            throw new BizException("锅次号不能为空");
        }
        if (repo.existsByBatchNo(in.batchNo)) {
            throw new BizException("锅次号已存在");
        }
        if (in.recipe == null || in.recipe.isBlank()) {
            throw new BizException("配方名不能为空");
        }
        if (in.startTime == null) {
            throw new BizException("开始时间不能为空");
        }
        if (in.endTime != null && in.endTime.isBefore(in.startTime)) {
            throw new BizException("结束时间不能早于开始时间");
        }
        if (in.status == null || in.status.isBlank()) {
            in.status = "卤制中";
        }
        if (!Dict.isValidBatchStatus(in.status)) {
            throw new BizException("批次状态非法");
        }
        Batch b = new Batch();
        b.batchNo = in.batchNo;
        b.recipe = in.recipe;
        b.startTime = in.startTime;
        b.endTime = in.endTime;
        b.status = in.status;
        b.outputQty = (in.outputQty == null) ? 0 : in.outputQty;
        b.note = (in.note == null) ? "" : in.note;
        b.createdAt = LocalDateTime.now();
        return repo.save(b);
    }

    public Batch update(Long id, Batch in) {
        Batch b = repo.findById(id).orElseThrow(() -> new BizException("批次不存在"));
        if (in.recipe != null && !in.recipe.isBlank()) {
            b.recipe = in.recipe;
        }
        if (in.startTime != null) {
            b.startTime = in.startTime;
        }
        if (in.endTime != null) {
            b.endTime = in.endTime;
        }
        if (b.endTime != null && b.startTime != null && b.endTime.isBefore(b.startTime)) {
            throw new BizException("结束时间不能早于开始时间");
        }
        if (in.outputQty != null) {
            b.outputQty = in.outputQty;
        }
        if (in.note != null) {
            b.note = in.note;
        }
        // 状态流转：已封装 / 已报废 为终态；已报废不可再封装
        if (in.status != null && !in.status.isBlank() && !in.status.equals(b.status)) {
            if (!Dict.canTransferBatch(b.status, in.status)) {
                throw new BizException("批次状态流转非法：已封装 / 已报废 为终态，且已报废不可再封装");
            }
            if ("已封装".equals(in.status) && (b.outputQty == null || b.outputQty <= 0)) {
                throw new BizException("封装前必须填写大于 0 的封装数量");
            }
            b.status = in.status;
        }
        return repo.save(b);
    }

    /** 状态流转（已报废不可再封装）。 */
    public Batch updateStatus(Long id, String to) {
        Batch b = repo.findById(id).orElseThrow(() -> new BizException("批次不存在"));
        String from = b.status;
        if (!Dict.canTransferBatch(from, to)) {
            throw new BizException("批次状态流转非法：已封装 / 已报废 为终态，且已报废不可再封装");
        }
        if ("已封装".equals(to) && (b.outputQty == null || b.outputQty <= 0)) {
            throw new BizException("封装前必须填写大于 0 的封装数量");
        }
        if (to != null && !to.equals(from)) {
            b.status = to;
        }
        return repo.save(b);
    }
}
