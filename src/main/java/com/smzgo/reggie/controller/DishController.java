package com.smzgo.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smzgo.reggie.common.R;
import com.smzgo.reggie.dto.DishDto;
import com.smzgo.reggie.entity.Category;
import com.smzgo.reggie.entity.Dish;
import com.smzgo.reggie.service.CategoryService;
import com.smzgo.reggie.service.DishFlavorService;
import com.smzgo.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.callback.LanguageCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        // 使用dto来封装dish与dishFlavor两张表的实体类
        dishService.saveDishWithFlavor(dishDto);
        return R.success("新增菜品成功！");
    }


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


}
