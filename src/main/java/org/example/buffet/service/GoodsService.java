package org.example.buffet.service;

import org.example.buffet.entity.Goods;
import org.example.buffet.mapper.GoodsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GoodsService {
    private static final String IN_STOCK = "有货";
    private static final String OUT_OF_STOCK = "无货";
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");

    private final GoodsMapper goodsMapper;

    public GoodsService(GoodsMapper goodsMapper) {
        this.goodsMapper = goodsMapper;
    }

    public List<Goods> list() {
        return goodsMapper.findAll();
    }

    public List<Goods> listInStock() {
        return goodsMapper.findByStockStatus(IN_STOCK);
    }

    @Transactional
    public Goods add(Goods goods) {
        validateId(goods.goodsId());
        if (goods.goodsName() == null || goods.goodsName().isBlank()
                || goods.goodsName().codePointCount(0, goods.goodsName().length()) > 255) {
            throw new IllegalArgumentException("商品名称须为 1 到 255 个字符");
        }
        validatePrice(goods.price());
        validateStatus(goods.stockStatus());

        int quantity = goods.stockQty() == null ? 0 : goods.stockQty();
        if (quantity < 0) {
            throw new IllegalArgumentException("库存数量不得小于 0");
        }
        Goods created = new Goods(goods.goodsId(), goods.goodsName(), goods.price().setScale(2),
                goods.stockStatus(), quantity);
        goodsMapper.insert(created);
        return created;
    }

    @Transactional
    public Goods update(Integer goodsId, BigDecimal price, String stockStatus) {
        validateId(goodsId);
        validatePrice(price);
        validateStatus(stockStatus);

        Goods existing = requireGoodsForUpdate(goodsId);
        BigDecimal updatedPrice = price.setScale(2);
        goodsMapper.update(existing.goodsId(), updatedPrice, stockStatus);
        return new Goods(existing.goodsId(), existing.goodsName(), updatedPrice, stockStatus, existing.stockQty());
    }

    @Transactional
    public String updateStatus(Integer goodsId, String stockStatus) {
        validateId(goodsId);
        validateStatus(stockStatus);

        Goods existing = requireGoodsForUpdate(goodsId);
        goodsMapper.updateStatus(existing.goodsId(), stockStatus);
        return stockStatus;
    }

    @Transactional
    public void delete(Integer goodsId) {
        validateId(goodsId);
        if (goodsMapper.delete(goodsId) == 0) {
            throw new GoodsNotFoundException(goodsId);
        }
    }

    private Goods requireGoodsForUpdate(Integer goodsId) {
        Goods goods = goodsMapper.findForUpdate(goodsId);
        if (goods == null) {
            throw new GoodsNotFoundException(goodsId);
        }
        return goods;
    }

    private static void validateId(Integer goodsId) {
        if (goodsId == null || goodsId < 1) {
            throw new IllegalArgumentException("商品编号须为正整数");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0 || price.compareTo(MAX_PRICE) > 0
                || price.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("商品价格须在 0 到 9999999999.99 之间，最多两位小数");
        }
    }

    private static void validateStatus(String stockStatus) {
        if (!IN_STOCK.equals(stockStatus) && !OUT_OF_STOCK.equals(stockStatus)) {
            throw new IllegalArgumentException("库存状态须为有货或无货");
        }
    }

    public static class GoodsNotFoundException extends RuntimeException {
        public GoodsNotFoundException(Integer goodsId) {
            super("商品不存在：" + goodsId);
        }
    }
}
