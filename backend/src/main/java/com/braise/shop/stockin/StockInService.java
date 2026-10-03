package com.braise.shop.stockin;

import com.braise.shop.common.BizException;
import com.braise.shop.common.Dict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockInService {

    private final StockInRepository repo;

    public StockInService(StockInRepository repo) {
        this.repo = repo;
    }

    public List<StockIn> list() {
        return repo.findAll();
    }

    public StockIn get(Long id) {
        return repo.findById(id).orElseThrow(() -> new BizException("进货记录不存在"));
    }

    public StockIn create(StockIn in) {
        if (in.supplier == null || in.supplier.isBlank()) {
            throw new BizException("供应商不能为空");
        }
        if (!Dict.isValidStockMaterial(in.material)) {
            throw new BizException("材料类别必须是：冻禽 / 香料 / 素菜 / 油料");
        }
        if (in.qty == null || in.qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("进货数量必须大于 0");
        }
        if (in.unit == null || in.unit.isBlank()) {
            throw new BizException("单位不能为空");
        }
        if (!Dict.isValidAcceptStatus(in.acceptStatus)) {
            in.acceptStatus = "待验收";
        }
        StockIn s = new StockIn();
        s.supplier = in.supplier;
        s.material = in.material;
        s.qty = in.qty;
        s.unit = in.unit;
        s.acceptStatus = in.acceptStatus;
        s.batchNo = (in.batchNo == null) ? "" : in.batchNo;
        s.arrivalDate = (in.arrivalDate == null) ? LocalDate.now() : in.arrivalDate;
        s.createdAt = LocalDateTime.now();
        return repo.save(s);
    }

    public StockIn update(Long id, StockIn in) {
        StockIn s = repo.findById(id).orElseThrow(() -> new BizException("进货记录不存在"));
        if (in.supplier != null && !in.supplier.isBlank()) {
            s.supplier = in.supplier;
        }
        if (in.material != null && !in.material.isBlank()) {
            if (!Dict.isValidStockMaterial(in.material)) {
                throw new BizException("材料类别必须是：冻禽 / 香料 / 素菜 / 油料");
            }
            s.material = in.material;
        }
        if (in.qty != null) {
            if (in.qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("进货数量必须大于 0");
            }
            s.qty = in.qty;
        }
        if (in.unit != null && !in.unit.isBlank()) {
            s.unit = in.unit;
        }
        if (in.batchNo != null) {
            s.batchNo = in.batchNo;
        }
        if (in.arrivalDate != null) {
            s.arrivalDate = in.arrivalDate;
        }
        // 验收状态流转：退回的不可再入库（退回不能再转合格）
        if (in.acceptStatus != null && !in.acceptStatus.isBlank() && !in.acceptStatus.equals(s.acceptStatus)) {
            if (!Dict.isValidAcceptStatus(in.acceptStatus)) {
                throw new BizException("验收状态非法");
            }
            if ("退回".equals(s.acceptStatus) && "合格".equals(in.acceptStatus)) {
                throw new BizException("已退回的原料不可重新入库（不可转合格）");
            }
            s.acceptStatus = in.acceptStatus;
        }
        return repo.save(s);
    }

    /** 验收：待验收 → 合格 / 退回。 */
    public StockIn accept(Long id, String acceptStatus) {
        StockIn s = repo.findById(id).orElseThrow(() -> new BizException("进货记录不存在"));
        if (!"待验收".equals(s.acceptStatus)) {
            throw new BizException("仅待验收记录可验收");
        }
        if (!("合格".equals(acceptStatus) || "退回".equals(acceptStatus))) {
            throw new BizException("验收结果只能是：合格 / 退回");
        }
        s.acceptStatus = acceptStatus;
        return repo.save(s);
    }

    /**
     * 可用原料统计：仅「合格」计入可用数量（退回不可入库，待验收尚未入库）。
     */
    public Map<String, BigDecimal> availableByMaterial() {
        Map<String, BigDecimal> m = new LinkedHashMap<>();
        for (String mat : Dict.STOCK_MATERIALS) {
            m.put(mat, BigDecimal.ZERO);
        }
        for (StockIn s : repo.findByAcceptStatus("合格")) {
            BigDecimal cur = m.getOrDefault(s.material, BigDecimal.ZERO);
            m.put(s.material, cur.add(s.qty == null ? BigDecimal.ZERO : s.qty));
        }
        return m;
    }
}
