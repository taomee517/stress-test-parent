package com.fzk.otu.client.device;

import lombok.Data;

@Data
public class Ex223240Device extends Extend240Device {
    /**工作模式 1-正常 2-展车 3-深度休眠*/
    private String tag223Info = "2";
}
