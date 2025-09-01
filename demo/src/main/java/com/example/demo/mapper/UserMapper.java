package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    User getUserById(int id);

    User getUserByUsername(String username);

    List<User> getUser(@Param("map") Map<String, Object> map);

    @Select("select * from user order by ${orderBy}")
    List<User> getSortedUser(String orderBy);
}
