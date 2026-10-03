package com.braise.shop.batch;

import com.braise.shop.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService service;

    public BatchController(BatchService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Batch>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(status == null ? service.list() : service.list().stream()
                .filter(b -> status.equals(b.status)).toList());
    }

    @PostMapping
    public ApiResponse<Batch> create(@RequestBody Batch in) {
        return ApiResponse.success(service.create(in));
    }

    @PutMapping("/{id}")
    public ApiResponse<Batch> update(@PathVariable Long id, @RequestBody Batch in) {
        return ApiResponse.success(service.update(id, in));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<Batch> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.success(service.updateStatus(id, status));
    }
}
