package com.inventrax.nilkamal_vna.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.nilkamal_vna.R;
import com.inventrax.nilkamal_vna.pojos.InventoryDTO;
import com.inventrax.nilkamal_vna.pojos.VlpdDto;

import java.util.List;

public class PalletAdapter extends RecyclerView.Adapter {

    private List<VlpdDto> palletList;

    Context context;

    public PalletAdapter(Context context, List<VlpdDto> list) {
        this.context = context;
        this.palletList = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtPalletNo;

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtPalletNo = (TextView) itemView.findViewById(R.id.txtPalletNo);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pallet_number, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {



        VlpdDto vlpdDto = (VlpdDto) palletList.get(position);

        // set the data in items
        ((MyViewHolder) holder).txtPalletNo.setText(vlpdDto.getPalletNo());

    }


    @Override
    public int getItemCount() {
        return palletList.size();
    }

}