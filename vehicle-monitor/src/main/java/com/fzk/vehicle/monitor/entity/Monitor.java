package com.fzk.vehicle.monitor.entity;

import com.alibaba.fastjson.JSON;
import com.fzk.stress.entity.Vehicle;
import com.fzk.stress.util.DateTimeUtil;
import com.fzk.stress.util.JedisBuilder;
import com.fzk.stress.util.ThreadPoolUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class Monitor {
    private List<String> imeis;
    private static volatile Map<String,Boolean> onMap = new HashMap<>(16);
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
            boolean flag = new Random().nextBoolean();
            log.info("ON分配，imei = {}, futureDrive = {}", imei, flag);
            if(flag){
                //未来某个时刻启动并行驶一段轨迹
                int start = new Random().nextInt(20);
                int end = new Random().nextInt(60) + start;
                log.info("车辆即将进入驾驶状态，imei = {},start = {},end = {}",imei,start,end);
                driveTheCarInFuture(vehicle,start,end);
            }
        }
    }

    private void handleParkingVehicle(Vehicle vehicle){
        String imei = vehicle.getBindingDeviceImei();
        Boolean onSignal = onMap.get(imei);
        //没有存入，就表示非ON状态
        if(Objects.isNull(onSignal)){
            scheduledPool.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    publishStatus(vehicle);
                }
            },5, 150,TimeUnit.SECONDS);
        }
    }

    private void driveTheCarInFuture(Vehicle vehicle,int start, int end){
        //车辆启动
        scheduledPool.schedule(new Runnable() {
            @Override
            public void run() {
                //车辆变为ON
                String imei = vehicle.getBindingDeviceImei();
                log.info("车辆启动，imei = {}, hexTime = {}",imei);
                onMap.put(imei,Boolean.TRUE);
                vehicle.setOnSeries("1111");
                vehicle.setGear("4");
                publishStatus(vehicle);
                scheduledPool.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        String imei = vehicle.getBindingDeviceImei();
                        Boolean onSignal = onMap.get(imei);
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
                onMap.remove(imei);
                vehicle.setOnSeries("2222");
                vehicle.setGear("1");
                publishStatus(vehicle);
            }
        },end, TimeUnit.MINUTES);
    }

    public void publishStatus(Vehicle vehicle){
        Jedis jedis = JedisBuilder.instance().getJedis();
        try {
            String hexTime = DateTimeUtil.TimeToHexString(DateTimeUtil.getDatetime());
            vehicle.setGpsTime(hexTime);
            String imei = vehicle.getBindingDeviceImei();
            String topic = "CarStatusQueue:" + imei;
            String vehicleJson = JSON.toJSONString(vehicle);
            log.info("抓取车辆状态，imei = {},vehicle = {}",imei,vehicleJson);
            jedis.lpush(topic,vehicleJson);
        } finally {
            jedis.close();
        }
    }
}
