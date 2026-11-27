package org.example.buffet.entity;

import java.math.BigDecimal;

public record Goods(Integer goodsId, String goodsName, BigDecimal price, String stockStatus, Integer stockQty) {
}
