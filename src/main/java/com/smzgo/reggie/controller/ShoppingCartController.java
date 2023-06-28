package com.smzgo.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smzgo.reggie.common.BaseContext;
import com.smzgo.reggie.common.R;
import com.smzgo.reggie.entity.ShoppingCart;
import com.smzgo.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("添加购物车");

        // 获得当前用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 当前添加的是菜品
        if( dishId != null) {
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        } else {
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        ShoppingCart shoppingCartOne = shoppingCartService.getOne(lambdaQueryWrapper);
        // 如果存在，则在原有的基础上加一
        if(shoppingCartOne != null) {
            Integer number = shoppingCartOne.getNumber();
            shoppingCartOne.setNumber(number+1);
            shoppingCartService.updateById(shoppingCartOne);
        }else{
            // 如果不存在则直接添加
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            shoppingCartOne = shoppingCart;
        }

        return R.success(shoppingCartOne);
    }


}
