package com.inventrax.nilkamal_vna.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class VNALoadingDTO {


    @SerializedName("Result")
    private String Result;
    @SerializedName("Mcode")
    private String Mcode;
    @SerializedName("qty")
    private String qty;
    @SerializedName("BatchNo")
    private String BatchNo;
    @SerializedName("PickRSNCount")
    private String PickRSNCount;
    @SerializedName("MDescreiption")
    private String MDescreiption;

    public VNALoadingDTO(){

    }


    public VNALoadingDTO(Set<? extends Map.Entry<?, ?>> entries)
    {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {


                case "Result":
                    if(entry.getValue()!=null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;
                case "Mcode":
                    if(entry.getValue()!=null) {
                        this.setMcode(entry.getValue().toString());
                    }
                    break;
                case "qty":
                    if(entry.getValue()!=null) {
                        this.setQty(entry.getValue().toString());
                    }
                    break;
                case "BatchNo":
                    if(entry.getValue()!=null) {
                        this.setBatchNo(entry.getValue().toString());
                    }
                    break;
                case "MDescreiption":
                    if(entry.getValue()!=null) {
                        this.setMDescreiption(entry.getValue().toString());
                    }
                    break;
                case "PickRSNCount":
                    if(entry.getValue()!=null) {
                        this.setPickRSNCount(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    public String getMcode() {
        return Mcode;
    }

    public void setMcode(String mcode) {
        Mcode = mcode;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getBatchNo() {
        return BatchNo;
    }

    public void setBatchNo(String batchNo) {
        BatchNo = batchNo;
    }

    public String getMDescreiption() {
        return MDescreiption;
    }

    public void setMDescreiption(String MDescreiption) {
        this.MDescreiption = MDescreiption;
    }

    public String getPickRSNCount() {
        return PickRSNCount;
    }

    public void setPickRSNCount(String pickRSNCount) {
        PickRSNCount = pickRSNCount;
    }

}
