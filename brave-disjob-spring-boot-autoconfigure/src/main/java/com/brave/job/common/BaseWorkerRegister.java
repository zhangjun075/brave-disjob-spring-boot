package com.brave.job.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.brave.config.ClientConfiguration;
import com.brave.util.IpUtil;
import com.brave.util.JobUtil;
import com.brave.vo.JobProperty;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author junzhang
 */
@Slf4j
public abstract class BaseWorkerRegister {

    @Autowired
	ClientConfiguration clientConfiguration;
    @Autowired
	JobUtil jobUtil;

    /**
     * @param path
     * @param value
     */
	public void register(String path,String value) throws Exception {
		String classPackageName = clientConfiguration.getNodeDate(path);
		//如果是空的，就直接注册，如果不是空的，节点的包名必须一致，否则要抛异常。
		if(Strings.isNullOrEmpty(classPackageName)) {
			clientConfiguration.setNodeData(path,value);
		} else {
			if( !classPackageName.equals(value)) {
				throw new Exception("集群中worker节点的包名不一致。");
			}
		}

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

	/**
	 * 不同的实现类不同的实现方法
	 * @param ids
	 */
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

    public void init_1(String jobName) throws Exception{
        String pkg = this.getClass().getName();
        register("/"+jobName,pkg);
    }

}
