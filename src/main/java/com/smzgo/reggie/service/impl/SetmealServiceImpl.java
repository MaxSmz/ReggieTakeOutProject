package com.smzgo.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smzgo.reggie.common.CustomException;
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

    /**
     * 删除套餐并且同时删除套菜和菜品的关联关系
     * @param ids<Long>
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        // 查询套菜状态确定是否可以删除
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId,ids);
        lambdaQueryWrapper.eq(Setmeal::getStatus,1);

        // 如果不能删除，则抛出异常
        long count = this.count(lambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不可以删除!");
        }

        // 可以删除,先删除套餐表中的信息
        this.removeByIds(ids);

        // 再删除关系表中的信息
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper1);
    }

    /**
     * 批量停售
     * @param ids
     */
    @Override
    public void updateSetmealStatusToBlock(List<Long> ids) {
        LambdaUpdateWrapper<Setmeal> setmealLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        setmealLambdaUpdateWrapper.in(Setmeal::getId, ids);
        setmealLambdaUpdateWrapper.set(Setmeal::getStatus,0);
        this.update(setmealLambdaUpdateWrapper);
    }

    /**
     * 批量启售
     * @param ids
     */
    @Override
    public void updateSetmealStatusToOpen(List<Long> ids) {
        LambdaUpdateWrapper<Setmeal> setmealLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        setmealLambdaUpdateWrapper.in(Setmeal::getId, ids);
        setmealLambdaUpdateWrapper.set(Setmeal::getStatus,1);
        this.update(setmealLambdaUpdateWrapper);
    }
}
