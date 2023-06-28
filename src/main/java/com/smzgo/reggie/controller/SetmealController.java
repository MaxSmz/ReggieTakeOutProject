package com.smzgo.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smzgo.reggie.common.R;
import com.smzgo.reggie.dto.SetmealDto;
import com.smzgo.reggie.entity.Category;
import com.smzgo.reggie.entity.Setmeal;
import com.smzgo.reggie.service.CategoryService;
import com.smzgo.reggie.service.SetmealService;
import com.smzgo.reggie.service.impl.SetmealDishServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishServiceImpl setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功！");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {

        // 构造setmeal page查询
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name != null, Setmeal::getName,name);
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,lambdaQueryWrapper);

        // 为了获得categoryName需要使用setmealDto以及一个新的dtoPage
        Page<SetmealDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).toList();

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    @DeleteMapping
    public R<String> remove(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("删除套菜成功！");
    }

    /**
     * 批量停售
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> blockSale(@RequestParam List<Long> ids){
        setmealService.updateSetmealStatusToBlock(ids);
        return R.success("批量停售成功！");
    }

    /**
     * 批量启售
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> openSale(@RequestParam List<Long> ids){
        setmealService.updateSetmealStatusToOpen(ids);
        return R.success("批量启售成功！");
    }

    /**
     * 获取套菜集合
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lambdaQueryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(lambdaQueryWrapper);
        return R.success(list);
    }

}
