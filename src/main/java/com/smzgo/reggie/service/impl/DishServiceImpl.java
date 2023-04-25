package com.smzgo.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smzgo.reggie.dto.DishDto;
import com.smzgo.reggie.entity.Dish;
import com.smzgo.reggie.entity.DishFlavor;
import com.smzgo.reggie.mapper.DishMapper;
import com.smzgo.reggie.service.DishFlavorService;
import com.smzgo.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;


    @Transactional // 设计多张表的操作，需要使用事务管理器
    @Override
    public void saveDishWithFlavor(DishDto dishDto) {
        // 存储菜品
        this.save(dishDto);

        // 绑定菜品id至菜品口味，因为前端要求上传的json数据并没有将dishId绑定到dishFlavor，但是在dishFlavor表中需要绑定dishID。
        // 因此我们需要将dto中属于dishFlavor表中的数据拿出，并绑定dishId属性
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        // 存储菜品口味
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查菜品的信息和对应的口味信息
     * @param id
     */
    @Override
    public DishDto getByIdDishWithFlavor(Long id) {
        // 查询菜品基本信息，从dish表获取
        Dish dish = dishService.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        // 查询菜品口味信息，从dish_flavor表获取
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> list = dishFlavorService.list(lambdaQueryWrapper);
        dishDto.setFlavors(list);
        return dishDto;
    }

    @Transactional
    @Override
    public void updateDishWithFlavor(DishDto dishDto) {
        // 更新菜品信息
        this.updateById(dishDto);

        // 更新口味信息
        // 删除之前的口味信息
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);

        // 保存最新的口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
        item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}
