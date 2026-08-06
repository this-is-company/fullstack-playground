package com.example.dbquerymybatis;

import com.example.dbquerymybatis.mapper.UserMapper;
import com.example.dbquerymybatis.model.QueryResult;
import com.example.dbquerymybatis.model.User;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 기반 DB 조회 서비스. Nexus에 배포 후 Maven/Gradle 프로젝트에서 소비한다.
 */
public class MybatisDbQueryService {

    private final UserMapper userMapper;

    public MybatisDbQueryService(UserMapper userMapper) {
        Assert.notNull(userMapper, "userMapper must not be null");
        this.userMapper = userMapper;
    }

    public List<User> findAllUsers() {
        return userMapper.findAll();
    }

    public Optional<User> findUserById(long id) {
        return Optional.ofNullable(userMapper.findById(id));
    }

    public long countUsers() {
        return userMapper.count();
    }

    public List<User> findUsersByDepartment(String department) {
        Assert.hasText(department, "department must not be blank");
        return userMapper.findByDepartment(department);
    }

    public QueryResult<User> queryUsersByDepartment(String department) {
        List<User> rows = findUsersByDepartment(department);
        return new QueryResult<>(rows.size(), rows);
    }
}
