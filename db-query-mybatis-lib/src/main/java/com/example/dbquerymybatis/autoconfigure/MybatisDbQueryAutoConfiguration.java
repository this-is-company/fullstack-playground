package com.example.dbquerymybatis.autoconfigure;

import com.example.dbquerymybatis.MybatisDbQueryService;
import com.example.dbquerymybatis.mapper.UserMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = MybatisAutoConfiguration.class)
@ConditionalOnClass({Mapper.class, UserMapper.class})
@MapperScan("com.example.dbquerymybatis.mapper")
public class MybatisDbQueryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisDbQueryService mybatisDbQueryService(UserMapper userMapper) {
        return new MybatisDbQueryService(userMapper);
    }
}
