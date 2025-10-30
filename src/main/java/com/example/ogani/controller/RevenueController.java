package com.example.ogani.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ogani.dtos.response.RevenueStats;
import com.example.ogani.dtos.response.TopProductDTO;
import com.example.ogani.service.RevenueService;

@RestController
@RequestMapping("/api/revenue")
public class RevenueController {
    
    @Autowired
    private RevenueService revenueService;

    @GetMapping("/stats")
    public ResponseEntity<List<RevenueStats>> getRevenueStats(@RequestParam String type) {
        List<RevenueStats> stats = revenueService.getRevenueByPeriod(type);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDTO>> getTopProducts(@RequestParam String sortBy) {
        if ("revenue".equals(sortBy)) {
            return ResponseEntity.ok(revenueService.getTopProductsByRevenue());
        } else if ("quantity".equals(sortBy)) {
            return ResponseEntity.ok(revenueService.getTopProductsByQuantity());
        }
        throw new IllegalArgumentException("sortBy must be 'revenue' or 'quantity'");
    }
}
