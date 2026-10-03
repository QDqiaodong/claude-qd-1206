SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

-- ---------------------------------------------------------------------
-- 卤味熟食店 · 商品 / 卤制批次 / 销售订单 / 原料进货
-- 库名 braise_shop；application.yml 里 ddl-auto: none，表结构全部由本文件在 MySQL 镜像构建期导入。
-- 注意：mysql 容器是 UTC，时间戳一律写 NOW() / UTC_TIMESTAMP()，日期用 CURDATE()。
-- 日期算术统一用 DATE_SUB(CURDATE(), INTERVAL N DAY)，不要写成 CURDATE() - N。

CREATE DATABASE IF NOT EXISTS braise_shop
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE braise_shop;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS sale_order;
DROP TABLE IF EXISTS stock_in;
DROP TABLE IF EXISTS batch;
DROP TABLE IF EXISTS goods;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------- 卤味商品 ----------------
CREATE TABLE goods (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(128) NOT NULL COMMENT '品名',
    category     VARCHAR(16)  COMMENT '分类：鸭脖 / 猪蹄 / 卤蛋 / 藕片 / 海带 / 豆干',
    unit         VARCHAR(8)   COMMENT '计价单位：称重 / 按份',
    price        DECIMAL(10,2) NOT NULL COMMENT '售价',
    spicy        VARCHAR(8)   COMMENT '辣度：不辣 / 微辣 / 中辣 / 重辣',
    member_price DECIMAL(10,2) COMMENT '会员价（≤ 售价）',
    on_sale      TINYINT(1)   DEFAULT 1 COMMENT '是否上架：1 上架 / 0 下架',
    stock        INT          DEFAULT 0 COMMENT '库存',
    created_at   DATETIME     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 卤制批次 ----------------
CREATE TABLE batch (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_no     VARCHAR(32) NOT NULL UNIQUE COMMENT '锅次号',
    recipe       VARCHAR(128) NOT NULL COMMENT '配方名',
    start_time   DATETIME     COMMENT '开始卤制时间',
    end_time     DATETIME     COMMENT '结束时间（≥ 开始时间）',
    status       VARCHAR(16)  COMMENT '状态：卤制中 / 冷却中 / 已封装 / 已报废',
    output_qty   INT          DEFAULT 0 COMMENT '封装数量（封装时须 > 0）',
    note         VARCHAR(255) COMMENT '备注',
    created_at   DATETIME     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 销售订单 ----------------
CREATE TABLE sale_order (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel      VARCHAR(16)  COMMENT '渠道：堂食 / 外卖 / 预订',
    take_no      VARCHAR(32)  COMMENT '取餐号',
    amount       DECIMAL(10,2) COMMENT '订单金额',
    status       VARCHAR(16)  COMMENT '状态：待制作 / 制作中 / 待取 / 已完成 / 已退单',
    paid         TINYINT(1)   DEFAULT 0 COMMENT '是否已支付',
    created_at   DATETIME     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 订单明细 ----------------
CREATE TABLE order_item (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT NOT NULL COMMENT '订单 id',
    goods_id     BIGINT NOT NULL COMMENT '商品 id',
    qty          INT NOT NULL COMMENT '数量（> 0）',
    unit_price   DECIMAL(10,2) COMMENT '单价',
    CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES sale_order (id),
    CONSTRAINT fk_oi_goods FOREIGN KEY (goods_id) REFERENCES goods (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- 原料进货 ----------------
CREATE TABLE stock_in (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier     VARCHAR(128) NOT NULL COMMENT '供应商',
    material     VARCHAR(16)  COMMENT '材料：冻禽 / 香料 / 素菜 / 油料',
    qty          DECIMAL(10,2) NOT NULL COMMENT '数量（> 0）',
    unit         VARCHAR(8)   COMMENT '单位',
    accept_status VARCHAR(16) COMMENT '验收：待验收 / 合格 / 退回',
    batch_no     VARCHAR(32)  COMMENT '到货批次号',
    arrival_date DATE         COMMENT '到货日期',
    created_at   DATETIME     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 种子数据：覆盖各模块业务规则，并植入异常态行，便于审计 / 出题点检：
--   goods  #7 下架（不可加入订单）
--   batch  #3 已报废（不可再封装）
--   sale_order #4 已退单（不可再改、退单时已回补库存）
--   stock_in   #3 退回（不可入库 / 不计入可用原料）
-- ---------------------------------------------------------------------

INSERT IGNORE INTO goods (name, category, unit, price, spicy, member_price, on_sale, stock, created_at) VALUES
('招牌鸭脖',   '鸭脖', '称重', 32.00, '中辣', 28.00, 1, 120, NOW()),
('卤猪蹄',     '猪蹄', '按份', 45.00, '微辣', 40.00, 1, 60,  NOW()),
('卤蛋',       '卤蛋', '按份', 3.50,  '不辣', 3.50,  1, 200, NOW()),
('卤藕片',     '藕片', '称重', 18.00, '微辣', 16.00, 1, 80,  NOW()),
('卤海带',     '海带', '称重', 15.00, '不辣', 13.00, 1, 90,  NOW()),
('卤豆干',     '豆干', '按份', 12.00, '重辣', 10.00, 1, 150, NOW()),
('暂停·招牌大猪蹄(下架)', '猪蹄', '按份', 88.00, '重辣', 80.00, 0, 0, NOW());

INSERT IGNORE INTO batch (batch_no, recipe, start_time, end_time, status, output_qty, note, created_at) VALUES
('L20260901-01', '招牌卤料', '2026-09-01 09:00:00', '2026-09-01 18:00:00', '已封装', 100, '正常封装', NOW()),
('L20260910-02', '微辣卤料', '2026-09-10 09:00:00', '2026-09-10 17:00:00', '冷却中', 0,   '待封装',   NOW()),
('L20260920-03', '重辣卤料', '2026-09-20 10:00:00', NULL,                   '已报废', 0,   '卤制异常报废', NOW()),
('L20260928-04', '素菜卤料', '2026-09-28 08:00:00', '2026-09-28 16:00:00', '卤制中', 0,   '进行中',   NOW());

INSERT IGNORE INTO sale_order (channel, take_no, amount, status, paid, created_at) VALUES
('堂食', 'T001', 48.50, '已完成', 1, NOW()),
('外卖', 'W002', 50.00, '待取',   1, NOW()),
('预订', 'Y003', 30.00, '待制作', 0, NOW()),
('堂食', 'T004', 36.00, '已退单', 1, NOW()),
('外卖', 'W005', 33.00, '制作中', 1, NOW());

INSERT IGNORE INTO order_item (order_id, goods_id, qty, unit_price) VALUES
(1, 2, 1, 45.00),
(1, 3, 1, 3.50),
(2, 1, 1, 32.00),
(2, 4, 1, 18.00),
(3, 5, 2, 15.00),
(4, 6, 3, 12.00),
(5, 4, 1, 18.00),
(5, 5, 1, 15.00);

INSERT IGNORE INTO stock_in (supplier, material, qty, unit, accept_status, batch_no, arrival_date, created_at) VALUES
('老张冻品', '冻禽', 200.00, 'kg', '合格', 'B20260901', DATE_SUB(CURDATE(), INTERVAL 10 DAY), NOW()),
('川味香料', '香料', 50.00,  'kg', '合格', 'B20260905', DATE_SUB(CURDATE(), INTERVAL 8 DAY),  NOW()),
('绿野素菜', '素菜', 120.00, '斤', '退回', 'B20260912', DATE_SUB(CURDATE(), INTERVAL 6 DAY),  NOW()),
('好油坊',   '油料', 80.00,  'L',  '待验收', 'B20260920', DATE_SUB(CURDATE(), INTERVAL 2 DAY), NOW()),
('老张冻品', '冻禽', 150.00, 'kg', '合格', 'B20260925', DATE_SUB(CURDATE(), INTERVAL 1 DAY),  NOW());
