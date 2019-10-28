package com.brave.annotation;

import java.lang.reflect.Field;

import com.brave.job.common.BaseWorkerRegister;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EnableBraveDisJobProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof BaseWorkerRegister) {
			EnableBraveDisJob enableBraveDisJob = AnnotationUtils.findAnnotation(bean.getClass(), EnableBraveDisJob.class);
			String jobName = enableBraveDisJob.name();
			try {
				Field field = bean.getClass().getDeclaredField("jobName");
				field.setAccessible(true);
				field.set(bean,jobName);
				field.setAccessible(false);
				((BaseWorkerRegister) bean).init_1(jobName);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bean;
	}
}
