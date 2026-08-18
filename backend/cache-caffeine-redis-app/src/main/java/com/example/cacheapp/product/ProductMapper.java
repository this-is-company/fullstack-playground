package com.example.cacheapp.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product findById(@Param("id") Long id);

    List<Product> findAll();

    int updateStock(@Param("id") Long id, @Param("stock") int stock);
}
