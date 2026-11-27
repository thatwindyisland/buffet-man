package org.example.buffet.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.buffet.entity.Goods;

import java.math.BigDecimal;
import java.util.List;

public interface GoodsMapper {
    List<Goods> findAll();

    List<Goods> findByStockStatus(@Param("stockStatus") String stockStatus);

    Goods findForUpdate(@Param("goodsId") Integer goodsId);

    void insert(Goods goods);

    void update(@Param("goodsId") Integer goodsId, @Param("price") BigDecimal price,
                @Param("stockStatus") String stockStatus);

    void updateStatus(@Param("goodsId") Integer goodsId, @Param("stockStatus") String stockStatus);

    int delete(@Param("goodsId") Integer goodsId);
}
