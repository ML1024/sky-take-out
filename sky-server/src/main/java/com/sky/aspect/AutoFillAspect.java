package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面：统一拦截加入AutoFill注解的方法，实现公共字段自动填充处理逻辑
 */
@Aspect
@Component //切面类也是一个bean，要交给spring容器管理
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点：对哪些类的哪些方法进行拦截
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {

    }

    /**
     * 前置通知：在通知当中进行公共字段的赋值
     */
    @Before("autoFillPointCut()") //表示匹配上切入点表达式autoFillPointCut的时候，执行通知的方法
    public void autoFill(JoinPoint joinPoint) { //参数：连接点JoinPoint。通过连接点，知道当前哪个方法被拦截到了，以及该方法的参数
        log.info("开始进行公共字段自动填充...");

        //1.获取到当前被拦截的方法的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型 INSERT或UPDATE

        //2.获取到当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) { //防止出现空指针，先判断有没有参数
            return;
        }

        Object entity = args[0]; //实体类型是不确定的，要用Object接收
                                 //我们做了约定实体放在第一位参数

        //3.准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //4.根据当前不同的操作类型，为对应的属性通过反射来赋值
        if (operationType == OperationType.INSERT) {
            //为4个公共字段赋值
            try {
                //Object类型是父类，父类没有子类的方法，只能通过反射获取子类的方法
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                //通过反射为对象属性赋值（因为不知道entity是什么类型的，无法调用set方法）
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (operationType == OperationType.UPDATE) {
            //为2个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);

                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

