package com.smzgo.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smzgo.reggie.dto.SetmealDto;
import com.smzgo.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐并且同时保存套菜和菜品的关联关系
     * @param setmealDto
     */
    void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐并且同时删除套菜和菜品的关联关系
     * @param ids<Long>
     */
    void removeWithDish(List<Long> ids);

    /**
     * 批量停售
     * @param ids
     */
    void updateSetmealStatusToBlock(List<Long> ids);

    /**
     * 批量启售
     * @param ids
     */
    void updateSetmealStatusToOpen(List<Long> ids);
}
