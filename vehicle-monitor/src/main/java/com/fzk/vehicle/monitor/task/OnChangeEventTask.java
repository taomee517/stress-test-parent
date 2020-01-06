package com.fzk.vehicle.monitor.task;

import com.fzk.stress.cache.RedisService;
import com.fzk.stress.cache.TopicCenter;
import com.fzk.stress.entity.Vehicle;
import com.fzk.vehicle.monitor.util.StatusPublishUtil;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OnChangeEventTask implements TimerTask {
    private Vehicle vehicle;
    private boolean onSignal;

    public OnChangeEventTask(Vehicle vehicle, boolean onSignal) {
        this.vehicle = vehicle;
        this.onSignal = onSignal;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        String imei = vehicle.getBindingDeviceImei();
        if (onSignal) {
            log.info("车辆启动，imei = {}",imei);
            RedisService.set(TopicCenter.buildOnKey(imei),true);
            vehicle.setOnSeries("1111");
            vehicle.setGear("4");
            StatusPublishUtil.handleDrivingCarStatus(vehicle);
        }else {
            log.info("车辆熄火，imei = {}",imei);
            RedisService.delete(TopicCenter.buildOnKey(imei));
            vehicle.setOnSeries("2222");
            vehicle.setGear("1");
            StatusPublishUtil.handleParkingCarStatus(vehicle);
        }
        StatusPublishUtil.publishStatus(vehicle);
    }
}
