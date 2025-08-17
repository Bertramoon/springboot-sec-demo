package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deserialize")
public class DeserializeController {
    @PostMapping("/fastjson-bad-1")
    @ResponseBody
    public JSONObject deserialize(@RequestBody String json) {
        return JSON.parseObject(json, Feature.SupportNonPublicField);
    }
}
