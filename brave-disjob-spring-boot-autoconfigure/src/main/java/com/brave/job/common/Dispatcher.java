package com.brave.job.common;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.brave.config.ClientConfiguration;
import com.brave.util.IpUtil;
import com.brave.util.JobUtil;
import com.brave.vo.JobLogPojo;
import com.brave.vo.JobProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class Dispatcher {
    public static int jobId = 0;

    @Autowired
	ClientConfiguration dispatchClient;

    @Autowired
	JobUtil jobUtil;
    /**
     * 需要被重写
     */
    public void dispatchWork() {
        log.info("common job main");
    }

    public void run(String jobName) {

        //写日志
        JobProperty jobProperty = JobUtil.JOB_NODE_MAP.get(jobName);
        String exeDispatcherPath = jobProperty.getLock() + "/" + IpUtil.getLocalIP() + "-" + jobUtil.getPort() +"-dispatcher";

        try {
            List<String> childNodes = ClientConfiguration.curatorFramework.getChildren().forPath(jobProperty.getLock());
            if (childNodes != null && childNodes.size() >0) {
                log.info("已经有任务在执行，暂时不执行，等待下一次触发");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("{} dispatch worker begin to acquire lock.... ",jobName);
        InterProcessMutex mutex = ClientConfiguration.mutexConcurrentHashMap.get(jobName);
        if(mutex == null) {
            log.info("mutex initial failed,break this time");
            return;
        }
        try {
            boolean flag = mutex.acquire(1, TimeUnit.MICROSECONDS);
            if(flag) {

                //记录日志
                JobLogPojo jobLogPojo = JobLogPojo.builder().jobName(jobName).jobType(JobUtil.MAIN_JOB_TYPE).executor(
                    JobUtil.JOB_NODE_MAP.get(jobName).getExecutor()).build();
                jobId = jobUtil.addJobLog(jobLogPojo);

                log.info("{} acquire this lock.",jobName);
                ClientConfiguration.curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(exeDispatcherPath);

                //防止机器运行速度过快，导致在秒级别同时跑多次，所以会延迟10秒加载
                TimeUnit.SECONDS.sleep(10);
                //todo 记录日志表，时间点 、机器Ip、获取了、任务名称
                //获取了锁，这个时候开始去获取数据进行分片。
                dispatchWork();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                ClientConfiguration.curatorFramework.delete().forPath(exeDispatcherPath);
                mutex.release();
                log.info("release this lock...");
            } catch (Exception e) {
                log.info("release mutex exception..");
            }
        }
    }

    /**
     *
     */
    public synchronized void writeWorkerData(String jobName,List<Integer> jobs) {
        //获取目前可执行任务机器数
        Optional<List<String>>
            subNodes = dispatchClient.getSubNodes(JobUtil.JOB_NODE_MAP.get(jobName).getRoot());
        int sumWorker = subNodes.map(list -> list.size()).orElse(0);
        if(sumWorker == 0) {
            log.info("目前可执行任务机器数是0，自动退出，待有机器注册，再执行");
            //记录日志
            JobLogPojo jobLogPojo = JobLogPojo.builder().id(jobId).exeTime("--").build();
            jobUtil.modifyJobLog(jobLogPojo);
            return;
        }
        List<String> workers = subNodes.get();
        Map<String,List<Integer>> jobsAllocateResult = null;
        //按规则计算分片
        if(jobs != null && jobs.size() >0) {
            jobsAllocateResult = JobUtil.allotOfAverage_2(workers,jobs.stream().sorted().collect(
                Collectors.toList()));
        }
        if(jobsAllocateResult == null ) {
            log.info("分配失败");
            //记录日志
            JobLogPojo jobLogPojo = JobLogPojo.builder().id(jobId).exeTime("xx").build();
            jobUtil.modifyJobLog(jobLogPojo);
            return;
        }else{

            //记录日志
            JobLogPojo jobLogPojo = JobLogPojo.builder().id(jobId).status(String.valueOf(jobs.size())).param("").build();
            jobUtil.modifyJobLog(jobLogPojo);

            //分配好后，开始写节点信息。写入最小和最大
            jobsAllocateResult.forEach((ip,ids) -> {
                    int max = ids.stream().max(Integer::compareTo).orElse(0);
                    int min = ids.stream().min(Integer::compareTo).orElse(0);
                    String item = "["+min + "," + max +"]";
                    dispatchClient.setNodeData(ip,item) ;

                }
            );
        }
    }


}
