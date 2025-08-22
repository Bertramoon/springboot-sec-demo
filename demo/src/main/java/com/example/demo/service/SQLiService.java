package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.vo.sqli.SQLiRequest;

import java.util.List;

public interface SQLiService {
    public List<User> jdbcBad3(SQLiRequest sqLiRequest);
}
