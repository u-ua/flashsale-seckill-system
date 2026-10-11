# FlashSale｜高并发电商秒杀系统

> Java 后端作品集项目 · Redis 原子决策 · Kafka 异步下单 · MySQL 最终幂等

一个用于展示秒杀核心链路的 Spring Boot 项目。它把“是否能抢到”放在 Redis 原子脚本中快速决定，把订单持久化放入 Kafka 异步执行，并用 MySQL 唯一索引保证最终幂等。

> 这是学习/作品集项目，**没有伪造压测吞吐、P95 或订单 TPS 数据**。运行 `docs/benchmark.md` 的方案后，再将实测报告作为仓库证据提交。

## 招聘方速览

| 关注点 | 本项目的实现 |
| --- | --- |
| 如何防超卖？ | Redis Lua 将库存检查、扣减及一人一单标记合并为单次原子执行；MySQL 唯一索引提供最终兜底。 |
| 流量高峰如何保护数据库？ | 接口在 Redis 决策成功后写入 Kafka，订单由消费者异步落库。 |
| 热点读与恶意请求如何处理？ | Caffeine + Redis 二级缓存保护热点数据；Redis ZSet + Lua 实现用户、IP、全局三维滑动窗口限流。 |

仓库中的实现与可用于简历的表述见 [docs/resume-project.md](docs/resume-project.md)。

## 技术栈

Java 21、Spring Boot、MySQL 8、Redis 7、Lua、Kafka、Caffeine、Redisson、Docker Compose、JUnit 5。

## 架构

```text
客户端
  │ POST /api/v1/vouchers/{id}/orders (X-User-Id)
  ▼
用户 / IP / 全局 ZSet 滑动窗口限流（Redis Lua，原子 trim + count + add）
  ▼
秒杀 Lua：资格/库存/一人一单 → DECR 库存 + SADD 用户标记
  │ 成功
  ▼
Kafka topic: seckill.orders ───────────► 消费者
                                      │ 查询幂等
                                      ▼
                             MySQL voucher_order
                             UNIQUE(user_id, voucher_id)

商品详情：Caffeine L1 → Redis L2 → MySQL
                         │
                         └─ Redisson 分布式锁互斥重建；空值短缓存
```

## 快速启动

前置条件：Docker Desktop、JDK 21、Maven 3.9+。

```powershell
docker compose up -d
mvn spring-boot:run
```

启动后访问 [http://localhost:8080](http://localhost:8080)，可打开内置演示台：查看商品与库存、初始化本地 Redis 库存、模拟用户抢购，并轮询订单从 `PROCESSING` 到 `PERSISTED` 的状态。前端没有引入额外框架或构建步骤，由 Spring Boot 的 `src/main/resources/static/` 直接托管。

首次启动后，初始化 Redis 秒杀库存（此操作会清除该券的 Redis 已下单标记，仅适合本地测试）：

```powershell
Invoke-RestMethod -Method Post "http://localhost:8080/api/v1/vouchers/1/stock?stock=100"
```

查询券详情：

```powershell
Invoke-RestMethod "http://localhost:8080/api/v1/vouchers/1"
```

提交秒杀请求：

```powershell
Invoke-RestMethod -Method Post `
  -Headers @{ "X-User-Id" = "10001" } `
  -Uri "http://localhost:8080/api/v1/vouchers/1/orders"
```

正常返回中的 `status=ACCEPTED` 表示请求已通过 Redis 原子校验并已进入 Kafka 投递链路，**不等同于订单已经落库**。请在稍后检查 MySQL：

```powershell
docker compose exec mysql mysql -uroot -proot -D seckill -e "SELECT * FROM voucher_order;"
```

## 关键设计

### 1. 防超卖与重复下单

`src/main/resources/lua/seckill.lua` 在同一 Redis 执行上下文中完成：库存检查 → 重复用户检查 → `DECR` → `SADD`。只有返回 `0` 才会生成订单号并写入 Kafka。库存键是 `seckill:stock:{voucherId}`，用户集合键是 `seckill:order:users:{voucherId}`。

消费者仍然保留两道最终保护：先按 `userId + voucherId` 查询，再依赖 MySQL 的 `UNIQUE(user_id, voucher_id)`。这能抵御 Kafka 至少一次投递造成的重复消费；唯一键冲突被安全忽略。

### 2. 削峰与一致性边界

接口同步阶段只完成 Redis 决策和 Kafka 投递，数据库写入由消费者异步处理，因此峰值接口成功率与最终订单写入 TPS 是不同指标。当前实现采用“Redis 预扣 + 异步落库”模型；若 Kafka 长时间不可用，生产环境还应补偿 Redis 标记/库存，或采用 Outbox/事务消息方案。

### 3. 缓存与限流

商品详情使用 Caffeine 本地缓存和 Redis 分布式缓存；Redis 空值缓存防穿透，Redisson 锁防止热点 key 失效时并发打穿数据库。秒杀接口在 AOP 中做用户、IP、全局三维滑动窗口限流，ZSet 的清理、计数、写入和过期均由 Lua 原子完成。

## 测试与验证

```powershell
mvn test
```

仓库 CI 使用 Temurin JDK 21 执行单元/契约测试；当前测试覆盖 Redis 键规则与 Lua 脚本的库存、重复下单判断顺序。本地启动依赖后，建议额外检查：同一用户重复请求只接受一次、库存耗尽后返回“库存不足”、重复投递同一 Kafka 消息只生成一条 MySQL 订单，以及 Redis 库存不为负。

性能测试步骤见 [docs/benchmark.md](docs/benchmark.md)。提交 GitHub 前，建议把脱敏后的 JMeter `.jmx`、测试环境参数和输出结果一起提交到 `evidence/`，而不是把估算值写到简历。

## 项目结构

```text
src/main/java/com/mengyao/seckill
├── api/          HTTP API 与统一响应
├── order/        Lua 调用、Kafka 投递/消费、订单幂等
├── ratelimit/    注解 AOP 与 Redis ZSet 滑动窗口
├── cache/        Caffeine + Redis + Redisson 缓存保护
└── catalog/      商品详情读取示例
```
