package com.braise.shop.order;

import java.math.BigDecimal;
import java.util.List;

/** 下单请求：订单主信息 + 明细列表（至少一条）。 */
public class SaleOrderCreate {

    public String channel;
    public String takeNo;
    public Boolean paid;
    public List<OrderItemInput> items;

    /** 明细输入：仅含 goodsId 与 qty，单价取商品当前售价。 */
    public static class OrderItemInput {
        public Long goodsId;
        public Integer qty;
    }
}
