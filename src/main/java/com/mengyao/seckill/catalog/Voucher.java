package com.mengyao.seckill.catalog;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher")
public class Voucher {
    @Id private Long id;
    private String title;
    private Integer stock;
    @Column(name="updated_at") private LocalDateTime updatedAt;
    protected Voucher() { }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Integer getStock() { return stock; }
}
