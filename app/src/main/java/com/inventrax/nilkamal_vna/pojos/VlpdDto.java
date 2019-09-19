package com.inventrax.nilkamal_vna.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class VlpdDto {

    @SerializedName("ID")
    private String iD;

    @SerializedName("UserId")
    private String UserId;

    @SerializedName("VLPDNumber")
    private String vLPDNumber;

    @SerializedName("Result")
    private String result;

    @SerializedName("PickedPalletNumber")
    private String pickedPalletNumber;

    @SerializedName("Location")
    private String location;

    @SerializedName("RSNNumber")
    private String rSNNumber;

    @SerializedName("Quantity")
    private String quantity;

    @SerializedName("Type")
    private String type;

    @SerializedName("OBDNumber")
    private String OBDNumber;

    @SerializedName("BoxNumber")
    private String BoxNumber;

    @SerializedName("AssignedId")
    private String AssignedId;

    @SerializedName("Mcode")
    private String Mcode;

    @SerializedName("Description")
    private String Description;

    @SerializedName("DockName")
    private String DockName;

    @SerializedName("BatchNo")
    private String BatchNo;

    @SerializedName("ExpDate")
    private String ExpDate;

    @SerializedName("MfgDate")
    private String MfgDate;

    @SerializedName("PendingQty")
    private String PendingQty;


    @SerializedName("PickedQty")
    private String PickedQty;

    @SerializedName("SKUPendingQty")
    private String SKUPendingQty;

    @SerializedName("HUNo")
    private String HUNo;

    @SerializedName("HUNumber")
    private String HUNumber;

    @SerializedName("DockLocation")
    private String DockLocation;

    @SerializedName("HUSize")
    private String HUSize;

    @SerializedName("StorageLocation")
    private String StorageLocation;

    @SerializedName("NewUniqueRSN")
    private String NewUniqueRSN;



    @SerializedName("UniqueRSN")
    private String UniqueRSN;


    @SerializedName("PalletNo")
    private String palletNo;




    @SerializedName("IpAddress")
    private String IpAddress;

    @SerializedName("Message")
    private String Message;


    public VlpdDto() { }


    public VlpdDto(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "ID":
                    if (entry.getValue() != null) {
                        this.setiD(entry.getValue().toString());
                    }
                    break;

                case "VLPDNumber":
                    if (entry.getValue() != null) {
                        this.setvLPDNumber(entry.getValue().toString());
                    }
                    break;

                case "Result":
                    if (entry.getValue() != null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;

                case "PickedPalletNumber":
                    if (entry.getValue() != null) {
                        this.setPickedPalletNumber(entry.getValue().toString());
                    }
                    break;
                case "RSNNumber":
                    if (entry.getValue() != null) {
                        this.setrSNNumber(entry.getValue().toString());
                    }
                    break;

                case "Quantity":
                    if (entry.getValue() != null) {
                        this.setQuantity(entry.getValue().toString());
                    }
                    break;
                case "Type":
                    if (entry.getValue() != null) {
                        this.setType(entry.getValue().toString());
                    }
                    break;
                case "OBDNumber":
                    if (entry.getValue() != null) {
                        this.setOBDNumber(entry.getValue().toString());
                    }
                    break;
                case "BoxNumber":
                    if (entry.getValue() != null) {
                        this.setBoxNumber(entry.getValue().toString());
                    }
                    break;
                case "AssignedId":
                    if (entry.getValue() != null) {
                        this.setAssignedId(entry.getValue().toString());
                    }
                    break;
                case "Mcode":
                    if (entry.getValue() != null) {
                        this.setMcode(entry.getValue().toString());
                    }
                    break;
                case "Description":
                    if (entry.getValue() != null) {
                        this.setDescription(entry.getValue().toString());
                    }
                    break;
                case "DockName":
                    if (entry.getValue() != null) {
                        this.setDockName(entry.getValue().toString());
                    }
                    break;
                case "BatchNo":
                    if (entry.getValue() != null) {
                        this.setBatchNo(entry.getValue().toString());
                    }
                    break;
                case "ExpDate":
                    if (entry.getValue() != null) {
                        this.setExpDate(entry.getValue().toString());
                    }
                    break;
                case "MfgDate":
                    if (entry.getValue() != null) {
                        this.setMfgDate(entry.getValue().toString());
                    }
                    break;
                case "PendingQty":
                    if (entry.getValue() != null) {
                        this.setPendingQty(entry.getValue().toString());
                    }
                    break;
                case "HUNo":
                    if (entry.getValue() != null) {
                        this.setHUNo(entry.getValue().toString());
                    }
                    break;
                 case "HUSize":
                    if (entry.getValue() != null) {
                        this.setHUSize(entry.getValue().toString());
                    }
                    break;
                 case "StorageLocation":
                    if (entry.getValue() != null) {
                        this.setStorageLocation(entry.getValue().toString());
                    }
                    break;
                case "NewUniqueRSN":
                    if (entry.getValue() != null) {
                        this.setNewUniqueRSN(entry.getValue().toString());
                    }
                break;
                case "UserId":
                    if (entry.getValue() != null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;
                case "UniqueRSN":
                    if (entry.getValue() != null) {
                        this.setUniqueRSN(entry.getValue().toString());
                    }
                    break;
                case "PalletNo":
                    if (entry.getValue() != null) {
                        this.setPalletNo(entry.getValue().toString());
                    }
                    break;
                case "Message":
                    if (entry.getValue() != null) {
                        this.setMessage(entry.getValue().toString());
                    }
                    break;
                case "IpAddress":
                    if (entry.getValue() != null) {
                        this.setIpAddress(entry.getValue().toString());
                    }
                    break;
                 case "PickedQty":
                    if (entry.getValue() != null) {
                        this.setPickedQty(entry.getValue().toString());
                    }
                    break;
            }
        }
    }

    public String getOBDNumber() {
        return OBDNumber;
    }

    public void setOBDNumber(String OBDNumber) {
        this.OBDNumber = OBDNumber;
    }

    public String getBoxNumber() {
        return BoxNumber;
    }

    public void setBoxNumber(String boxNumber) {
        BoxNumber = boxNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getiD() {
        return iD;
    }

    public void setiD(String iD) {
        this.iD = iD;
    }

    public String getvLPDNumber() {
        return vLPDNumber;
    }

    public void setvLPDNumber(String vLPDNumber) {
        this.vLPDNumber = vLPDNumber;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPickedPalletNumber() {
        return pickedPalletNumber;
    }

    public void setPickedPalletNumber(String pickedPalletNumber) {
        this.pickedPalletNumber = pickedPalletNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getrSNNumber() {
        return rSNNumber;
    }

    public void setrSNNumber(String rSNNumber) {
        this.rSNNumber = rSNNumber;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getAssignedId() {
        return AssignedId;
    }

    public void setAssignedId(String assignedId) {
        AssignedId = assignedId;
    }

    public String getMcode() {
        return Mcode;
    }

    public void setMcode(String mcode) {
        Mcode = mcode;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDockName() {
        return DockName;
    }

    public void setDockName(String dockName) {
        DockName = dockName;
    }

    public String getBatchNo() {
        return BatchNo;
    }

    public void setBatchNo(String batchNo) {
        BatchNo = batchNo;
    }

    public String getExpDate() {
        return ExpDate;
    }

    public void setExpDate(String expDate) {
        ExpDate = expDate;
    }

    public String getMfgDate() {
        return MfgDate;
    }

    public void setMfgDate(String mfgDate) {
        MfgDate = mfgDate;
    }

    public String getPendingQty() {
        return PendingQty;
    }

    public void setPendingQty(String pendingQty) {
        PendingQty = pendingQty;
    }

    public String getHUNo() {
        return HUNo;
    }

    public void setHUNo(String HUNo) {
        this.HUNo = HUNo;
    }

    public String getHUSize() {
        return HUSize;
    }

    public void setHUSize(String HUSize) {
        this.HUSize = HUSize;
    }

    public String getStorageLocation() {
        return StorageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        StorageLocation = storageLocation;
    }

    public String getNewUniqueRSN() {
        return NewUniqueRSN;
    }

    public void setNewUniqueRSN(String newUniqueRSN) {
        NewUniqueRSN = newUniqueRSN;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getUniqueRSN() {
        return UniqueRSN;
    }

    public void setUniqueRSN(String uniqueRSN) {
        UniqueRSN = uniqueRSN;
    }

    public String getPalletNo() {
        return palletNo;
    }

    public void setPalletNo(String palletNo) {
        this.palletNo = palletNo;
    }


    public String getSKUPendingQty() {
        return SKUPendingQty;
    }

    public void setSKUPendingQty(String SKUPendingQty) {
        this.SKUPendingQty = SKUPendingQty;
    }

    public String getHUNumber() {
        return HUNumber;
    }

    public void setHUNumber(String HUNumber) {
        this.HUNumber = HUNumber;
    }

    public String getDockLocation() {
        return DockLocation;
    }

    public void setDockLocation(String dockLocation) {
        DockLocation = dockLocation;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getIpAddress() {
        return IpAddress;
    }

    public void setIpAddress(String ipAddress) {
        IpAddress = ipAddress;
    }


    public String getPickedQty() {
        return PickedQty;
    }

    public void setPickedQty(String pickedQty) {
        PickedQty = pickedQty;
    }

}
