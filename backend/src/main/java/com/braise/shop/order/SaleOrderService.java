package com.braise.shop.order;

import com.braise.shop.common.BizException;
import com.braise.shop.common.Dict;
import com.braise.shop.goods.Goods;
import com.braise.shop.goods.GoodsRepository;
import com.braise.shop.goods.GoodsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SaleOrderService {

    private final SaleOrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final GoodsRepository goodsRepo;
    private final GoodsService goodsService;

    public SaleOrderService(SaleOrderRepository orderRepo, OrderItemRepository itemRepo,
                            GoodsRepository goodsRepo, GoodsService goodsService) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.goodsRepo = goodsRepo;
        this.goodsService = goodsService;
    }

    public List<SaleOrder> list() {
        return orderRepo.findAll();
    }

    public SaleOrder get(Long id) {
        return orderRepo.findById(id).orElseThrow(() -> new BizException("订单不存在"));
    }

    public List<OrderItem> items(Long orderId) {
        return itemRepo.findByOrderId(orderId);
    }

    @Transactional
    public SaleOrder create(SaleOrderCreate req) {
        if (req.items == null || req.items.isEmpty()) {
            throw new BizException("订单至少包含一条商品明细");
        }
        if (!Dict.isValidOrderChannel(req.channel)) {
            req.channel = "堂食";
        }
        SaleOrder order = new SaleOrder();
        order.channel = req.channel;
        order.takeNo = (req.takeNo == null || req.takeNo.isBlank()) ? "T" + System.currentTimeMillis() % 100000 : req.takeNo;
        order.status = "待制作";
        order.paid = (req.paid == null) ? false : req.paid;
        order.createdAt = LocalDateTime.now();

        SaleOrder saved = orderRepo.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (SaleOrderCreate.OrderItemInput it : req.items) {
            if (it.goodsId == null) {
                throw new BizException("明细缺少商品");
            }
            Goods g = goodsRepo.findById(it.goodsId).orElseThrow(() -> new BizException("商品不存在：" + it.goodsId));
            if (g.onSale == null || !g.onSale) {
                throw new BizException("下架商品不可加入订单：" + g.name);
            }
            if (it.qty == null || it.qty <= 0) {
                throw new BizException("明细数量必须大于 0");
            }
            OrderItem item = new OrderItem();
            item.orderId = saved.id;
            item.goodsId = g.id;
            item.qty = it.qty;
            item.unitPrice = g.price;
            total = total.add(g.price.multiply(BigDecimal.valueOf(it.qty)));
            items.add(item);
        }
        itemRepo.saveAll(items);
        saved.amount = total;
        return orderRepo.save(saved);
    }

    public SaleOrder updateStatus(Long id, String to) {
        SaleOrder o = orderRepo.findById(id).orElseThrow(() -> new BizException("订单不存在"));
        if (!Dict.canTransferOrder(o.status, to)) {
            throw new BizException("订单状态流转非法：已完成 / 已退单 为终态，且须按 待制作→制作中→待取 顺序流转");
        }
        o.status = to;
        return orderRepo.save(o);
    }

    public SaleOrder pay(Long id) {
        SaleOrder o = orderRepo.findById(id).orElseThrow(() -> new BizException("订单不存在"));
        if (!"待制作".equals(o.status) && !"制作中".equals(o.status) && !"待取".equals(o.status)) {
            throw new BizException("该状态订单不可支付");
        }
        o.paid = true;
        return orderRepo.save(o);
    }

    /** 退单：仅已支付且状态为「待取」可退；退单后回补商品库存，且退单为终态不可再改。 */
    @Transactional
    public SaleOrder refund(Long id) {
        SaleOrder o = orderRepo.findById(id).orElseThrow(() -> new BizException("订单不存在"));
        if (!Boolean.TRUE.equals(o.paid)) {
            throw new BizException("未支付的订单不可退单");
        }
        if (!"待取".equals(o.status)) {
            throw new BizException("仅「待取」状态且已支付的订单可退单");
        }
        // 回补库存
        List<OrderItem> items = itemRepo.findByOrderId(id);
        for (OrderItem it : items) {
            goodsService.restock(it.goodsId, it.qty == null ? 0 : it.qty);
        }
        o.status = "已退单";
        return orderRepo.save(o);
    }
}
