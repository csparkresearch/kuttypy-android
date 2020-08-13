package com.example.kuttypy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import de.nitri.gauge.Gauge;

public class SensorPopup extends DialogFragment {

    private final int minVal,maxVal;
    private boolean isModal = false;
    int position=0;
    Gauge gauge;

    public XYPlot plot;
    private Redrawer redrawer;
    private DataSet myData;
    private int SIZE=2000;
    private AdvancedLineAndPointRenderer rendererRef;


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
    public void setPosition(int pos){
        position = pos;
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
        gauge.setColors(Constants.colors[position]);

        plot = (XYPlot) view.findViewById(R.id.plot);
        SIZE = 500;
        myData = new DataSet(SIZE);

        // add a new series' to the xyplot:
        MyFadeFormatter formatter =new MyFadeFormatter((int) (SIZE));
        formatter.getLinePaint().setColor(Constants.colors[position]);
        formatter.setLegendIconEnabled(false);
        plot.addSeries(myData, formatter);
        plot.setRangeBoundaries(minVal, maxVal, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, SIZE, BoundaryMode.FIXED);

        // reduce the number of range labels
        plot.setLinesPerRangeLabel(3);

        // set a redraw rate of 30hz and start immediately:
        redrawer = new Redrawer(plot, 30, true);

        rendererRef = plot.getRenderer(AdvancedLineAndPointRenderer.class);


    }

    void setValue(float val, boolean smooth){
        try {
            if(smooth) gauge.moveToValue(val);
            else gauge.setValue(val);
            myData.addPoint(val);
            rendererRef.setLatestIndex(myData.latestIndex);
        }catch(Exception e){
            Log.e("setValue error",e.getMessage());
            e.printStackTrace();
        }

    }



    /**
     * Special {@link AdvancedLineAndPointRenderer.Formatter} that draws a line
     * that fades over time.  Designed to be used in conjunction with a circular buffer model.
     */
    public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

        private int trailSize;

        MyFadeFormatter(int trailSize) {
            this.trailSize = trailSize;
        }

        @Override
        public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
            // offset from the latest index:
            int offset;
            if(thisIndex > latestIndex) {
                offset = latestIndex + (seriesSize - thisIndex);
            } else {
                offset =  latestIndex - thisIndex;
            }

            float scale = 255f / trailSize;
            int alpha = (int) (255 - (offset * scale));
            getLinePaint().setAlpha(alpha > 0 ? alpha : 0);
            return getLinePaint();
        }
    }



    /**
     * Primitive simulation of some kind of signal.  For this example,
     * we'll pretend its an ecg.  This class represents the data as a circular buffer;
     * data is added sequentially from left to right.  When the end of the buffer is reached,
     * i is reset back to 0 and simulated sampling continues.
     */
    public static class DataSet implements XYSeries {

        private final Number[] data;
        public int latestIndex;
        /**
         *
         * @param size Sample size contained within this model
         */
        DataSet(int size) {
            data = new Number[size];
            for(int i = 0; i < data.length; i++) {
                data[i] = 0;
            }


        }

        public void addPoint(float p){
            if (latestIndex >= data.length) {
                latestIndex = 0;
            }
            data[latestIndex] = p;

            if(latestIndex < data.length - 1) {
                // null out the point immediately following i, to disable
                // connecting i and i+1 with a line:
                data[latestIndex +1] = null;
            }

            latestIndex++;
        }


        @Override
        public int size() {
            return data.length;
        }

        @Override
        public Number getX(int index) {
            return index;
        }

        @Override
        public Number getY(int index) {
            return data[index];
        }

        @Override
        public String getTitle() {
            return "Signal";
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        redrawer.finish();
    }

}