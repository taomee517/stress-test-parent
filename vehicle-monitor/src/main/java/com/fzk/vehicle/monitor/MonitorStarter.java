package com.fzk.vehicle.monitor;

import com.fzk.stress.cache.RedisService;
import com.fzk.stress.util.FileInfoCheckUtil;
import com.fzk.vehicle.monitor.entity.Monitor;

import java.util.List;

public class MonitorStarter {
    public static void main(String[] args) throws Exception {
        RedisService.clearAllOnStatus();
        List<String> imeis = FileInfoCheckUtil.getColumnData();
        Monitor monitor = new Monitor(imeis);
        monitor.detectRealTimeStatus();
    }
}
