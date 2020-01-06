package com.fzk.vehicle.monitor.task;

import com.fzk.stress.cache.RedisService;
import com.fzk.stress.entity.Vehicle;
import com.fzk.stress.util.HashedWheelTimerUtil;
import com.fzk.vehicle.monitor.util.StatusPublishUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.fzk.stress.constants.Configuration.NONE_ON_REPORT_INTERVAL;

public class ParkingCarStatusPublishTask implements TimerTask {
    private Vehicle vehicle;
    private static HashedWheelTimer timer = HashedWheelTimerUtil.instance().getTimer();

    public ParkingCarStatusPublishTask(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        String imei = vehicle.getBindingDeviceImei();
        boolean onSignal = RedisService.getOnStatus(imei);
        //非ON状态
        if(!onSignal){
            StatusPublishUtil.publishStatus(vehicle);
            timer.newTimeout(this, NONE_ON_REPORT_INTERVAL, TimeUnit.SECONDS);
        }
    }
}
