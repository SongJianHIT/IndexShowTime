package com.itheima.stock.controller;

import com.itheima.stock.pojo.StockBusiness;
import com.itheima.stock.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description
 */
@RestController
@RequestMapping("/api/quot")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/stock/business/all")
    public List<StockBusiness> findAllBusinessInfo(){
       return stockService.findAll();
    }



}
