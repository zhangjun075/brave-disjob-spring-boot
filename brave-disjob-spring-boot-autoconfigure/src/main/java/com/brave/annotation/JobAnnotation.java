package com.brave.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class JobAnnotation {

    @Around("@annotation(job)")
    public Object init(ProceedingJoinPoint joinPoint, Job job) throws Throwable {

        String name = job.name();

        Object[] args = joinPoint.getArgs();
        args[0] = name;
        joinPoint.proceed(args);
        return null;
    }

}
