package com.brave.util;

import java.util.concurrent.ConcurrentHashMap;

import com.brave.vo.JobProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeMeta {
    public static ConcurrentHashMap<String, JobProperty> JOB_NODE_MAP = new ConcurrentHashMap<>();
}
