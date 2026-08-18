package com.example.demo.domain;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.demo.domain.QProduct.product;

@Repository
public class ProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ProductQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<Product> findAll() {
        return queryFactory.selectFrom(product)
                .orderBy(product.id.asc())
                .fetch();
    }

    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(product)
                        .where(product.id.eq(id))
                        .fetchOne()
        );
    }
}
