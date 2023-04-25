package com.smzgo.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smzgo.reggie.dto.DishDto;
import com.smzgo.reggie.entity.Dish;
import com.smzgo.reggie.entity.DishFlavor;

public interface DishService extends IService<Dish> {

    // 保存dish与dishFlavor
    void saveDishWithFlavor(DishDto dishDto);

    // 根据id查菜品的信息和对应的口味信息
    DishDto getByIdDishWithFlavor(Long id);

    void updateDishWithFlavor(DishDto dishDto);

}
