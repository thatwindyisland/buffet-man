package org.example.buffet.controller;

import org.example.buffet.entity.Goods;
import org.example.buffet.service.GoodsService;
import org.example.buffet.service.GoodsService.GoodsNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping("/list")
    public List<Goods> list() {
        return goodsService.list();
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public Goods add(@RequestBody Goods goods) {
        return goodsService.add(goods);
    }

    @PutMapping("/{goodsId}")
    public Goods update(@PathVariable("goodsId") String goodsId, @RequestBody GoodsUpdate request) {
        return goodsService.update(goodsId, request.price(), request.stockStatus());
    }

    @DeleteMapping("/{goodsId}")
    public Map<String, Object> delete(@PathVariable("goodsId") String goodsId) {
        goodsService.delete(goodsId);
        return Map.of("goodsId", goodsId, "deleted", true);
    }

    @PutMapping("/status/{goodsId}")
    public StockUpdate updateStatus(@PathVariable("goodsId") String goodsId, @RequestBody StockUpdate request) {
        return new StockUpdate(goodsService.updateStatus(goodsId, request.stockStatus()));
    }

    @GetMapping("/stock")
    public List<Goods> listInStock() {
        return goodsService.listInStock();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidInput(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidJson() {
        return Map.of("error", "请求体须为可解析的 JSON");
    }

    @ExceptionHandler(GoodsNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(GoodsNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> duplicateId() {
        return Map.of("error", "商品编号已存在");
    }

    public record GoodsUpdate(BigDecimal price, String stockStatus) {
    }

    public record StockUpdate(String stockStatus) {
    }
}
