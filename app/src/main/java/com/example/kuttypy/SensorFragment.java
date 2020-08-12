package com.example.kuttypy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SensorFragment extends Fragment {
    private spectrumData data;
    private LinearLayout sensorLayout;
    private Button scan;
    NumberedAdapter adapter;
    private CheckBox smooth;
    public String dev;

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

        scan = (Button) root.findViewById(R.id.scanButton);
        smooth = (CheckBox) root.findViewById(R.id.smoothBox);
        data = new ViewModelProvider(requireActivity()).get(spectrumData.class);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.setCommand("scan");
            }
        });


        if(getArguments() != null) {

            dev = SensorFragmentArgs.fromBundle(getArguments()).getDevice();
            Toast.makeText(requireActivity(),"Device found: "+dev,Toast.LENGTH_SHORT).show();
            if(dev.equals("MPU6050")){
                adapter = new NumberedAdapter(getActivity(),6, new int[]{-32767, -32767, -32767, -32767, -32767, -32767}, new int[]{32767, 32767, 32767, 32767, 32767, 32767});
            }else if(dev.equals("BMP280")){
                adapter = new NumberedAdapter(getActivity(),3, new int[]{0, 0, 0}, new int[]{100, 1600, 100});
            }else if(dev.equals("ADCSENS")){
                adapter = new NumberedAdapter(getActivity(),8, new int[]{0, 0, 0, 0, 0, 0, 0, 0}, new int[]{1023, 1023, 1023,1023, 1023, 1023,1023, 1023});
            }
            data.setSensor(dev);
        }


        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

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

                if (adapter.popup.gauge != null){
                    if (adapter.popup.position <l.size()){

                        adapter.popup.setValue((Float) l.get(adapter.popup.position),smooth.isChecked());
                    }
                }


            }
        });



    }




}
