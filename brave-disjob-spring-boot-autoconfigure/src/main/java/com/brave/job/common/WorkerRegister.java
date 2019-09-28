package com.brave.job.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.brave.config.ClientConfiguration;
import com.brave.util.IpUtil;
import com.brave.util.JobUtil;
import com.brave.vo.JobProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author junzhang
 */
@Slf4j
public abstract class WorkerRegister {

    @Autowired
	ClientConfiguration clientConfiguration;
    @Autowired
	JobUtil jobUtil;

    /**
     * @param path
     * @param value
     */
    public void register(String path,String value){
        clientConfiguration.setNodeData(path,value);
    }

    /**
     * @param jobName
     */
    public void registerLog(String jobName) {
        JobProperty jobProperty = JobUtil.JOB_NODE_MAP.get(jobName);
        String exeDispatcherPath = jobProperty.getLock() + "/" + IpUtil.getLocalIP() + "-" + jobUtil.getPort() +"-worker";

        try {
            ClientConfiguration.curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(exeDispatcherPath);
        } catch (Exception e) {
            log.info("add job {} log exeception:{}",exeDispatcherPath,e);
        }
    }

    /**
     * @param jobName
     */
    public void unRegisterLog(String jobName) {
        JobProperty jobProperty = JobUtil.JOB_NODE_MAP.get(jobName);
        String exeDispatcherPath = jobProperty.getLock() + "/" + IpUtil.getLocalIP() + "-" + jobUtil.getPort() +"-worker";
        try {
            ClientConfiguration.curatorFramework.delete().forPath(exeDispatcherPath);
        } catch (Exception e) {
            log.info("delete job {} log exeception:{}",exeDispatcherPath,e);
        }
    }

    /**
     *
     * @param ids
     * @return
     */
     public List<Integer> processItem(@NotNull String ids) {
        List<Integer> result = new ArrayList<>();

        String[] idr = ids.replace("[","").replace("]","").split(",");
        if(idr.length == 0 || idr == null) {
            return null;
        }
        Arrays.stream(idr).forEach(id -> {
            result.add(Integer.parseInt(id));
        });
        return result;
    }

    public abstract void run(String ids);

    /**
     * @param ids
     * @param jobName
     */
    public void work(String ids,String jobName) {
        log.info("{} class name is {}",jobName,this.getClass().getName());
        registerLog(jobName);
        run(ids);
        unRegisterLog(jobName);
    }

//    /**
//     * 把worker现成注册上去。
//     */
//    @PostConstruct
//    public void init() {
//        String pkg = MainWorker.class.getName();
//        register("/"+jobName,pkg);
//    }

    public void init_1(String jobName) {
        String pkg = this.getClass().getName();
        register("/"+jobName,pkg);
    }

}
