package com.example.kuttypy;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;


public class spectrumData extends ViewModel {
    public Integer samples = 10;
    private final MutableLiveData<Integer> count = new MutableLiveData<Integer>(0);

    private final MutableLiveData<List<Integer>> states = new MutableLiveData<List<Integer>>();
    private final MutableLiveData<List<Integer>> reg = new MutableLiveData<List<Integer>>();
    private final MutableLiveData<List<Integer>> adc = new MutableLiveData<List<Integer>>();
    private final MutableLiveData<List<Integer>> adcSingle = new MutableLiveData<List<Integer>>();
    private final MutableLiveData<List<Float>> i2cActive = new MutableLiveData<List<Float>>();
    private final MutableLiveData<String> currentSensor = new MutableLiveData<String>();
    private final MutableLiveData<String> command = new MutableLiveData<String>();

    public void setCount(Integer cnt) {   count.postValue(cnt); }
    public LiveData<Integer> getCount() {
        return count;
    }

    public void setStates(List cnt) {   states.postValue(cnt); }
    public LiveData<List<Integer>> getStates() {
        return states;
    }

    public void setReg(List info) {   reg.postValue(info); }
    public LiveData<List<Integer>> getReg() {
        return reg;
    }


    public void setADC(List info) {   adc.postValue(info); }
    public LiveData<List<Integer>> getADC() {return adc; }

    public void setSingleADC(List info) {   adcSingle.postValue(info); }
    public LiveData<List<Integer>> getSingleADC() {return adcSingle; }

    public void setI2C(List info) {   i2cActive.postValue(info); }
    public LiveData<List<Float>> scanI2C() {return i2cActive; }


    public void setSensor(String info) {   currentSensor.postValue(info); }
    public LiveData<String> getSensor() {return currentSensor; }

    public void setCommand(String info) {   command.postValue(info); }
    public LiveData<String> getCommand() {return command; }


}