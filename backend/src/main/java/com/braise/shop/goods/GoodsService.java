package com.braise.shop.goods;

import com.braise.shop.common.BizException;
import com.braise.shop.common.Dict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoodsService {

    private final GoodsRepository repo;

    public GoodsService(GoodsRepository repo) {
        this.repo = repo;
    }

    public List<Goods> list() {
        return repo.findAll();
    }

    public List<Goods> listOnSale() {
        return repo.findByOnSale(true);
    }

    public Goods get(Long id) {
        return repo.findById(id).orElseThrow(() -> new BizException("商品不存在"));
    }

    public Goods create(Goods in) {
        if (in.name == null || in.name.isBlank()) {
            throw new BizException("品名不能为空");
        }
        if (!Dict.isValidGoodsCategory(in.category)) {
            throw new BizException("商品分类必须是：鸭脖 / 猪蹄 / 卤蛋 / 藕片 / 海带 / 豆干");
        }
        if (!Dict.isValidGoodsUnit(in.unit)) {
            throw new BizException("计价单位必须是：称重 / 按份");
        }
        if (!Dict.isValidGoodsSpicy(in.spicy)) {
            throw new BizException("辣度必须是：不辣 / 微辣 / 中辣 / 重辣");
        }
        if (in.price == null || in.price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("售价必须大于 0");
        }
        Goods g = new Goods();
        g.name = in.name;
        g.category = in.category;
        g.unit = in.unit;
        g.price = in.price;
        g.spicy = in.spicy;
        // 会员价默认等于售价；若传入则必须 ≤ 售价
        g.memberPrice = (in.memberPrice == null) ? in.price : in.memberPrice;
        if (g.memberPrice.compareTo(g.price) > 0) {
            throw new BizException("会员价不能高于售价");
        }
        g.onSale = (in.onSale == null) ? true : in.onSale;
        g.stock = (in.stock == null || in.stock < 0) ? 0 : in.stock;
        g.createdAt = LocalDateTime.now();
        return repo.save(g);
    }

    public Goods update(Long id, Goods in) {
        Goods g = repo.findById(id).orElseThrow(() -> new BizException("商品不存在"));
        if (in.name != null && !in.name.isBlank()) {
            g.name = in.name;
        }
        if (in.category != null && !in.category.isBlank()) {
            if (!Dict.isValidGoodsCategory(in.category)) {
                throw new BizException("商品分类必须是：鸭脖 / 猪蹄 / 卤蛋 / 藕片 / 海带 / 豆干");
            }
            g.category = in.category;
        }
        if (in.unit != null && !in.unit.isBlank()) {
            if (!Dict.isValidGoodsUnit(in.unit)) {
                throw new BizException("计价单位必须是：称重 / 按份");
            }
            g.unit = in.unit;
        }
        if (in.spicy != null && !in.spicy.isBlank()) {
            if (!Dict.isValidGoodsSpicy(in.spicy)) {
                throw new BizException("辣度必须是：不辣 / 微辣 / 中辣 / 重辣");
            }
            g.spicy = in.spicy;
        }
        if (in.price != null) {
            if (in.price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("售价必须大于 0");
            }
            g.price = in.price;
        }
        if (in.memberPrice != null) {
            g.memberPrice = in.memberPrice;
        }
        if (g.memberPrice != null && g.price != null && g.memberPrice.compareTo(g.price) > 0) {
            throw new BizException("会员价不能高于售价");
        }
        if (in.onSale != null) {
            g.onSale = in.onSale;
        }
        if (in.stock != null) {
            if (in.stock < 0) {
                throw new BizException("库存不能为负");
            }
            g.stock = in.stock;
        }
        return repo.save(g);
    }

    public Goods updateOnSale(Long id, Boolean onSale) {
        Goods g = repo.findById(id).orElseThrow(() -> new BizException("商品不存在"));
        g.onSale = onSale;
        return repo.save(g);
    }

    /** 批量下架：用于底部浮条的批量操作。 */
    public int batchOffSale(List<Long> ids) {
        int n = 0;
        for (Long id : ids) {
            Goods g = repo.findById(id).orElse(null);
            if (g != null) {
                g.onSale = false;
                repo.save(g);
                n++;
            }
        }
        return n;
    }

    /** 批量降价：会员价按比例（0~1）下调，且不得高于售价。 */
    public int batchDiscount(List<Long> ids, BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new BizException("折扣比例须在 0~1 之间");
        }
        int n = 0;
        for (Long id : ids) {
            Goods g = repo.findById(id).orElse(null);
            if (g != null && g.price != null) {
                g.memberPrice = g.price.multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP);
                repo.save(g);
                n++;
            }
        }
        return n;
    }

    /** 退单回补库存（订单模块调用）。 */
    public void restock(Long goodsId, int qty) {
        if (goodsId == null) {
            return;
        }
        Goods g = repo.findById(goodsId).orElse(null);
        if (g != null) {
            int cur = (g.stock == null) ? 0 : g.stock;
            g.stock = cur + qty;
            repo.save(g);
        }
    }
}
