package com.brave.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobProperty {
    public String job;
    public String lock;
    public String root;
    public String subNode;
    public String executor;
    public String log;
}
