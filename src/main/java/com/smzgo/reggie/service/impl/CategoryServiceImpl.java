package com.smzgo.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smzgo.reggie.common.CustomException;
import com.smzgo.reggie.entity.Category;
import com.smzgo.reggie.entity.Dish;
import com.smzgo.reggie.entity.Setmeal;
import com.smzgo.reggie.mapper.CategoryMapper;
import com.smzgo.reggie.service.CategoryService;
import com.smzgo.reggie.service.DishService;
import com.smzgo.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    DishService dishService;

    @Autowired
    SetmealService setmealService;

    @Override
    public void remove(Long id) {

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        long count1 = dishService.count(dishLambdaQueryWrapper);

        // 是否有菜品关联了此分类
        if (count1 > 0) {
            throw new CustomException("已关联菜品，无法删除!");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        long count2 = setmealService.count(setmealLambdaQueryWrapper);

        // 是否有套餐关联了此分类
        if (count2 > 0) {
            throw new CustomException("已关联套餐，无法删除!");
        }

        super.removeById(id);

    }


}
