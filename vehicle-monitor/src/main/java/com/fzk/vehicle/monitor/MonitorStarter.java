package com.fzk.vehicle.monitor;

import com.fzk.stress.cache.RedisService;
import com.fzk.stress.util.FileInfoCheckUtil;
import com.fzk.vehicle.monitor.entity.Monitor;

import java.util.Arrays;
import java.util.List;

public class MonitorStarter {
    public static void main(String[] args) throws Exception {
        RedisService.clearAllOnStatus();
        List<String> imeis = FileInfoCheckUtil.getColumnData();

        /**单一设备验证用*/
//        List<String> imeis = Arrays.asList("865886034429940");

        Monitor monitor = new Monitor(imeis);
        monitor.detectRealTimeStatus();
    }
}
