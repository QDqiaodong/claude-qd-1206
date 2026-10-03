package com.braise.shop.common;

import java.util.List;
import java.util.Map;

/**
 * 卤味熟食店业务词典与规则参数表：卤味商品分类 / 计价单位 / 辣度、卤制批次状态流转、
 * 销售订单渠道 / 状态流转、原料进货材料类别 / 验收状态。规则集中在此，便于出题时被要求调整或扩展。
 */
public final class Dict {

    private Dict() {
    }

    // ---------------- 卤味商品 ----------------
    /** 鸭脖 / 猪蹄 / 卤蛋 / 藕片 / 海带 / 豆干 */
    public static final List<String> GOODS_CATEGORIES = List.of("鸭脖", "猪蹄", "卤蛋", "藕片", "海带", "豆干");

    /** 称重 / 按份 */
    public static final List<String> GOODS_UNITS = List.of("称重", "按份");

    /** 不辣 / 微辣 / 中辣 / 重辣 */
    public static final List<String> GOODS_SPICY = List.of("不辣", "微辣", "中辣", "重辣");

    // ---------------- 卤制批次 ----------------
    /** 卤制中 / 冷却中 / 已封装 / 已报废 */
    public static final List<String> BATCH_STATUS = List.of("卤制中", "冷却中", "已封装", "已报废");

    /**
     * 批次状态机：卤制中可转冷却中或报废；冷却中可转已封装或报废；
     * 已封装 / 已报废 均为终态（已报废不可再封装）。
     */
    public static final Map<String, List<String>> BATCH_FLOW = Map.of(
            "卤制中", List.of("冷却中", "已报废"),
            "冷却中", List.of("已封装", "已报废"),
            "已封装", List.of(),
            "已报废", List.of());

    // ---------------- 销售订单 ----------------
    /** 堂食 / 外卖 / 预订 */
    public static final List<String> ORDER_CHANNELS = List.of("堂食", "外卖", "预订");

    /** 待制作 / 制作中 / 待取 / 已完成 / 已退单 */
    public static final List<String> ORDER_STATUS = List.of("待制作", "制作中", "待取", "已完成", "已退单");

    /**
     * 订单状态机：待制作→制作中→待取；待取可转已完成或已退单；
     * 已完成 / 已退单 为终态（已退单不可再改）。
     */
    public static final Map<String, List<String>> ORDER_FLOW = Map.of(
            "待制作", List.of("制作中"),
            "制作中", List.of("待取"),
            "待取", List.of("已完成", "已退单"),
            "已完成", List.of(),
            "已退单", List.of());

    // ---------------- 原料进货 ----------------
    /** 冻禽 / 香料 / 素菜 / 油料 */
    public static final List<String> STOCK_MATERIALS = List.of("冻禽", "香料", "素菜", "油料");

    /** 待验收 / 合格 / 退回 */
    public static final List<String> ACCEPT_STATUS = List.of("待验收", "合格", "退回");

    // ---------------- 取值方法 ----------------

    public static boolean isValidGoodsCategory(String c) {
        return c != null && GOODS_CATEGORIES.contains(c);
    }

    public static boolean isValidGoodsUnit(String u) {
        return u != null && GOODS_UNITS.contains(u);
    }

    public static boolean isValidGoodsSpicy(String s) {
        return s != null && GOODS_SPICY.contains(s);
    }

    public static boolean isValidBatchStatus(String s) {
        return s != null && BATCH_STATUS.contains(s);
    }

    public static boolean isValidOrderChannel(String c) {
        return c != null && ORDER_CHANNELS.contains(c);
    }

    public static boolean isValidOrderStatus(String s) {
        return s != null && ORDER_STATUS.contains(s);
    }

    public static boolean isValidStockMaterial(String m) {
        return m != null && STOCK_MATERIALS.contains(m);
    }

    public static boolean isValidAcceptStatus(String s) {
        return s != null && ACCEPT_STATUS.contains(s);
    }

    /**
     * 批次状态流转校验：已封装 / 已报废 为终态不可再流转；否则须按 BATCH_FLOW 前进。
     */
    public static boolean canTransferBatch(String from, String to) {
        if (!isValidBatchStatus(from) || !isValidBatchStatus(to)) {
            return false;
        }
        List<String> next = BATCH_FLOW.get(from);
        return next != null && next.contains(to);
    }

    /**
     * 订单状态流转校验：已完成 / 已退单 为终态不可再流转；否则须按 ORDER_FLOW 前进。
     */
    public static boolean canTransferOrder(String from, String to) {
        if (!isValidOrderStatus(from) || !isValidOrderStatus(to)) {
            return false;
        }
        List<String> next = ORDER_FLOW.get(from);
        return next != null && next.contains(to);
    }
}
