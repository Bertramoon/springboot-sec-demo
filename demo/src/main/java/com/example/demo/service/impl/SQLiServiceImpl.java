package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.service.SQLiService;
import com.example.demo.vo.Response;
import com.example.demo.vo.sqli.SQLiRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SQLiServiceImpl implements SQLiService {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Override
    public List<User> jdbcBad3(SQLiRequest sqLiRequest) {
        Map<String, Object> conditions = sqLiRequest.getConditions();
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
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
