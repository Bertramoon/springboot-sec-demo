package com.example.demo.vo.sqli;

import lombok.Data;

import java.util.Map;

@Data
public class SQLiRequest {
    private Map<String, Object> conditions;
}
