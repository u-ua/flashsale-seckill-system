CREATE TABLE IF NOT EXISTS voucher_order (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  voucher_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_user_voucher (user_id, voucher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS voucher (
  id BIGINT NOT NULL PRIMARY KEY,
  title VARCHAR(128) NOT NULL,
  stock INT NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO voucher (id, title, stock, updated_at) VALUES (1, '100元秒杀券', 100, NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title), stock=VALUES(stock), updated_at=VALUES(updated_at);
