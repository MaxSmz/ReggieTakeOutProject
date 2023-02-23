package com.smzgo.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smzgo.reggie.common.R;
import com.smzgo.reggie.entity.Employee;
import com.smzgo.reggie.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登陆 
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,
                             @RequestBody Employee employee) {
        
        // 1.将页面提交过来的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.根据页面提交的用户名查询数据库，判断用户是否注册
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());

        Employee emp = employeeService.getOne(queryWrapper);

        // 3.如果没有查询到则返回登陆失败结果
        if (emp == null) {
            return R.error("用户名无效");
        }

       // 4.密码比对不一致，返回失败结果
       if (!emp.getPassword().equals(password)) {
           return R.error("密码不一致");
       }

       // 5.查看员工状态，若已被禁用则返回错误
        if (emp.getStatus() == 0) {
            return R.error("账号已被禁用");
        }

        // 6.登陆成功，将员工id存入session
        request.getSession().setAttribute("employee",emp.getId());

        return R.success(emp);
    }
    // 员工登出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    // 新增员工
    @PostMapping
    public R<String> save(HttpServletRequest request,
                          @RequestBody Employee employee) {
        log.info("新增员工,员工信息：{}",employee.toString());

        // 设置员工初始密码
        String password = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(password);

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());


        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
        employeeService.save(employee);

        return R.success("新增员工成功");
    }



}
