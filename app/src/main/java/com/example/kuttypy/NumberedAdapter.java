package com.example.kuttypy;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.nitri.gauge.Gauge;

public class NumberedAdapter extends RecyclerView.Adapter<NumberedAdapter.ViewHolder> {
    private final int[] minVal, maxVal;
    private List<String> labels;
    private int totalgauges;
    public Gauge[] myGauges;



    public NumberedAdapter(int count,int[] min, int[] max) {
        totalgauges = count;
        minVal = min;
        maxVal = max;
        myGauges = new Gauge[totalgauges];
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final int myPos = position+1;
        myGauges[position] = holder.gauge;
        holder.gauge.setMaxValue(maxVal[position]);
        holder.gauge.setMinValue(minVal[position]);
        holder.gauge.setTotalNicks(120);
        holder.gauge.setValuePerNick((maxVal[position]-minVal[position])/100);
        holder.gauge.setMajorNickInterval(10);
        holder.gauge.setUpperTextSize(100);
        holder.gauge.setLowerTextSize(48);

        //handling item click event
        holder.gauge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(holder.gauge.getContext(), ""+myPos, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return totalgauges;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public Gauge gauge;

        public ViewHolder(View itemView) {
            super(itemView);
            gauge = (Gauge) itemView.findViewById(R.id.gauge);
        }
    }
}