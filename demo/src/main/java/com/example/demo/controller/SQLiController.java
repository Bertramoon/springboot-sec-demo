package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.SQLiService;
import com.example.demo.vo.Response;
import com.example.demo.vo.sqli.SQLiRequest;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sqli")
public class SQLiController {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Resource
    private SQLiService sqliService;

    @PostMapping("/jdbc-bad-1")
    public Response<List<User>> jdbcBad1(@RequestBody SQLiRequest request) {
        Map<String, Object> conditions = request.getConditions();
        List<User> result = new ArrayList<>();
        // 获取JDBC连接并执行SQL查询
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // 执行SQL查询
            String sql = "SELECT * FROM user WHERE 1=1";
            if (conditions != null && !conditions.isEmpty()) {
                for (Map.Entry<String, Object> condition : conditions.entrySet()) {
                    sql += " AND " + condition.getKey() + " = \"" + condition.getValue() + "\"";
                }
            }
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                // 处理查询结果
                while (resultSet.next()) {
                    // 从结果集中获取数据
                    int id = resultSet.getInt("id");
                    String username = resultSet.getString("username");
                    result.add(new User(id, username, null));
                }
            }
            return Response.success(result);
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping("/jdbc-bad-2")
    public Response<List<User>> jdbcBad2(@RequestBody SQLiRequest request) {
        Map<String, Object> conditions = request.getConditions();
        List<Object> args = new ArrayList<>();
        List<User> result = new ArrayList<>();
        // 获取JDBC连接并执行SQL查询
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // 执行SQL查询
            String sql = "SELECT * FROM user WHERE 1=1";
            if (conditions != null && !conditions.isEmpty()) {
                for (Map.Entry<String, Object> condition : conditions.entrySet()) {
                    sql += " AND " + condition.getKey() + " = ?";
                    args.add(condition.getValue());
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < args.size(); i++) {
                    statement.setObject(i + 1, args.get(i));
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    // 处理查询结果
                    while (resultSet.next()) {
                        // 从结果集中获取数据
                        int id = resultSet.getInt("id");
                        String username = resultSet.getString("username");
                        result.add(new User(id, username, null));
                    }
                }
            }
            return Response.success(result);
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 使用service层调用
     */
    @PostMapping("/jdbc-bad-3")
    public Response<List<User>> jdbcBad3(@RequestBody SQLiRequest request) {
        return Response.success(sqliService.jdbcBad3(request));
    }

    @PostMapping("/jdbc-good-1")
    public Response<List<User>> jdbcGood1(@RequestBody SQLiRequest request) {
        Map<String, Object> conditions = request.getConditions();
        List<Object> args = new ArrayList<>();
        List<User> result = new ArrayList<>();
        // 获取JDBC连接并执行SQL查询
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // 执行SQL查询
            String sql = "SELECT * FROM user WHERE 1=1";
            if (conditions != null && !conditions.isEmpty()) {
                for (Map.Entry<String, Object> condition : conditions.entrySet()) {
                    // 检查字段名是否在User类的属性中
                    if (Arrays.stream(User.class.getDeclaredFields()).noneMatch(field -> field.getName().equals(condition.getKey()))) {
                        return Response.fail("字段名不存在");
                    }
                    sql += " AND " + condition.getKey() + " = ?";
                    args.add(condition.getValue());
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < args.size(); i++) {
                    statement.setString(i + 1, (String) args.get(i));
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    // 处理查询结果
                    while (resultSet.next()) {
                        // 从结果集中获取数据
                        int id = resultSet.getInt("id");
                        String username = resultSet.getString("username");
                        result.add(new User(id, username, null));
                    }
                }
            }
            return Response.success(result);
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }
}
