package com.cspark.kuttypy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.cspark.kuttypy.Constants.addressMap;

public class I2CFragment extends Fragment {
    private spectrumData data;
    private LinearLayout sensorLayout;
    private Button scan;
    LinearLayout layoutA;

    public I2CFragment() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ConstraintLayout root = (ConstraintLayout)inflater.inflate(R.layout.fragment_i2c, container, false);

        Iterator sensors = addressMap.entrySet().iterator();
        while (sensors.hasNext()) {
            Map.Entry sensor = (Map.Entry)sensors.next();
            Fragment fragment = getFragmentManager().findFragmentByTag((String) sensor.getValue());
            if (fragment != null)
                getFragmentManager().beginTransaction().remove(fragment).commit();
        }

        layoutA = (LinearLayout) root.findViewById(R.id.detectedLayout);

        scan = (Button) root.findViewById(R.id.searchButton);
        data = new ViewModelProvider(requireActivity()).get(spectrumData.class);


        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data.setCommand("scan");
            }
        });

        return root;

    }



    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        data.getI2Cscan().observe(getViewLifecycleOwner(), new Observer<List<Integer>>() {
            @Override
            public void onChanged(@Nullable List l) {
                //scan.setText(String.valueOf(l));
                layoutA.removeAllViews();

                for(int i=0;i<l.size();i++) {
                    final byte addr = (byte)((int) l.get(i) &0xFF);
                    Button btn1 = new Button(requireContext());
                    btn1.setText(l.get(i).toString()+" : "+ addressMap.get(addr));
                    layoutA.addView(btn1);
                    btn1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Bundle bundle = new Bundle();
                            String devString = addressMap.get(addr);
                            if(devString != null) {
                                if(getFragmentManager().findFragmentByTag(devString) == null) {
                                    Toast.makeText(requireActivity(), "Launching" + devString, Toast.LENGTH_SHORT).show();
                                    bundle.putString("device", addressMap.get(addr));
                                    SensorFragment fragInfo = new SensorFragment();
                                    fragInfo.setArguments(bundle);

                                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                    transaction.add(R.id.sensLayout, fragInfo,devString);
                                    //transaction.addToBackStack(devString);  // if written, this transaction will be added to backstack
                                    transaction.commit();
                                }else{
                                    Toast.makeText(requireActivity(), "Already active:" + devString, Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(requireActivity(),"Sensor address not implemented: "+addr,Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }

            }
        });


    }

    @Override
    public void onStop() {
        super.onStop();
    }


}
