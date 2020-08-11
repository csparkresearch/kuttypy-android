package com.example.kuttypy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import de.nitri.gauge.Gauge;

public class SensorPopup extends DialogFragment {

    private final int minVal,maxVal;
    private boolean isModal = false;
    int position;
    Gauge gauge;

    public static SensorPopup newInstance(int min, int max)
    {
        SensorPopup frag = new SensorPopup(min,max);
        frag.isModal = true; // WHEN FRAGMENT IS CALLED AS A DIALOG SET FLAG
        return frag;
    }

    SensorPopup(int min, int max)
    {
        minVal = min;
        maxVal = max;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(isModal) // AVOID REQUEST FEATURE CRASH
        {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        else
        {
            View view = inflater.inflate(R.layout.fragment_sensor_popup, container, false);

            setupUI(view);
            return view;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder alertDialogBuilder;
        alertDialogBuilder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_sensor_popup, null);
        alertDialogBuilder.setView(view);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
        dialog.setTitle("Sensor Data: " + (position+1));

        setupUI(view);
        return dialog;
    }

    private void setupUI(View view){
        gauge = (Gauge) view.findViewById(R.id.gaugePopup);
        gauge.setMaxValue(maxVal);
        gauge.setMinValue(minVal);
        gauge.setTotalNicks(120);
        gauge.setValuePerNick((maxVal-minVal)/100);
        gauge.setMajorNickInterval(10);
        gauge.setUpperTextSize(100);
        gauge.setLowerTextSize(48);

    }
    void setValue(float val, boolean smooth){
        try {
            if(smooth) gauge.moveToValue(val);
            else gauge.setValue(val);
        }catch(Exception e){
            Log.e("setValue error",e.getMessage());
            e.printStackTrace();
        }

    }
}