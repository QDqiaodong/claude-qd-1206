package com.braise.shop.stockin;

import com.braise.shop.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-ins")
public class StockInController {

    private final StockInService service;

    public StockInController(StockInService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<StockIn>> list(@RequestParam(required = false) String material,
                                           @RequestParam(required = false) String acceptStatus) {
        List<StockIn> data = service.list();
        if (material != null) {
            data = data.stream().filter(s -> material.equals(s.material)).toList();
        }
        if (acceptStatus != null) {
            data = data.stream().filter(s -> acceptStatus.equals(s.acceptStatus)).toList();
        }
        return ApiResponse.success(data);
    }

    @PostMapping
    public ApiResponse<StockIn> create(@RequestBody StockIn in) {
        return ApiResponse.success(service.create(in));
    }

    @PutMapping("/{id}")
    public ApiResponse<StockIn> update(@PathVariable Long id, @RequestBody StockIn in) {
        return ApiResponse.success(service.update(id, in));
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<StockIn> accept(@PathVariable Long id, @RequestParam String acceptStatus) {
        return ApiResponse.success(service.accept(id, acceptStatus));
    }

    @GetMapping("/available")
    public ApiResponse<Map<String, java.math.BigDecimal>> available() {
        return ApiResponse.success(service.availableByMaterial());
    }
}
