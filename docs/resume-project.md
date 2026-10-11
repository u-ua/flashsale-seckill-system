# 简历项目说明

## 项目定位

面向高并发库存竞争场景的秒杀交易系统。核心目标是把高频抢购请求留在 Redis 原子决策层完成，并将订单持久化与接口响应解耦，降低数据库瞬时写入压力。

## 简历表述

**高并发电商秒杀系统｜Java 21、Spring Boot、MySQL、Redis、Lua、Kafka、Caffeine、Redisson**

- **库存扣减与一人一单：** 使用 Redis Lua 将资格校验、库存检查、`DECR` 扣减与用户下单标记合并为单次原子执行；消费端结合 MySQL `(user_id, voucher_id)` 唯一索引与查询校验，兜底 Kafka 至少一次投递下的重复消息。
- **异步下单与削峰：** 秒杀接口在 Redis 决策成功后投递 Kafka，由消费者异步完成订单落库；同步响应中的 `ACCEPTED` 与最终 `PERSISTED` 状态分离，避免将入口接收能力误表述为数据库写入吞吐。
- **热点缓存与限流：** 设计 Caffeine L1 + Redis L2 商品缓存，使用空值缓存和 Redisson 互斥锁降低穿透/击穿风险；基于 Redis ZSet + Lua + AOP 注解实现用户、IP、全局三维滑动窗口限流。

## 可核验实现位置

| 能力 | 代码证据 |
| --- | --- |
| Lua 原子秒杀 | `src/main/resources/lua/seckill.lua` |
| Kafka 投递与幂等消费 | `src/main/java/com/mengyao/seckill/order/SeckillService.java`、`OrderConsumer.java` |
| 数据库唯一约束 | `docker/mysql/init.sql` |
| 二级缓存与互斥重建 | `src/main/java/com/mengyao/seckill/cache/VoucherCacheService.java` |
| 三维滑动窗口限流 | `src/main/java/com/mengyao/seckill/ratelimit/` |
| 页面演示 | `src/main/resources/static/` |

## 性能表述边界

本仓库不包含未经环境、压测脚本和原始报告支撑的 QPS、P95 或 TPS 数字。压测时应分别记录入口 HTTP 接收指标、Kafka 积压/清空时间、最终订单 TPS，以及库存与唯一订单约束等正确性结果；模板见 [benchmark.md](benchmark.md)。

