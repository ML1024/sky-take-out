package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行公共字段自动填充
 */

@Target(ElementType.METHOD)  //指定注解只能加在方法上
@Retention(RetentionPolicy.RUNTIME) //RUNTIME表示这个注解运行时生效
public @interface AutoFill {

    //指定当前数据库的操作类型，通过枚举的方式指定
    //枚举已经定义好了，在 sky-common/enumeration/OperationType
    OperationType value();
}


