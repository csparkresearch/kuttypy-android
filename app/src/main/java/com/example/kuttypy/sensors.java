package com.example.kuttypy;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class MPU6050{
    public byte address = 0x68;
    private comlib comms ;
    public String name = "MPU6050";

    public MPU6050(comlib communicationHandler){
            comms = communicationHandler;
            comms.writeI2C(address, new byte[]{0x1B, 0x00}); //gyro range.
            comms.writeI2C(address, new byte[]{0x1C, 0x00}); // accel range.
            comms.writeI2C(address, new byte[]{0x6B, 0x00}); // Initialize. power on.
    }

    public List getData(){
        List raw = comms.readI2C(address, (byte) (0x3B), 14);
        Integer[] arr = (Integer[])raw.toArray(new Integer[raw.size()]);
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



class BMP280{
    public byte address = 118;
    public byte BMP280_REG_CONTROL = (byte) 0xF4;
    public byte BMP280_REG_RESULT = (byte) 0xF6;
    public byte BMP280_oversampling = 0;
    public float _BMP280_PRESSURE_MIN_HPA = 0;
    public float _BMP280_PRESSURE_MAX_HPA = 1600;
    public float _BMP280_sea_level_pressure = (float) 1013.25; //for calibration.. from circuitpython library


    private comlib comms ;
    public String name = "BMP280";
    double tfine = 0;
    private float[] tcal = {27504,26435,-1000}, pcal = {36477,-10645,3024,2855,140,-7,15500,-14600,6000};
    public BMP280(comlib communicationHandler){
        comms = communicationHandler;
        comms.writeI2C(address, new byte[]{(byte) 0xF4, (byte) 0xFF}); // Initialize. power on.
        List raw = comms.readI2C(address, (byte) (0x88), 24); //calibration data
        Log.e("CALIBRATION", String.valueOf(raw.size()));
        Integer[] arr = (Integer[])raw.toArray(new Integer[raw.size()]);

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

    public List getData(){
        List raw = comms.readI2C(address, (byte) (0xF7), 6);
        Integer[] arr = (Integer[])raw.toArray(new Integer[raw.size()]);

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












public class sensors {
    public List<Object> availableSensors = new ArrayList<Object>();
    public sensors(comlib comms){
        availableSensors.add( new MPU6050(comms) );
    }

}
