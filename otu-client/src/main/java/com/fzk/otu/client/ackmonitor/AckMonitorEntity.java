package com.fzk.otu.client.ackmonitor;

import lombok.Data;


@Data
public class AckMonitorEntity {
    private String imei;
    private String traceId;
    private IAckSuccessCallback ackCallback;
    private long sendTime;
    private int timeout;
    private String ackSign;
}
