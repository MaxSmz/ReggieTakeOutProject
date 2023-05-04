package com.smzgo.reggie.dto;


import com.smzgo.reggie.entity.Setmeal;
import com.smzgo.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
