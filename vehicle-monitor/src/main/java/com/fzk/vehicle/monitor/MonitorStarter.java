package com.fzk.vehicle.monitor;

import com.fzk.stress.util.FileInfoCheckUtil;
import com.fzk.vehicle.monitor.entity.Monitor;

import java.util.Arrays;
import java.util.List;

public class MonitorStarter {
    public static void main(String[] args) throws Exception {
//        String filePath = "E:\\private\\test\\pressure test\\压测设备.xlsx";
//        List<String> imeis = FileInfoCheckUtil.getColumnData(filePath);

        //4.111
//        List<String> imeis = Arrays.asList("863613035276886","865258037560719","863613035280920");
        List<String> imeis = Arrays.asList("156888888888822");
        Monitor monitor = new Monitor(imeis);
        monitor.detectRealTimeStatus();
    }
}
