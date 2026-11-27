package org.example.buffet.entity;

import java.math.BigDecimal;

public record Goods(String goodsId, String goodsName, BigDecimal price, String stockStatus, Integer stockQty) {
}
