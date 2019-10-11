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
	//switcher 主要是为了来控制job的执行
	public String switcher;
	public String switchPath;
	public String switchSubPath;
}
