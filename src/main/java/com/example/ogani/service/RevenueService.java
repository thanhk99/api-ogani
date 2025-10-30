package com.example.ogani.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ogani.dtos.response.RevenueStats;
import com.example.ogani.dtos.response.TopProductDTO;
import com.example.ogani.repository.OrderDetailRepository;
import com.example.ogani.repository.OrderRepository;

@Service
public class RevenueService {
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository ;
    
    public List<RevenueStats> getRevenueByPeriod(String periodType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate, endDate;

        // Mặc định lấy dữ liệu 1 năm gần nhất
        if ("yearly".equals(periodType)) {
            startDate = now.minusYears(5);
            endDate = now.plusDays(1);
            return mapToRevenueStats(orderRepository.getYearlyRevenue(startDate, endDate));
        } else if ("quarterly".equals(periodType)) {
            startDate = now.minusYears(2);
            endDate = now.plusDays(1);
            return mapToRevenueStats(orderRepository.getQuarterlyRevenue(startDate, endDate));
        } else if ("monthly".equals(periodType)) {
            startDate = now.minusMonths(12);
            endDate = now.plusDays(1);
            return mapToRevenueStats(orderRepository.getMonthlyRevenue(startDate, endDate));
        } else if ("weekly".equals(periodType)) {
            startDate = now.minusWeeks(12);
            endDate = now.plusDays(1);
            return mapToRevenueStats(orderRepository.getWeeklyRevenue(startDate, endDate));
        }
        return new ArrayList<>();
    }

    private List<RevenueStats> mapToRevenueStats(List<Object[]> results) {
        return results.stream()
                .map(row -> new RevenueStats(
                        row[0].toString(),
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }
    public List<TopProductDTO> getTopProductsByRevenue() {
        return mapToTopProductDTO(orderDetailRepository.findTopProductsByRevenue());
    }
    public List<TopProductDTO> getTopProductsByQuantity() {
        return mapToTopProductDTO(orderDetailRepository.findTopProductsByQuantity());
    }
    private List<TopProductDTO> mapToTopProductDTO(List<Object[]> results) {
        return results.stream()
                .map(row -> {
                    Long productId = ((Number) row[0]).longValue();
                    String productName = (String) row[1];
                    Long totalQuantity = ((Number) row[2]).longValue();
                    Long totalRevenue = ((Number) row[3]).longValue();
                    return new TopProductDTO(productId, productName, totalQuantity, totalRevenue);
                })
                .collect(Collectors.toList());
    }
}
