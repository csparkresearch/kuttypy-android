package com.cspark.kuttypy;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Constants {
    static final byte MPU6050_ADDRESS = 0x68;
    static final byte BMP280_ADDRESS = 118 ;
    static final byte TSL2561_ADDRESS = 0x39 ;
    static final byte ADC_ADDRESS = (byte) 128;// special. I2C is only upto 127

    static final Map<Byte, String> addressMap = new HashMap<Byte, String>() {{
        put(MPU6050_ADDRESS, "MPU6050");
        put(BMP280_ADDRESS, "BMP280");
        put(TSL2561_ADDRESS, "TSL2561");
    }};

    static final int[] colors = new int[]{
            Color.rgb(255, 0, 0),
            Color.rgb(  0, 255, 0),
            Color.rgb(0, 0, 255),
            Color.rgb(255, 255, 0),
            Color.rgb(0, 255, 255),
            Color.rgb(255, 0, 255),
            Color.rgb(255, 255, 255),
            Color.rgb(100, 255, 100),
            Color.rgb(100, 100, 255)
    };

}

interface sensorMethods{
    List getData();
}


class GenericSensor{
    public byte address = 0x68;
    private sensorMethods sens;
    GenericSensor(comlib communicationHandler, byte addr){
        address = addr;
        if(address == Constants.MPU6050_ADDRESS){ // assume MPU6050
            sens = new MPU6050(communicationHandler,Constants.MPU6050_ADDRESS);
        }else if(address == Constants.BMP280_ADDRESS){
            sens = new BMP280(communicationHandler,Constants.BMP280_ADDRESS);
        }else if(address == Constants.TSL2561_ADDRESS){
            sens = new TSL2561(communicationHandler,Constants.TSL2561_ADDRESS);
        }else if(address == Constants.ADC_ADDRESS){
            sens = new ADC(communicationHandler);
        }
    }

    List getData(){
        return sens.getData();
    }
}

class MPU6050 implements sensorMethods{
    private byte address;
    private comlib comms ;
    public String name = "MPU6050";

    MPU6050(comlib communicationHandler, byte addr){
            address = addr;
            comms = communicationHandler;
            comms.writeI2C(address, new byte[]{0x1B, 0x00}); //gyro range.
            comms.writeI2C(address, new byte[]{0x1C, 0x00}); // accel range.
            comms.writeI2C(address, new byte[]{0x6B, 0x00}); // Initialize. power on.
    }

    @Override
    public List getData(){
        List raw = comms.readI2C(address, (byte) (0x3B), 14);
        Integer[] arr = (Integer[])raw.toArray(new Integer[0]);
        if(raw.size() == 15 ){
            raw.clear();
            final int[] processed = {( arr[0] << 8) | arr[1],(arr[2] << 8) | arr[3],(arr[4] << 8) | arr[5],
                    (arr[8] << 8) | arr[9],(arr[10] << 8) | arr[11],
                    ( arr[12] << 8) | arr[13]
            };
            for (int i : processed) raw.add((float)(short)i);
        }else{
            raw.clear();
        }
    return raw;
    }

}


class TSL2561 implements sensorMethods{
    private byte address;
    private comlib comms ;
    public String name = "TSL2561 Luminosity";

    TSL2561(comlib communicationHandler, byte addr){
        address = addr;
        comms = communicationHandler;
        comms.writeI2C(address, new byte[]{(byte) 0x80, 0x03}); //power on.
        comms.writeI2C(address, new byte[]{((byte) 0x80 )| (0x01), 0x00}); // Timing|Gain.
    }

    @Override
    public List getData(){
        List raw = comms.readI2C(address, (byte) (0x80 | 0x20 | 0x0C),4);
        Integer[] arr = (Integer[])raw.toArray(new Integer[0]);
        if(raw.size() == 15 ){
            raw.clear();
            final int[] processed = {( arr[1] << 8) | arr[0],(arr[3] << 8) | arr[2]};
            for (int i : processed) raw.add((float)(short)i);
        }else{
            raw.clear();
        }
        return raw;
    }

}



class ADC implements sensorMethods{
    private comlib comms ;
    public String name = "10 bit ADC";

