package com.example.dbquerymybatis.mapper;

import com.example.dbquerymybatis.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("""
            SELECT id, name, email, department, created_at AS createdAt
            FROM users
            ORDER BY id
            """)
    List<User> findAll();

    @Select("""
            SELECT id, name, email, department, created_at AS createdAt
            FROM users
            WHERE id = #{id}
            """)
    User findById(@Param("id") long id);

    @Select("SELECT COUNT(*) FROM users")
    long count();

    @Select("""
            SELECT id, name, email, department, created_at AS createdAt
            FROM users
            WHERE department = #{department}
            ORDER BY name
            """)
    List<User> findByDepartment(@Param("department") String department);
}
