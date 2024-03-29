package com.smzgo.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smzgo.reggie.entity.ShoppingCart;
import com.smzgo.reggie.mapper.ShoppingCartMapper;
import com.smzgo.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
