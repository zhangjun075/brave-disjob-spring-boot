package com.brave.util;


import java.util.ArrayList;
import java.util.List;

import com.brave.vo.JobProperty;
import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("brave")
public class JobProperties {
    public List<JobProperty> jobs = new ArrayList<>();
}
