package com.fzk.stress.entity;

import lombok.Data;

@Data
public class Vehicle {
    private String bindingDeviceImei;

    /**格式为年月日时分秒
     例如120110030405为18年1月16日3点4分5秒*/
    private String gpsTime;
    private String gpsValid;
    private String lngSign = "e";
    private String lng = "10637.60128";
    private String latSign = "n";
    private String lat = "2943.30582";
    private String speed = "0";
    private String direction;
    private String stars;
    /**ACC+ON+引擎+行驶*/
    private String onSeries = "2222";

    /**总门边+左前+右前+左后+右后+后箱+前盖*/
    private String doorStatus = "2222222";
    /**总门锁+左前+右前+左后+右后*/
    private String lockStatus = "11111";
    /**总门窗+左前+右前+左后+右后+天窗*/
    private String winStatus = "222222";
    /**总车灯+大灯+小灯*/
    private String lampStatus = "222";
    /**设备设防+告警*/
    private String securityStatus = "11";
    private String gear = "1";
    private String totalMileage;
    private String remainingMileage;
    private String voltage;
    /**电动车插入状态*/
    private String chargePlugStatus;
    private String chargeStatus;

    private String remainingPowerValid;
    private String remainingPower;

    private String obdSpeed = "0";
    private String angle;
    /**手刹*/
    private String brakeStatus = "1";



}
