package com.inventrax.nilkamal_vna.interfaces;

import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface ApiInterface {

    @POST("Login/UserLogin")
    Call<String> UserLogin(@Body WMSCoreMessage oRequest);

    @POST("StockTakeLogin/UserLogin")
    Call<String> StockTakeLogin(@Body WMSCoreMessage oRequest);

    @POST("Login/GetPrinters")
    Call<String> GetPrinters(@Body WMSCoreMessage oRequest);

    @POST("Inbound/GetOpenInboundList")
    Call<String> GetOpenInboundList(@Body WMSCoreMessage oRequest);

    @POST("Inbound/GetClientbasedStorageLocations")
    Call<String> GetClientbasedStorageLocations(@Body WMSCoreMessage oRequest);

    @POST("Inbound/GetLocationType")
    Call<String> GetLocationType(@Body WMSCoreMessage oRequest);

    @POST("Inbound/ConfirmReceiptOnUniqueRSNScan")
    Call<String> ConfirmReceiptOnUniqueRSNScan(@Body WMSCoreMessage oRequest);

    @POST("Inbound/UpdateLBH")
    Call<String> UpdateLBH(@Body WMSCoreMessage oRequest);

    @POST("Inbound/PalletCreation")
    Call<String> PalletCreation(@Body WMSCoreMessage oRequest);

    @POST("Inbound/GetPalletCurrentLocation")
    Call<String> GetPalletCurrentLocation(@Body WMSCoreMessage oRequest);

    @POST("Inbound/GetRSNInfo")
    Call<String> GetRSNInfo(@Body WMSCoreMessage oRequest);

    @POST("Inbound/GetPalletValidation")
    Call<String> GetPalletValidation(@Body WMSCoreMessage oRequest);

    @POST("Inbound/PrintMouldedFurnitureLable")
    Call<String> PrintMouldedFurnitureLable(@Body WMSCoreMessage oRequest);

    @POST("Inbound/ConfirmHHReceiptONEANScan")
    Call<String> ConfirmHHReceiptONEANScan(@Body WMSCoreMessage oRequest);

    @POST("Inbound/GetPendingInboundInfo")
    Call<String> GetPendingInboundInfo(@Body WMSCoreMessage oRequest);

    @POST("PutAway/CheckInboundRefNumber")
    Call<String> CheckInboundRefNumber(@Body WMSCoreMessage oRequest);

    @POST("PutAway/CheckPalletAndSuggestPutawayLocation")
    Call<String> CheckPalletAndSuggestPutawayLocation(@Body WMSCoreMessage oRequest);

    @POST("PutAway/VNAPalletScan")
    Call<String> VNAPalletScan(@Body WMSCoreMessage oRequest);

    @POST("PutAway/GeneratePutawayandPickingSuggestion")
    Call<String> GeneratePutawayandPickingSuggestion(@Body WMSCoreMessage oRequest);

    @POST("PutAway/ConfirmBinPosting")
    Call<String> ConfirmBinPosting(@Body WMSCoreMessage oRequest);

    @POST("PutAway/UpdatePalletType")
    Call<String> UpdatePalletType(@Body WMSCoreMessage oRequest);

    @POST("PutAway/GetPalletTypeList")
    Call<String> GetPalletTypeList(@Body WMSCoreMessage oRequest);

    @POST("PutAway/PutawayandpickingSkip")
    Call<String> PutawayandpickingSkip(@Body WMSCoreMessage oRequest);

    @POST("PutAway/VNApickingSkip")
    Call<String> VNApickingSkip(@Body WMSCoreMessage oRequest);

    @POST("PutAway/CheckPalletandLocationValidation")
    Call<String> CheckPalletandLocationValidation(@Body WMSCoreMessage oRequest);

    @POST("PutAway/GetVNAPutawaySuggestion")
    Call<String> GetVNAPutawaySuggestion(@Body WMSCoreMessage oRequest);

    @POST("PutAway/UpsertBintoBinTransfer")
    Call<String> UpsertBintoBinTransfer(@Body WMSCoreMessage oRequest);

    @POST("PutAway/VNASuggestion")
    Call<String> VNASuggestion(@Body WMSCoreMessage oRequest);

    @POST("PutAway/GetPendingPutawyPalletList")
    Call<String> GetPendingPutawyPalletList(@Body WMSCoreMessage oRequest);

    @POST("PutAway/ValidatePalletOrLocation")
    Call<String> ValidatePalletOrLocation(@Body WMSCoreMessage oRequest);

    @POST("Inbound/GetInboundPalletInfo")
    Call<String> GetInboundPalletInfo(@Body WMSCoreMessage oRequest);


    @POST("Inbound/PrintEcomLabelsForFurniture")
    Call<String> PrintEcomLabelsForFurniture(@Body WMSCoreMessage oRequest);

    @POST("Inbound/ConfirmReceiptForSiteToSiteRSN")
    Call<String> ConfirmReceiptForSiteToSiteRSN(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetVLPDID")
    Call<String> GetVLPDID(@Body WMSCoreMessage oRequest);

    @POST("VLPD/PalletValidationForPtoP")
    Call<String> PalletValidationForPtoP(@Body WMSCoreMessage oRequest);


   @POST("VLPD/PickingandShortingSkip")
    Call<String> PickingandShortingSkip(@Body WMSCoreMessage oRequest);

    @POST("VLPD/PickingandShortingDockValidation")
    Call<String> PickingandShortingDockValidation(@Body WMSCoreMessage oRequest);

    @POST("VLPD/ExportPendingPallet")
    Call<String> ExportPendingPallet(@Body WMSCoreMessage oRequest);

    @POST("VLPD/VNAPalletValidation")
    Call<String> VNAPalletValidation(@Body WMSCoreMessage oRequest);

    @POST("VLPD/VNABintoBinMovement")
    Call<String> VNABintoBinMovement(@Body WMSCoreMessage oRequest);

    @POST("VLPD/PallettoPalletMovement")
    Call<String> PallettoPalletMovement(@Body WMSCoreMessage oRequest);

    @POST("VLPD/PrintNewRSN")
    Call<String> PrintNewRSN(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetVLPDPendingPalletCheck")
    Call<String> GetVLPDPendingPalletCheck(@Body WMSCoreMessage oRequest);

    @POST("Outbound/GetPendingOBDItemsForManualPicking")
    Call<String> GetPendingOBDItemsForManualPicking(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetVLPDPendingPalletsForDockMapping")
    Call<String> GetVLPDPendingPalletsForDockMapping(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetVLPDDockLocation")
    Call<String> GetVLPDDockLocation(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetPendigRSNToLoad")
    Call<String> GetPendigRSNToLoad(@Body WMSCoreMessage oRequest);

    @POST("VLPD/PickandCheck")
    Call<String> PickandCheck(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetPalletValidationandSuggestion")
    Call<String> GetPalletValidationandSuggestion(@Body WMSCoreMessage oRequest);

    @POST("Outbound/GetBoxNumberForOBD")
    Call<String> GetBoxNumberForOBD(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetVLPDStatus")
    Call<String> GetVLPDStatus(@Body WMSCoreMessage oRequest);

    @POST("VLPD/ConfirmVLPDLoading")
    Call<String> ConfirmVLPDLoading(@Body WMSCoreMessage oRequest);

    @POST("Outbound/GetOpenOBDListForManulaPicking")
    Call<String> GetOpenOBDListForManulaPicking(@Body WMSCoreMessage oRequest);

    @POST("VLPD/ValidatePalletAtVLPDDock")
    Call<String> ValidatePalletAtVLPDDock(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetOpenRefNumberList")
    Call<String> GetOpenRefNumberList(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetItemtoPick")
    Call<String> GetItemtoPick(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetOpenVNALOADLIST")
    Call<String> GetOpenVNALOADLIST(@Body WMSCoreMessage oRequest);

    @POST("VLPD/getVNAPendingLoadingCount")
    Call<String> getVNAPendingLoadingCount(@Body WMSCoreMessage oRequest);

    @POST("VLPD/VNAuniqueRSNLoading")
    Call<String> VNAuniqueRSNLoading(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetVNAPickingandShortingList")
    Call<String> GetVNAPickingandShortingList(@Body WMSCoreMessage oRequest);

    //Prasanna
    @POST("VLPD/ValidateBarcodeAndConfirmPicking")
    Call<String> ValidateBarcodeAndConfirmPicking(@Body WMSCoreMessage oRequest);

    @POST("Outbound/GetOpenOBDListForECOMPacking")
    Call<String> GetOpenOBDListForECOMPacking(@Body WMSCoreMessage oRequest);

    @POST("Outbound/CaptureOBDBoxPicking")
    Call<String> CaptureOBDBoxPicking(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetOpenVLPDListByPriority")
    Call<String> GetOpenVLPDListByPriority(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetOpenVLPDList")
    Call<String> GetOpenVLPDList(@Body WMSCoreMessage oRequest);

    //Prasanna
    @POST("VLPD/GetHHLoadingInfo")
    Call<String> GetHHLoadingInfo(@Body WMSCoreMessage oRequest);

    //Prasanna
    @POST("VLPD/ConfirmHHBoxLoading")
    Call<String> ConfirmHHBoxLoading(@Body WMSCoreMessage oRequest);


    @POST("Inbound/GetStockInformationByRSN")
    Call<String> GetStockInformationByRSN(@Body WMSCoreMessage oRequest);

    @POST("Exception/LogException")
    Call<String> LogException(@Body WMSCoreMessage oRequest);

    @POST("Transfers/GetInternaltransferPalletCurrentLocation")
    Call<String> GetInternaltransferPalletCurrentLocation(@Body WMSCoreMessage oRequest);

    @POST("Transfers/ConfirmBinTransferToPallet")
    Call<String> ConfirmBinTransferToPallet(@Body WMSCoreMessage oRequest);

    @POST("Transfers/MapPalletToLocation")
    Call<String> MapPalletToLocation(@Body WMSCoreMessage oRequest);

    @POST("Transfers/GetTempPalletItemCount")
    Call<String> GetTempPalletItemCount(@Body WMSCoreMessage oRequest);

    @POST("Transfers/GetInternaltransferInformation")
    Call<String> GetInternaltransferInformation(@Body WMSCoreMessage oRequest);

    @POST("Transfers/GetNewlyGeneratedRSNNumberByRSNNumber")
    Call<String> GetNewlyGeneratedRSNNumberByRSNNumber(@Body WMSCoreMessage oRequest);

    @POST("Transfers/PrintRSNnumber")
    Call<String> PrintRSNnumber(@Body WMSCoreMessage oRequest);

    @POST("VLPD/GetPendingPickItemsFromByDispatchID")
    Call<String> GetPendingPickItemsFromByDispatchID(@Body WMSCoreMessage oRequest);

    @POST("VLPD/UpdateSuggestedStatus")
    Call<String> UpdateSuggestedStatus(@Body WMSCoreMessage oRequest);

    @POST("Inbound/MapCaseNumberToUniqueRSN")
    Call<String> MapCaseNumberToUniqueRSN(@Body WMSCoreMessage oRequest);

    @POST("Outbound/GetBundleNumberForMatress")
    Call<String> GetBundleNumberForMatress(@Body WMSCoreMessage oRequest);

    @POST("Outbound/PrinteMatressBundle")
    Call<String> PrinteMatressBundle(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/CheckLocation")
    Call<String> CheckLocation(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/ConfirmCycleCount")
    Call<String> ConfirmCycleCount(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/GetCycleCountInformation")
    Call<String> GetCycleCountInformation(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/GetSKUDetails")
    Call<String> GetSKUDetails(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/GetPalletCurrentLocationForHUCycleCount")
    Call<String> GetPalletCurrentLocationForHUCycleCount(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/GetActualAndCCQuantiitesByLocation")
    Call<String> GetActualAndCCQuantiitesByLocation(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/CloseBinforCycleCount")
    Call<String> CloseBinforCycleCount(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/CheckLocationForHU")
    Call<String> CheckLocationForHU(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/GetSKUDetailsForHU")
    Call<String> GetSKUDetailsForHU(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/ConfirmCycleCountForHU")
    Call<String> ConfirmCycleCountForHU(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/CloseBinForHU")
    Call<String> CloseBinForHU(@Body WMSCoreMessage oRequest);

    @POST("CycleCount/ClearBin")
    Call<String> ClearBin(@Body WMSCoreMessage oRequest);

    @POST("VLPD/ConfirmMatressBunle")
    Call<String> ConfirmMatressBunle(@Body WMSCoreMessage oRequest);

    @POST("Outbound/CaptureMatressBundlePacking")
    Call<String> CaptureMatressBundlePacking(@Body WMSCoreMessage oRequest);

    @POST("Inbound/MoveStockToAuditBin")
    Call<String> MoveStockToAuditBin(@Body WMSCoreMessage oRequest);

    @POST("HHStockTake/GetLocationStatusForHH")
    Call<String> GetLocationStatusForHH(@Body WMSCoreMessage oRequest);

    @POST("HHStockTake/GetHHDetails")
    Call<String> GetHHDetails(@Body WMSCoreMessage oRequest);

    @POST("HHStockTake/UpsertEANDetails")
    Call<String> UpsertEANDetails(@Body WMSCoreMessage oRequest);

    @POST("HHStockTake/CloseHHBin")
    Call<String> CloseHHBin(@Body WMSCoreMessage oRequest);

    @POST("HHStockTake/GetStockDetailsHH")
    Call<String> GetStockDetailsHH(@Body WMSCoreMessage oRequest);

    @POST("Outbound/GetBoxNumberForVLPD")
    Call<String> GetBoxNumberForVLPD(@Body WMSCoreMessage oRequest);

}