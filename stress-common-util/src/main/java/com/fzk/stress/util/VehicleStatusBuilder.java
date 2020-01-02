package com.fzk.stress.util;

import com.fzk.stress.entity.Vehicle;
import org.apache.commons.lang3.StringUtils;

public class VehicleStatusBuilder {
    public static String buildAllStatusMessage(Vehicle vehicle,int lastStatusIndex){
        StringBuilder sb = new StringBuilder();
        if (lastStatusIndex%5==0) {
            sb.append("(1*5d|7|331,");
        }else {
            sb.append("(1*5d|5|331,");
        }
        //GPS时间
        appendStatus(sb,vehicle.getGpsTime());
        //坐标有效标志
        appendStatus(sb,vehicle.getGpsValid());
        //经度标志
        appendStatus(sb,vehicle.getLngSign());
        //经度
        appendStatus(sb,vehicle.getLng());
        //纬度标志
        appendStatus(sb,vehicle.getLatSign());
        //纬度
        appendStatus(sb,vehicle.getLat());
        //速度
        appendStatus(sb,vehicle.getSpeed());
        //方向
        appendStatus(sb,vehicle.getDirection());
        //星数
        appendStatus(sb,vehicle.getStars());
        //ON系列
        appendStatus(sb,vehicle.getOnSeries());
        //门状态
        appendStatus(sb,vehicle.getDoorStatus());
        //锁状态
        appendStatus(sb,vehicle.getLockStatus());
        //窗状态
        appendStatus(sb,vehicle.getWinStatus());
        //灯状态
        appendStatus(sb,vehicle.getLampStatus());
        //安全状态
        appendStatus(sb,vehicle.getSecurityStatus());
        //档位
        appendStatus(sb,vehicle.getGear());
        //总里程
        appendStatus(sb,vehicle.getTotalMileage());
        //剩余里程
        appendStatus(sb,vehicle.getRemainingMileage());
        //电压
        appendStatus(sb,vehicle.getVoltage());
        //充电桩插入状态
        appendStatus(sb,vehicle.getChargePlugStatus());
        //充电状态
        appendStatus(sb,vehicle.getChargeStatus());
        //剩余电量有效标志
        appendStatus(sb,vehicle.getRemainingPowerValid());
        //剩余电量
        appendStatus(sb,vehicle.getRemainingPower());
        //OBD速度
        appendStatus(sb,vehicle.getObdSpeed());
        //卫星夹角
        appendStatus(sb,vehicle.getAngle());
        //手刹状态
        appendStatus(sb,vehicle.getBrakeStatus());
        sb.append("|)");
        return sb.toString();
    }

    private static void appendStatus(StringBuilder sb, String status){
        if(StringUtils.isNotBlank(status)){
            sb.append(status);
        }
        sb.append(",");
    }
}
