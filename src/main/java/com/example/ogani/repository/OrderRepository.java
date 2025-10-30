package com.example.ogani.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ogani.models.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "Select * from Orders where user_id = :id order by id desc", nativeQuery = true)
    List<Order> getOrderByUser(long id);
    // Lọc đơn đã thanh toán (PAID, SHIPPING, COMPLETED)
    @Query("SELECT o FROM Order o WHERE o.orderStatus IN (OrderStatus.PAID, OrderStatus.SHIPPING, OrderStatus.COMPLETED)")
    List<Order> findPaidOrders();

    // Doanh thu theo tuần
    @Query(value = """
        SELECT 
            CONCAT(YEAR(o.pay_datetime), '-W', LPAD(WEEK(o.pay_datetime, 1), 2, '0')) AS period,
            SUM(o.total_price) AS totalRevenue
        FROM orders o
        WHERE o.order_status IN ('PAID', 'SHIPPING', 'COMPLETED')
        AND o.pay_datetime >= :startDate
        AND o.pay_datetime < :endDate
        GROUP BY CONCAT(YEAR(o.pay_datetime), '-W', LPAD(WEEK(o.pay_datetime, 1), 2, '0'))
        ORDER BY MIN(o.pay_datetime)
        """, nativeQuery = true)
    List<Object[]> getWeeklyRevenue(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    // Doanh thu theo tháng
    @Query(value = """
        SELECT 
            DATE_FORMAT(o.pay_datetime, '%Y-%m') AS period,
            SUM(o.total_price) AS totalRevenue
        FROM orders o
        WHERE o.order_status IN ('PAID', 'SHIPPING', 'COMPLETED')
          AND o.pay_datetime >= :startDate
          AND o.pay_datetime < :endDate
        GROUP BY DATE_FORMAT(o.pay_datetime, '%Y-%m')
        ORDER BY period
        """, nativeQuery = true)
    List<Object[]> getMonthlyRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Doanh thu theo quý
    @Query(value = """
        SELECT 
            CONCAT('Q', QUARTER(o.pay_datetime), '-', YEAR(o.pay_datetime)) AS period,
            SUM(o.total_price) AS totalRevenue
        FROM orders o
        WHERE o.order_status IN ('PAID', 'SHIPPING', 'COMPLETED')
        AND o.pay_datetime >= :startDate
        AND o.pay_datetime < :endDate
        GROUP BY CONCAT('Q', QUARTER(o.pay_datetime), '-', YEAR(o.pay_datetime))
        ORDER BY MIN(o.pay_datetime)
        """, nativeQuery = true)
    List<Object[]> getQuarterlyRevenue(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    // Doanh thu theo năm
    @Query(value = """
        SELECT 
            YEAR(o.pay_datetime) AS period,
            SUM(o.total_price) AS totalRevenue
        FROM orders o
        WHERE o.order_status IN ('PAID', 'SHIPPING', 'COMPLETED')
          AND o.pay_datetime >= :startDate
          AND o.pay_datetime < :endDate
        GROUP BY YEAR(o.pay_datetime)
        ORDER BY period
        """, nativeQuery = true)
    List<Object[]> getYearlyRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
