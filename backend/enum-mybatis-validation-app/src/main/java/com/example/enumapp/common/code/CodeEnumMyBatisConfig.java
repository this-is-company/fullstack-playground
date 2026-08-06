package com.example.enumapp.common.code;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * classpath 에서 CodeEnum 을 구현한 Enum 을 모두 찾아 TypeHandler 로 등록한다.
 */
@Configuration
public class CodeEnumMyBatisConfig {

    private static final String BASE_PACKAGE = "com.example.enumapp";

    @Bean
    ConfigurationCustomizer codeEnumTypeHandlerCustomizer() {
        return configuration -> registerAllCodeEnums(configuration.getTypeHandlerRegistry());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void registerAllCodeEnums(TypeHandlerRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(CodeEnum.class));

        for (BeanDefinition bd : scanner.findCandidateComponents(BASE_PACKAGE)) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                if (clazz.isEnum() && CodeEnum.class.isAssignableFrom(clazz)) {
                    Class<? extends Enum> enumClass = (Class<? extends Enum>) clazz;
                    registry.register(enumClass, new CodeEnumTypeHandler(enumClass));
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Failed to load CodeEnum: " + bd.getBeanClassName(), e);
            }
        }
    }

    /** 테스트/검증용: 등록된 핸들러 존재 여부 확인 */
    public static boolean isRegistered(SqlSessionFactory factory, Class<? extends CodeEnum> enumType) {
        return factory.getConfiguration().getTypeHandlerRegistry().hasTypeHandler(enumType);
    }
}
