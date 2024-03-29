package com.smzgo.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.smzgo.reggie.common.R;
import com.smzgo.reggie.entity.User;
import com.smzgo.reggie.service.UserService;
import com.smzgo.reggie.utils.ValidateCodeUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RequestMapping("/user")
@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession httpSession) {
        // 获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            // 随机生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code = {}", code);
            // 使用阿里云发送验证码

            // 将生成的验证码保存到session 以便前端校验
            httpSession.setAttribute(phone,code);
            return R.success("短信发送成功！");
        }
        return R.success("短信发送失败");
    }

    /**
     * 移动端用户登陆
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map userPhoneCodeMap, HttpSession httpSession) {
        // 获取手机号
        String phone = userPhoneCodeMap.get("phone").toString();

        // 获取验证码
        String code = userPhoneCodeMap.get("code").toString();

        // 从session中获得验证码
        Object codeInSession = httpSession.getAttribute(phone);

        // 比对验证码
        if (codeInSession != null && codeInSession.equals(code)) {
            // 比对成功，登陆成功
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(lambdaQueryWrapper);
            // 若手机号不在user表中，则存入
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
                System.out.println("$$");
            }
            httpSession.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登陆失败");
    }
}
