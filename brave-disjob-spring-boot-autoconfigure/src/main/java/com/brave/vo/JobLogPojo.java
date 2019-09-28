package com.brave.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author junzhang
 */
@Data
@Builder
public class JobLogPojo{

    private int id;
    private String jobName;
    private String exeTime;
    private String executor;
    private String param;
    private String status;
    private String jobType;

}
