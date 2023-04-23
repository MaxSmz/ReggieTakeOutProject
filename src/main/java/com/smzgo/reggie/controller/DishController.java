package com.smzgo.reggie.controller;

import com.smzgo.reggie.common.R;
import com.smzgo.reggie.dto.DishDto;
import com.smzgo.reggie.service.DishFlavorService;
import com.smzgo.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        // 使用dto来封装dish与dishFlavor两张表的实体类
        dishService.saveDishWithFlavor(dishDto);
        return R.success("新增菜品成功！");
    }


}
