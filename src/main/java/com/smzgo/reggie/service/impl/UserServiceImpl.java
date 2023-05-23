package com.smzgo.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smzgo.reggie.entity.User;
import com.smzgo.reggie.mapper.UserMapper;
import com.smzgo.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
