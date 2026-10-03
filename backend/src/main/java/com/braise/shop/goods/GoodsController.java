package com.braise.shop.goods;

import com.braise.shop.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private final GoodsService service;

    public GoodsController(GoodsService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Goods>> list(@RequestParam(required = false) Boolean onSale) {
        return ApiResponse.success(onSale == null ? service.list() : service.listOnSale());
    }

    @PostMapping
    public ApiResponse<Goods> create(@RequestBody Goods in) {
        return ApiResponse.success(service.create(in));
    }

    @PutMapping("/{id}")
    public ApiResponse<Goods> update(@PathVariable Long id, @RequestBody Goods in) {
        return ApiResponse.success(service.update(id, in));
    }

    @PostMapping("/{id}/onsale")
    public ApiResponse<Goods> updateOnSale(@PathVariable Long id, @RequestParam Boolean onSale) {
        return ApiResponse.success(service.updateOnSale(id, onSale));
    }

    @PostMapping("/batch-off")
    public ApiResponse<Integer> batchOffSale(@RequestBody List<Long> ids) {
        return ApiResponse.success(service.batchOffSale(ids));
    }

    @PostMapping("/batch-discount")
    public ApiResponse<Integer> batchDiscount(@RequestParam List<Long> ids, @RequestParam BigDecimal rate) {
        return ApiResponse.success(service.batchDiscount(ids, rate));
    }
}
