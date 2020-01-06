package com.fzk.vehicle.monitor.task;

import com.fzk.stress.cache.RedisService;
import com.fzk.stress.entity.Vehicle;
import com.fzk.stress.util.HashedWheelTimerUtil;
import com.fzk.vehicle.monitor.util.StatusPublishUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

import static com.fzk.stress.constants.Configuration.ON_REPORT_INTERVAL;

public class DrivingCarStatusPublishTask implements TimerTask {
    private Vehicle vehicle;
    private static HashedWheelTimer timer = HashedWheelTimerUtil.instance().getTimer();

    public DrivingCarStatusPublishTask(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        String imei = vehicle.getBindingDeviceImei();
        boolean onSignal = RedisService.getOnStatus(imei);
        if(onSignal){
            StatusPublishUtil.publishStatus(vehicle);
        }
        timer.newTimeout(this, ON_REPORT_INTERVAL, TimeUnit.SECONDS);
    }
}
