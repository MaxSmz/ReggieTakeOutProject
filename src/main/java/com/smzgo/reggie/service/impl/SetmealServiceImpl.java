package com.smzgo.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smzgo.reggie.dto.SetmealDto;
import com.smzgo.reggie.entity.Setmeal;
import com.smzgo.reggie.entity.SetmealDish;
import com.smzgo.reggie.mapper.SetmealMapper;
import com.smzgo.reggie.service.SetmealDishService;
import com.smzgo.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐并且同时保存套菜和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐信息至setmeal表 insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 保存关联关系信息至setmeal_dish表 insert操作
        setmealDishService.saveBatch(setmealDishes);

    }
}
