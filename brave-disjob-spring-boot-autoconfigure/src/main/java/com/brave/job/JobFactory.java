package com.brave.job;


import com.brave.job.common.WorkerRegister;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author junzhang
 */
@Component
public class JobFactory implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        if(JobFactory.applicationContext == null ) {
            JobFactory.applicationContext = applicationContext;
        }
    }

    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过name获取 Bean.
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }

    public void process(String data,String path,String jobName) {
        WorkerRegister workerRegister = null;
        try {
            workerRegister = (WorkerRegister) getBean(Class.forName(path));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        workerRegister.work(data,jobName);
    }

}
