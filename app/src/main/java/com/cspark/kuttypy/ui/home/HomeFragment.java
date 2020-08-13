package com.cspark.kuttypy.ui.home;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cspark.kuttypy.R;
import com.cspark.kuttypy.spectrumData;

import java.util.List;

public class HomeFragment extends Fragment {

    private SeekBar[] PA = new SeekBar[8];
    private TextView[] PNames = new TextView[8];
    private View customA;
    private spectrumData data;
    private int i;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Toast.makeText(requireActivity(), "Launched ADC Fragment", Toast.LENGTH_SHORT).show();

        ConstraintLayout root = (ConstraintLayout)inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayout layoutA = (LinearLayout) root.findViewById(R.id.adcLayout);

        for (i = 0; i < 8; i++) {
            customA = inflater.inflate(R.layout.adc, null);
            TextView sw = (TextView) customA.findViewById(R.id.pinName);
            sw.setText("PA" + i);
            final SeekBar bs = (SeekBar) customA.findViewById(R.id.pinValue);
            bs.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });

            PA[i] = bs;
            PNames[i] = sw;
            final int curpos = i;
            layoutA.addView(customA);
        }


        return root;
    }


    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        data = new ViewModelProvider(requireActivity()).get(spectrumData.class);
        data.setSensor("ADC");


        data.getADC().observe(getViewLifecycleOwner(), new Observer<List<Integer>>() {
            @Override
            public void onChanged(@Nullable List l) {
                for(int i=0;i<8;i++){
                    int s = (int) l.get(i);
                    PA[i].setProgress(s);
                }
            }
        });

        data.getSingleADC().observe(getViewLifecycleOwner(), new Observer<List<Integer>>() {
            @Override
            public void onChanged(@Nullable List l) {
                    PA[(int)l.get(0)].setProgress((int)l.get(1));
                    PNames[(int)l.get(0)].setText("PA"+(int)l.get(0)+":"+(int)l.get(1));
            }
        });



    }


    }
