package com.brave.annotation;

import java.lang.reflect.Field;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobNameProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {
        Field[] fields = bean.getClass().getFields();
        if (fields != null) {
            for (Field field : fields) {
                JobName myValue = AnnotationUtils.findAnnotation(field, JobName.class);
                if (myValue != null) {
                    String name = myValue.name();
                    field.setAccessible(true);
                    try {
                        field.set(bean, name);
                        field.setAccessible(false);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
        return bean;
    }

}
