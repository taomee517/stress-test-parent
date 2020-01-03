package com.fzk.vehicle.monitor.entity;

import com.fzk.stress.cache.RedisService;
import com.fzk.stress.cache.TopicCenter;
import com.fzk.stress.entity.Vehicle;
import com.fzk.stress.util.DateTimeUtil;
import com.fzk.stress.util.ThreadPoolUtil;
import com.fzk.stress.util.VehicleStatusBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.fzk.stress.cache.TopicCenter.CAR_STATUS_PREFIX;
import static com.fzk.stress.cache.TopicCenter.DELAY_MESSAGE_PREFIX;

@Data
@Slf4j
public class Monitor {
    private List<String> imeis;
    private static volatile Map<String,Integer> indexMap = new HashMap<>(16);
    private static ScheduledThreadPoolExecutor scheduledPool = ThreadPoolUtil.schedule;

    public Monitor(List<String> imeis) {
        this.imeis = imeis;
    }

    public void detectRealTimeStatus(){
        int size = imeis.size();
        for(int i=0;i<size;i++){
            String imei = imeis.get(i);
            Vehicle vehicle = new Vehicle();
            vehicle.setBindingDeviceImei(imei);
            //模拟一半的车辆处理非ON档
            //处理非ON的设备
            handleParkingVehicle(vehicle);
//            boolean flag = new Random().nextBoolean();
            boolean flag = true;
            log.info("ON分配，imei = {}, futureDrive = {}", imei, flag);
            if(flag){
                //未来某个时刻启动并行驶一段轨迹
//                int start = new Random().nextInt(20);
                int start = 1;
                int end = new Random().nextInt(60) + start;
                log.info("车辆{}分钟后，进入驾驶状态，imei = {},start = {},end = {}",start, imei, start, end);
                driveTheCarInFuture(vehicle,start,end);
            }
        }
    }

    private void handleParkingVehicle(Vehicle vehicle){
        String imei = vehicle.getBindingDeviceImei();
        scheduledPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                boolean onSignal = RedisService.getOnStatus(imei);
                //没有存入，就表示非ON状态
                if(Objects.isNull(onSignal)){
                    publishStatus(vehicle);
                }
            }
        },5, 150,TimeUnit.SECONDS);
    }

    private void driveTheCarInFuture(Vehicle vehicle,int start, int end){
        //车辆启动
        scheduledPool.schedule(new Runnable() {
            @Override
            public void run() {
                //车辆变为ON
                String imei = vehicle.getBindingDeviceImei();
                log.info("车辆启动，imei = {}",imei);
                RedisService.set(TopicCenter.buildOnKey(imei),true);
                vehicle.setOnSeries("1111");
                vehicle.setGear("4");
                publishStatus(vehicle);
                scheduledPool.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        String imei = vehicle.getBindingDeviceImei();
                        boolean onSignal = RedisService.getOnStatus(imei);
                        if(onSignal){
                            publishStatus(vehicle);
                        }
                    }
                },5, 5,TimeUnit.SECONDS);
            }
        },start, TimeUnit.MINUTES);
        //车辆熄火
        scheduledPool.schedule(new Runnable() {
            @Override
            public void run() {
                //车辆变为ON
                String imei = vehicle.getBindingDeviceImei();
                log.info("车辆熄火，imei = {}",imei);
                RedisService.delete(TopicCenter.buildOnKey(imei));
                vehicle.setOnSeries("2222");
                vehicle.setGear("1");
                publishStatus(vehicle);
            }
        },end, TimeUnit.MINUTES);
    }

    public void publishStatus(Vehicle vehicle){
        String hexTime = DateTimeUtil.timeToHexString(DateTimeUtil.getDatetime());
        vehicle.setGpsTime(hexTime);
        String imei = vehicle.getBindingDeviceImei();
        Integer srcLastStatusIndex = indexMap.get(imei);
        int lastStatusIndex = 0;
        if(Objects.nonNull(srcLastStatusIndex) && srcLastStatusIndex!=9999){
            lastStatusIndex = srcLastStatusIndex;
        }
        int currentStatusIndex = lastStatusIndex + 1;
        indexMap.put(imei, currentStatusIndex);
        String statusMsg = VehicleStatusBuilder.buildAllStatusMessage(vehicle,lastStatusIndex);
        String topic = StringUtils.join(CAR_STATUS_PREFIX,imei);
        log.info("发布状态消息，imei = {},statusMsg = {}", imei, statusMsg);
        if (RedisService.publish(topic,statusMsg) == 0) {
            String key = StringUtils.join(DELAY_MESSAGE_PREFIX,imei);
            RedisService.lpush(key,statusMsg);
        }
    }
}
