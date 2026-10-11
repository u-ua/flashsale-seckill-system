package com.mengyao.seckill.order;

import org.springframework.data.jpa.repository.JpaRepository;
public interface VoucherOrderRepository extends JpaRepository<VoucherOrder, Long> { boolean existsByUserIdAndVoucherId(long userId, long voucherId); }
