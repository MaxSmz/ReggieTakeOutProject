package com.smzgo.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smzgo.reggie.entity.DishFlavor;
import com.smzgo.reggie.mapper.DishFlavorMapper;
import com.smzgo.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
