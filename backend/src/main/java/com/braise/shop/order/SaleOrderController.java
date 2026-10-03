package com.braise.shop.order;

import com.braise.shop.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class SaleOrderController {

    private final SaleOrderService service;

    public SaleOrderController(SaleOrderService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SaleOrder>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(status == null ? service.list() : service.list().stream()
                .filter(o -> status.equals(o.status)).toList());
    }

    @GetMapping("/{id}/items")
    public ApiResponse<List<OrderItem>> items(@PathVariable Long id) {
        return ApiResponse.success(service.items(id));
    }

    @PostMapping
    public ApiResponse<SaleOrder> create(@RequestBody SaleOrderCreate req) {
        return ApiResponse.success(service.create(req));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<SaleOrder> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.success(service.updateStatus(id, status));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<SaleOrder> pay(@PathVariable Long id) {
        return ApiResponse.success(service.pay(id));
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<SaleOrder> refund(@PathVariable Long id) {
        return ApiResponse.success(service.refund(id));
    }
}
