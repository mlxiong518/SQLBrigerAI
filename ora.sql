-- 在 owuser schema 下创建表 tm_user
DROP TABLE owuser.tm_user;
CREATE TABLE owuser.tm_user (
                                id          NUMBER(10) PRIMARY KEY,
                                user_name   VARCHAR2(255) NOT NULL,
                                email       VARCHAR2(255) NOT NULL,
                                create_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建序列和触发器来模拟 SERIAL
CREATE SEQUENCE owuser.tm_user_seq START WITH 1 INCREMENT BY 1;
-- CREATE OR REPLACE TRIGGER owuser.tm_user_bir
-- BEFORE INSERT ON owuser.tm_user
-- FOR EACH ROW
-- BEGIN
-- SELECT owuser.tm_user_seq.NEXTVAL INTO :new.id FROM dual;
-- END;
/

-- 添加注释
COMMENT ON TABLE owuser.tm_user IS '用户表';
COMMENT ON COLUMN owuser.tm_user.id IS '用户ID';
COMMENT ON COLUMN owuser.tm_user.user_name IS '用户名';
COMMENT ON COLUMN owuser.tm_user.email IS '邮箱';
COMMENT ON COLUMN owuser.tm_user.create_date IS '创建时间';

-- 插入订单明细数据 tt_product_info
DROP TABLE owuser.tt_product_info;
CREATE TABLE owuser.tt_product_info
(
    id             NUMBER(10) PRIMARY KEY,
    product_name   VARCHAR2(255)   NOT NULL,
    price_per_unit NUMBER(10, 2) NOT NULL,
    product_descr  CLOB           NOT NULL,
    create_date    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建序列和触发器来模拟 SERIAL
CREATE SEQUENCE owuser.tt_product_info_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER owuser.tt_product_info_bir
BEFORE INSERT ON owuser.tt_product_info
FOR EACH ROW
BEGIN
SELECT owuser.tt_product_info_seq.NEXTVAL INTO :new.id FROM dual;
END;
/

-- 添加注释
COMMENT ON TABLE owuser.tt_product_info IS '产品信息表';
COMMENT ON COLUMN owuser.tt_product_info.id IS '产品ID';
COMMENT ON COLUMN owuser.tt_product_info.product_name IS '产品名称';
COMMENT ON COLUMN owuser.tt_product_info.price_per_unit IS '单价';
COMMENT ON COLUMN owuser.tt_product_info.product_descr IS '产品描述';
COMMENT ON COLUMN owuser.tt_product_info.create_date IS '创建时间';

-- 在 owuser schema 下创建表 tt_order
DROP TABLE owuser.tt_order;
CREATE TABLE owuser.tt_order (
                                 id           NUMBER(10) PRIMARY KEY,
                                 customer_id  NUMBER        NOT NULL,
                                 order_date   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 total_amount NUMBER(10, 2) NOT NULL
);

-- 创建序列和触发器来模拟 SERIAL
CREATE SEQUENCE owuser.tt_order_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER owuser.tt_order_bir
BEFORE INSERT ON owuser.tt_order
FOR EACH ROW
BEGIN
SELECT owuser.tt_order_seq.NEXTVAL INTO :new.id FROM dual;
END;
/

-- 添加注释
COMMENT ON TABLE owuser.tt_order IS '订单表';
COMMENT ON COLUMN owuser.tt_order.id IS '订单ID';
COMMENT ON COLUMN owuser.tt_order.customer_id IS '客户ID';
COMMENT ON COLUMN owuser.tt_order.order_date IS '订单日期';

-- 在 owuser schema 下创建表 tt_order_item
DROP TABLE owuser.tt_order_item;
CREATE TABLE owuser.tt_order_item
(
    id          NUMBER(10) PRIMARY KEY,
    order_id    NUMBER        NOT NULL,
    product_id  NUMBER        NOT NULL,
    quantity    NUMBER        NOT NULL,
    total_price NUMBER(10, 2) NOT NULL,
    order_descr CLOB,
    create_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES owuser.tt_order (id),
    FOREIGN KEY (product_id) REFERENCES owuser.tt_product_info (id)
);

-- 创建序列和触发器来模拟 SERIAL
CREATE SEQUENCE owuser.tt_order_item_seq START WITH 1 INCREMENT BY 1;
CREATE OR REPLACE TRIGGER owuser.tt_order_item_bir
BEFORE INSERT ON owuser.tt_order_item
FOR EACH ROW
BEGIN
SELECT owuser.tt_order_item_seq.NEXTVAL INTO :new.id FROM dual;
END;
/

-- 添加注释
COMMENT ON TABLE owuser.tt_order_item IS '订单明细表';
COMMENT ON COLUMN owuser.tt_order_item.id IS '订单明细ID';
COMMENT ON COLUMN owuser.tt_order_item.order_id IS '订单ID';
COMMENT ON COLUMN owuser.tt_order_item.product_id IS '产品ID';
COMMENT ON COLUMN owuser.tt_order_item.quantity IS '数量';
COMMENT ON COLUMN owuser.tt_order_item.total_price IS '总价';
COMMENT ON COLUMN owuser.tt_order_item.order_descr IS '订单描述';
COMMENT ON COLUMN owuser.tt_order_item.create_date IS '创建时间';