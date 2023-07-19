package com.smzgo.reggie.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smzgo.reggie.common.R;
import com.smzgo.reggie.dto.DishDto;
import com.smzgo.reggie.entity.Category;
import com.smzgo.reggie.entity.Dish;
import com.smzgo.reggie.entity.DishFlavor;
import com.smzgo.reggie.service.CategoryService;
import com.smzgo.reggie.service.DishFlavorService;
import com.smzgo.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 保存菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        // 使用dto来封装dish与dishFlavor两张表的实体类
        dishService.saveDishWithFlavor(dishDto);

        // 清理redis缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        stringRedisTemplate.delete(key);

        return R.success("新增菜品成功！");
    }

    /**
     * 分页展示菜品
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        // 问题阐述：在前端菜品展示页面中，有一列为菜品分类名称展示，而在dish中没有菜品分类名称，此时需要使用dishDto而不是dish。
        // 需要将dish中的属性拷贝到dishDto中，并给dishDto附上菜品分类名称的属性。同理需要构造dishDtoPage。

        // 构造分页构造器
        Page<Dish> dishPage = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page,pageSize);

        // 构造条件构造器
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 构造查询条件
        lambdaQueryWrapper.like(name != null, Dish::getName, name);
        lambdaQueryWrapper.orderByDesc(Dish::getCreateTime);

        dishService.page(dishPage,lambdaQueryWrapper);

        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");

        // 构建新的records属性
        List<Dish> records = dishPage.getRecords();
        // 给records中的dishDto赋值，重点在分类名称
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).toList();
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 获得菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto byIdDishWithFlavor = dishService.getByIdDishWithFlavor(id);
        return R.success(byIdDishWithFlavor);
    }

    /**
     * 更新菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateDishWithFlavor(dishDto);

        // 清理redis缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        stringRedisTemplate.delete(key);

        return R.success("修改菜品成功");
    }


    /**
     * 获取菜品集合，包含菜品的分类名称以及菜品的口味信息
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;

        // 查询redis是否有菜品的缓存信息
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        // 使用Fastjson将List对象转换成支持json格式的String存储在redis中
        String dishDtoListJsonString = stringRedisTemplate.opsForValue().get(key);
        dishDtoList = JSON.parseArray(dishDtoListJsonString, DishDto.class);

        // 如果有则返回
        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }

        // 查询菜品基本信息
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(lambdaQueryWrapper);

        dishDtoList = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            // 添加菜品分类名称至dishDto
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            // 添加菜品口味信息至dishDto
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            flavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(flavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).toList();

        // 如果没有则存入redis中之后返回
        String jsonString = JSON.toJSONString(dishDtoList);
        stringRedisTemplate.opsForValue().set(key,jsonString,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }


}
