package com.cspark.kuttypy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;

import java.util.List;

public class SensorFragment extends Fragment {
    private spectrumData data;
    private LinearLayout sensorLayout;
    private Button scan;
    NumberedAdapter adapter;
    private CheckBox smooth;
    public String dev;


    public XYPlot plot;
    private Redrawer redrawer;
    private SensorPopup.DataSet[] myData =  new SensorPopup.DataSet[20];
    private int SIZE=500;
    private AdvancedLineAndPointRenderer rendererRef;
    private TextView title;


    public SensorFragment() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ConstraintLayout root = (ConstraintLayout)inflater.inflate(R.layout.fragment_sensor, container, false);

        smooth = (CheckBox) root.findViewById(R.id.smoothBox);
        title = (TextView) root.findViewById(R.id.sensorTitle);
        data = new ViewModelProvider(requireActivity()).get(spectrumData.class);

        if(getArguments() != null) {

            dev = SensorFragmentArgs.fromBundle(getArguments()).getDevice();
            if(dev.equals("MPU6050")){
                adapter = new NumberedAdapter(getActivity(),6, new int[]{-32767, -32767, -32767, -32767, -32767, -32767}, new int[]{32767, 32767, 32767, 32767, 32767, 32767});
            }else if(dev.equals("BMP280")){
                adapter = new NumberedAdapter(getActivity(),3, new int[]{0, 0, 0}, new int[]{100, 1600, 100});
            }else if(dev.equals("TSL2561")){
                adapter = new NumberedAdapter(getActivity(),2, new int[]{0, 0}, new int[]{40000,40000});
            }else if(dev.equals("ADCSENS")){
                adapter = new NumberedAdapter(getActivity(),8, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, new int[]{1023, 1023, 1023,1023, 1023, 1023,1023, 1023});
            }
            title.setText("Sensor : "+dev);
            data.setSensor(dev);
        }


        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);


        plot = (XYPlot) root.findViewById(R.id.plotAll);


        // add a new series' to the xyplot:
        SensorPopup.MyFadeFormatter[] formatter =new SensorPopup.MyFadeFormatter[adapter.totalgauges];
        for(int i=0;i<adapter.totalgauges;i++){
            myData[i] = new SensorPopup.DataSet(SIZE);
            formatter[i] = new SensorPopup.MyFadeFormatter(SIZE);
            formatter[i].getLinePaint().setColor(Constants.colors[i]);
            formatter[i].setLegendIconEnabled(false);
            plot.addSeries(myData[i], formatter[i]);


        }
        plot.setRangeBoundaries(adapter.minVal[0], adapter.maxVal[0], BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, SIZE, BoundaryMode.FIXED);

        // reduce the number of range labels
        plot.setLinesPerRangeLabel(3);

        // set a redraw rate in hz and start immediately:
        redrawer = new Redrawer(plot, 30, true);

        rendererRef = plot.getRenderer(AdvancedLineAndPointRenderer.class);


        return root;

    }



    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        data.getI2C().observe(getViewLifecycleOwner(), new Observer<List<Float>>() {
            @Override
            public void onChanged(@Nullable List l) {
                //scan.setText(String.valueOf(l));


                for(int i=0;i<l.size();i++) {
                    adapter.setValue((Float) l.get(i),i,smooth.isChecked());
                }

                if (adapter.popup.gauge != null){ //popup is open
                    if (adapter.popup.position <l.size()){

                        adapter.popup.setValue((Float) l.get(adapter.popup.position),smooth.isChecked());
                    }
                }
                if(!adapter.popup.isVisible()){ //update main graph
                    for(int i=0;i<l.size();i++) {
                        if(myData[i] != null)myData[i].addPoint((Float) l.get(i));
                    }
                    if(l.size()>0)rendererRef.setLatestIndex(myData[0].latestIndex);

                }


            }
        });



    }

    @Override
    public void onStop() {
        super.onStop();
        redrawer.finish();
    }


}
