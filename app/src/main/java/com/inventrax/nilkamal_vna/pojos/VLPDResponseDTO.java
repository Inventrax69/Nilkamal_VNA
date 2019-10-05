package com.inventrax.nilkamal_vna.pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VLPDResponseDTO {

    @SerializedName("SuggestedItem")
    private List<ItemInfoDTO> suggestedItem ;
    /*  @SerializedName("PreviousPickedItemResponce")
      private List<ExecutionResponseDTO> previousPickedItemResponce ;*/
    @SerializedName("PreviousPickedItemResponce")
    private List<ExecutionResponseDTO> previousPickedItemResponce ;
    @SerializedName("IsNotSuggested")
    private Boolean isSuggested;
    @SerializedName("VlpdID")
    private String VlpdID ;

    @SerializedName("ID")
    private String ID;

    @SerializedName("VLPDNumber")
    private String VLPDNumber ;

    @SerializedName("Result")
    private String Result ;

    @SerializedName("PickRSNCount")
    private String PickRSNCount ;

    @SerializedName("LoadRSNCount")
    private String LoadRSNCount ;



    @SerializedName("DockNo")
    private String DockNo ;


    @SerializedName("ErrorMessage")
    private String ErrorMessage ;

    public VLPDResponseDTO(){

    }


    public VLPDResponseDTO(Set<? extends Map.Entry<?, ?>> entries)
    {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "SuggestedItem":
                    if(entry.getValue()!=null) {

                        List<LinkedTreeMap<?,?>> suggestedItemList=(List<LinkedTreeMap<?,?>>)entry.getValue();
                        List<ItemInfoDTO> lstSuggestedItem=new ArrayList<ItemInfoDTO>();
                        for(int i=0;i<suggestedItemList.size();i++)
                        {
                            ItemInfoDTO dto=new ItemInfoDTO(suggestedItemList.get(i).entrySet());
                            lstSuggestedItem.add(dto);
                        }

                        this.setSuggestedItem(lstSuggestedItem);
                    }
                    break;

                case "VlpdID":
                    if(entry.getValue()!=null) {
                        this.setVlpdID(entry.getValue().toString());
                    }
                    break;
                case "PreviousPickedItemResponce":
                    if(entry.getValue()!=null) {
                        List<LinkedTreeMap<?,?>> previousPickedItemResponceList=(List<LinkedTreeMap<?,?>>)entry.getValue();
                        List<ExecutionResponseDTO> lstPreviousPickedItemResponse=new ArrayList<ExecutionResponseDTO>();
                        ExecutionResponseDTO dto=null;
                        for(int i=0;i<previousPickedItemResponceList.size();i++)
                        {
                            dto=new ExecutionResponseDTO(previousPickedItemResponceList.get(i).entrySet());
                            lstPreviousPickedItemResponse.add(dto);
                        }

                        this.setPreviousPickedItemResponce(lstPreviousPickedItemResponse);
                    }
                    break;
                case "IsNotSuggested":
                    if (entry.getValue() != null) {
                        this.setSuggested(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                 case "VLPDNumber":
                    if (entry.getValue() != null) {
                        this.setVLPDNumber(entry.getValue().toString());
                    }
                    break;
                case "ID":
                    if (entry.getValue() != null) {
                        this.setID(entry.getValue().toString());
                    }
                    break;
                case "Result":
                    if (entry.getValue() != null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;
                case "PickRSNCount":
                    if (entry.getValue() != null) {
                        this.setPickRSNCount(entry.getValue().toString());
                    }
                    break;
                case "LoadRSNCount":
                    if (entry.getValue() != null) {
                        this.setLoadRSNCount(entry.getValue().toString());
                    }
                    break;
                case "ErrorMessage":
                    if (entry.getValue() != null) {
                        this.setErrorMessage(entry.getValue().toString());
                    }
                    break;

                   case "DockNo":
                    if (entry.getValue() != null) {
                        this.setDockNo(entry.getValue().toString());
                    }
                    break;
               /* case "PreviousPickedItemResponce":
                    if(entry.getValue()!=null) {


                        this.setPreviousPickedItemResponce(entry.getValue(.toString()));
                    }
                    break;*/
            }
        }
    }

    public Boolean getSuggested() {
        return isSuggested;
    }

    public void setSuggested(Boolean suggested) {
        isSuggested = suggested;
    }

    public List<ItemInfoDTO> getSuggestedItem() {
        return suggestedItem;
    }

    public void setSuggestedItem(List<ItemInfoDTO> suggestedItem) {
        this.suggestedItem = suggestedItem;
    }

    public List<ExecutionResponseDTO> getPreviousPickedItemResponce() {
        return previousPickedItemResponce;
    }

    public void setPreviousPickedItemResponce( List<ExecutionResponseDTO> previousPickedItemResponce) {
        this.previousPickedItemResponce = previousPickedItemResponce;
    }

    public String getVlpdID() {
        return VlpdID;
    }

    public void setVlpdID(String vlpdID) {
        VlpdID = vlpdID;
    }

    public String getVLPDNumber() {
        return VLPDNumber;
    }

    public void setVLPDNumber(String VLPDNumber) {
        this.VLPDNumber = VLPDNumber;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }


    public String getPickRSNCount() {
        return PickRSNCount;
    }

    public void setPickRSNCount(String pickRSNCount) {
        PickRSNCount = pickRSNCount;
    }

    public String getLoadRSNCount() {
        return LoadRSNCount;
    }

    public void setLoadRSNCount(String loadRSNCount) {
        LoadRSNCount = loadRSNCount;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        ErrorMessage = errorMessage;
    }

    public String getDockNo() {
        return DockNo;
    }

    public void setDockNo(String dockNo) {
        DockNo = dockNo;
    }

}