    ADC(comlib communicationHandler){
        comms = communicationHandler;
    }

    @Override
    public List getData(){
        List<Float> arr = new ArrayList<>(8);
        for(int i=0;i<8;i++){
            arr.add((float)comms.readADC(i));
        }

    return  arr;
    }

}




class BMP280 implements  sensorMethods{
    private byte address;
    public byte BMP280_REG_CONTROL = (byte) 0xF4;
    public byte BMP280_REG_RESULT = (byte) 0xF6;
    public byte BMP280_oversampling = 0;
    private float _BMP280_PRESSURE_MIN_HPA = 0;
    private float _BMP280_PRESSURE_MAX_HPA = 1600;
    public float _BMP280_sea_level_pressure = (float) 1013.25; //for calibration


    private comlib comms ;
    public String name = "BMP280";
    private double tfine = 0;
    private float[] tcal = {27504,26435,-1000}, pcal = {36477,-10645,3024,2855,140,-7,15500,-14600,6000};
    BMP280(comlib communicationHandler, byte addr){
        address = addr;
        comms = communicationHandler;
        comms.writeI2C(address, new byte[]{(byte) 0xF4, (byte) 0xFF}); // Initialize. power on.
        List raw = comms.readI2C(address, (byte) (0x88), 24); //calibration data
        Log.e("CALIBRATION", String.valueOf(raw.size()));
        Integer[] arr = (Integer[])raw.toArray(new Integer[0]);

    }

    private float calcTemperature(int adc_t) {
        double v1 = (adc_t / 16384.0 - tcal[0] / 1024.0) * tcal[1];
        double v2 = ((adc_t / 131072.0 - tcal[0] / 8192.0) * (adc_t / 131072.0 - tcal[0] / 8192.0)) * tcal[2];
        tfine = (v1 + v2);
        return (float) ((v1 + v2) / 5120.0);  //actual temperature.
    }
    private float calcPressure(int adc_p) {
        //calcTemperature(adc_t) //t_fine has been set now.
        double var1,var2,var3;
        var1 = tfine / 2.0 - 64000.0;
        var2 = var1 * var1 * pcal[5] / 32768.0;
        var2 = var2 + var1 * pcal[4] * 2.0;
        var2 = var2 / 4.0 + pcal[3] * 65536.0;
        var3 = pcal[2] * var1 * var1 / 524288.0;
        var1 = (var3 + pcal[1] * var1) / 524288.0;
        var1 = (1.0 + var1 / 32768.0) * pcal[0];
        if ( var1 == 0 )  return _BMP280_PRESSURE_MIN_HPA;
        double pressure = 1048576.0 - adc_p;
        pressure = ((pressure - var2 / 4096.0) * 6250.0) / var1;
        var1 = pcal[8] * pressure * pressure / 2147483648.0;
        var2 = pressure * pcal[7] / 32768.0;
        pressure = pressure + (var1 + var2 + pcal[6]) / 16.0;
        pressure /= 100;
        if (pressure<_BMP280_PRESSURE_MIN_HPA)
            return _BMP280_PRESSURE_MIN_HPA;
        if (pressure > _BMP280_PRESSURE_MAX_HPA)
            return _BMP280_PRESSURE_MAX_HPA;
        return (float) pressure;
    }

    @Override
    public List getData(){
        List raw = comms.readI2C(address, (byte) (0xF7), 6);
        Integer[] arr = (Integer[])raw.toArray(new Integer[0]);

        if(raw.size() == 7 ){
            raw.clear();
            final int[] processed = {(int) ((((arr[0] & 0xFF) * 65536.) + ((arr[1] & 0xFF) * 256.) + (arr[2] & 0xF0)) / 16.),
                    (int) ((((arr[3] & 0xFF) * 65536.) + ((arr[4] & 0xFF) * 256.) + (arr[5] & 0xF0)) / 16.)};
            //for (int i : processed) raw.add((float)(short)i);
            raw.add(calcTemperature(processed[1]));
            raw.add(calcPressure(processed[0]));
            raw.add((float)0.0); //altitde TODO
            Log.e("DATA", String.valueOf(raw));
        }else{
            raw.clear();
        }

        return raw;
    }

}








