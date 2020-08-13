package com.cspark.kuttypy.ui.gallery;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.cspark.kuttypy.MainActivity;
import com.cspark.kuttypy.R;
import com.cspark.kuttypy.spectrumData;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.rgb;

public class GalleryFragment extends Fragment{

    private GalleryViewModel galleryViewModel;
    private int i;
    private TextView PINB,PINA,PINC,PIND;
    private int PDO=0,PDA=0,PDB=0,PDC=0,DDA=0,DDB=0,DDC=0,DDD=0;
    private View customA,customB,customC,customD;
    private CheckBox[] PA = new CheckBox[8],PB = new CheckBox[8],PC = new CheckBox[8],PD = new CheckBox[8];
    private CheckBox b;
    private spectrumData data;
    FrameLayout controls;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ConstraintLayout root = (ConstraintLayout)inflater.inflate(R.layout.fragment_gallery, container, false);

        LinearLayout layoutA = (LinearLayout) root.findViewById(R.id.layoutA);
        LinearLayout layoutB = (LinearLayout) root.findViewById(R.id.layoutB);
        LinearLayout layoutC = (LinearLayout) root.findViewById(R.id.layoutC);
        LinearLayout layoutD = (LinearLayout) root.findViewById(R.id.layoutD);

        for (i = 7; i >=0; i--) {
            customA = inflater.inflate(R.layout.pin, null);
            Switch sw = (Switch) customA.findViewById(R.id.pinName);
            sw.setText("PA" + i);
            final CheckBox bs = (CheckBox) customA.findViewById(R.id.pinState);
            PA[i] = bs;

            final int curpos = i;
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    bs.setEnabled(isChecked);
                    if(isChecked)DDA = DDA | (1 << curpos);
                    else DDA = DDA & ~(1 << curpos);
                    List<Integer> arr = new ArrayList<Integer>(2);
                    arr.add(0x3A); //DDRA
                    arr.add(DDA); //value
                    data.setReg(arr);
                }
            });
            bs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)PDA = PDA | (1 << curpos);
                    else PDA = PDA & ~(1 << curpos);

                    List<Integer> arr = new ArrayList<Integer>(2);
                    arr.add(0x3B); //PORTA
                    arr.add(PDA); //value
                    data.setReg(arr);
                }

            });


            layoutA.addView(customA);
        }

        for (i = 7; i >=0; i--) {
            customB = inflater.inflate(R.layout.pin, null);
            Switch sw = (Switch) customB.findViewById(R.id.pinName);
            sw.setText("PB" + i);

            final CheckBox bs = (CheckBox) customB.findViewById(R.id.pinState);
            PB[i] = bs;

            final int curpos = i;
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    bs.setEnabled(isChecked);
                    if(isChecked)DDB = DDB | (1 << curpos);
                    else DDB = DDB & ~(1 << curpos);
                    List<Integer> arr = new ArrayList<Integer>(2);
                    arr.add(0x37); //DDRB
                    arr.add(DDB); //value
                    data.setReg(arr);
                }
            });

            bs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)PDB = PDB | (1 << curpos);
                    else PDB = PDB & ~(1 << curpos);

                    List<Integer> arr = new ArrayList<Integer>(2);
                    arr.add(0x38); //PORTB
                    arr.add(PDB); //value
                    data.setReg(arr);
                }

            });

            layoutB.addView(customB);
        }

        for (i = 0; i < 8; i++) {
            customC = inflater.inflate(R.layout.pin, null);
            Switch sw = (Switch) customC.findViewById(R.id.pinName);
            sw.setText("PC" + i);

            final CheckBox bs = (CheckBox) customC.findViewById(R.id.pinState);
            PC[i] = bs;


            final int curpos = i;
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    bs.setEnabled(isChecked);
                    if(isChecked)DDC = DDC | (1 << curpos);
                    else DDC = DDC & ~(1 << curpos);
                    List<Integer> arr = new ArrayList<Integer>(2);
                    arr.add(0x34); //DDRC
                    arr.add(DDC); //value
                    data.setReg(arr);
                }
            });
            bs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)PDC = PDC | (1 << curpos);
                    else PDC = PDC & ~(1 << curpos);

                    List<Integer> arr = new ArrayList<Integer>(2);
                    arr.add(0x35); //PORTD
                    arr.add(PDC); //value
                    data.setReg(arr);
                }

            });

            layoutC.addView(customC);
        }

        for (i = 7; i >=0; i--) {
            customD = inflater.inflate(R.layout.pin, null);
            Switch sw = (Switch) customD.findViewById(R.id.pinName);
            sw.setText("PD" + i);
            final CheckBox bs = (CheckBox) customD.findViewById(R.id.pinState);
            PD[i] = bs;

            final int curpos = i;
            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    bs.setEnabled(isChecked);
                    if(isChecked)DDD = DDD | (1 << curpos);
                    else DDD = DDD & ~(1 << curpos);
                    List<Integer> arr = new ArrayList<Integer>(2);
                    arr.add(0x31); //DDRD
                    arr.add(DDD); //value
                    data.setReg(arr);
                }
            });
            bs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)PDO = PDO | (1 << curpos);
                    else PDO = PDO & ~(1 << curpos);

                    List<Integer> arr = new ArrayList<Integer>(2);
                    arr.add(0x32); //PORTD
                    arr.add(PDO); //value
                    data.setReg(arr);
                }

            });


            layoutD.addView(customD);
        }

        PINA = (TextView) root.findViewById(R.id.PINA);
        PINB = (TextView) root.findViewById(R.id.PINB);
        PINC = (TextView) root.findViewById(R.id.PINC);
        PIND = (TextView) root.findViewById(R.id.PIND);
        // set a fixed origin and a "by-value" step mode so that grid lines will
        // move dynamically with the data when the users pans or zooms:
        ((MainActivity)getActivity()).dev = "IO";


        return (View) root;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            controls.setVisibility(View.INVISIBLE);
        }else{
            controls.setVisibility(View.VISIBLE);
        }
    }

    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        data = new ViewModelProvider(requireActivity()).get(spectrumData.class);

        data.getStates().observe(getViewLifecycleOwner(), new Observer<List<Integer>>() {
            @Override
            public void onChanged(@Nullable List l) {
                int s = (int) l.get(0);
                PINA.setText("PINA "+String.valueOf(s));
                for(int i=0;i<8;i++){
                    if(((s>>i) & 0x01) == 1) PA[i].setBackgroundColor(rgb(0, 255, 0));
                    else PA[i].setBackgroundColor(rgb(255, 0, 0));
                }

                s = (int) l.get(1);
                PINB.setText("PINB "+String.valueOf(s));
                for(int i=0;i<8;i++){
                    if(((s>>i) & 0x01) == 1) PB[i].setBackgroundColor(rgb(0, 255, 0));
                    else PB[i].setBackgroundColor(rgb(255, 0, 0));
                }

                s = (int) l.get(2);
                PINC.setText("PINC "+String.valueOf(s));
                for(int i=0;i<8;i++){
                    if(((s>>i) & 0x01) == 1) PC[i].setBackgroundColor(rgb(0, 255, 0));
                    else PC[i].setBackgroundColor(rgb(255, 0, 0));
                }

                s = (int) l.get(3);
                PIND.setText("PIND "+String.valueOf(s));
                for(int i=0;i<8;i++){
                    if(((s>>i) & 0x01) == 1) PD[i].setBackgroundColor(rgb(0, 255, 0));
                    else PD[i].setBackgroundColor(rgb(255, 0, 0));
                }


            }
        });



    }




}
