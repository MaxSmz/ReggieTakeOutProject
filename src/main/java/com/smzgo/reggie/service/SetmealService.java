package com.smzgo.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smzgo.reggie.dto.SetmealDto;
import com.smzgo.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐并且同时保存套菜和菜品的关联关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);
}
