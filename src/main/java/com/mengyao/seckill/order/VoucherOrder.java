package com.mengyao.seckill.order;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_order", uniqueConstraints = @UniqueConstraint(name = "uk_user_voucher", columnNames = {"user_id", "voucher_id"}))
public class VoucherOrder {
    @Id private Long id;
    @Column(name="user_id", nullable=false) private Long userId;
    @Column(name="voucher_id", nullable=false) private Long voucherId;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    protected VoucherOrder() { }
    public VoucherOrder(long id, long userId, long voucherId) { this.id=id; this.userId=userId; this.voucherId=voucherId; this.createdAt=LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getVoucherId() { return voucherId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
