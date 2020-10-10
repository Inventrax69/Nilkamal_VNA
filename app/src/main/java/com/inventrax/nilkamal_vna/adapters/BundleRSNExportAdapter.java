package com.inventrax.nilkamal_vna.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.nilkamal_vna.R;
import com.inventrax.nilkamal_vna.pojos.InboundDTO;

import java.util.List;

public class BundleRSNExportAdapter extends RecyclerView.Adapter{

    private List<InboundDTO> bundleExportList;

    Context context;
    public BundleRSNExportAdapter(Context context, List<InboundDTO> list) {
        this.context = context;
        this.bundleExportList = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtUniqueRSN;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtUniqueRSN = (TextView) itemView.findViewById(R.id.txtUniqueRSN);

        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bundle_rsn_export_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        InboundDTO inboundDTO = (InboundDTO) bundleExportList.get(position);

        // set the data in items
        if(inboundDTO.getUniqueRSN()!=null)
                ((MyViewHolder) holder).txtUniqueRSN.setText(inboundDTO.getUniqueRSN());


    }


    @Override
    public int getItemCount() {
        return bundleExportList.size();
    }
}
