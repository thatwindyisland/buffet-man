CREATE TABLE IF NOT EXISTS goods (
    goods_id VARCHAR(64) COLLATE utf8mb4_bin PRIMARY KEY,
    goods_name VARCHAR(255) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    stock_status VARCHAR(2) NOT NULL,
    stock_qty INT NOT NULL DEFAULT 0,
    CHECK (price >= 0),
    CHECK (stock_status IN ('有货', '无货')),
    CHECK (stock_qty >= 0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4;
