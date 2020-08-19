package com.inventrax.nilkamal_vna.fragments.HU;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cipherlab.barcode.GeneralString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;
import com.inventrax.nilkamal_vna.R;
import com.inventrax.nilkamal_vna.activities.MainActivity;
import com.inventrax.nilkamal_vna.common.Common;
import com.inventrax.nilkamal_vna.common.constants.EndpointConstants;
import com.inventrax.nilkamal_vna.common.constants.ErrorMessages;
import com.inventrax.nilkamal_vna.fragments.HomeFragment;
import com.inventrax.nilkamal_vna.interfaces.ApiInterface;
import com.inventrax.nilkamal_vna.pojos.ExecutionResponseDTO;
import com.inventrax.nilkamal_vna.pojos.InternalTransferDTO;
import com.inventrax.nilkamal_vna.pojos.ItemInfoDTO;
import com.inventrax.nilkamal_vna.pojos.LoginUserDTO;
import com.inventrax.nilkamal_vna.pojos.OutboundDTO;
import com.inventrax.nilkamal_vna.pojos.PrinterDetailsDTO;
import com.inventrax.nilkamal_vna.pojos.VLPDRequestDTO;
import com.inventrax.nilkamal_vna.pojos.VLPDResponseDTO;
import com.inventrax.nilkamal_vna.pojos.VlpdDto;
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.pojos.WMSExceptionMessage;
import com.inventrax.nilkamal_vna.searchableSpinner.SearchableSpinner;
import com.inventrax.nilkamal_vna.services.RestService;
import com.inventrax.nilkamal_vna.util.CustomEditText;
import com.inventrax.nilkamal_vna.util.DialogUtils;
import com.inventrax.nilkamal_vna.util.ExceptionLoggerUtils;
import com.inventrax.nilkamal_vna.util.FragmentUtils;
import com.inventrax.nilkamal_vna.util.ProgressDialogUtils;
import com.inventrax.nilkamal_vna.util.ScanValidator;
import com.inventrax.nilkamal_vna.util.SoundUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VNASLocToSLocFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_SLOC_TO_SLOC";
    private View rootView;
    private RelativeLayout rlPick;
    private TextView  lblSKU, lblDesc, lblBatch,
            lblPendingQty, lblHUNo, lblHUSize,lblVLPDNumber,lblScannedBarcode;
    private CardView cvScanFromPallet,cvScanToPallet, cvScanBarcode;
    private ImageView ivScanFromPallet,ivScanToPallet, ivScanBarcode;
    private TextInputLayout txtInputLayoutPallet, txtInputLayoutQty;
    private CustomEditText etFromPallet,etToPallet, etQty;
    private Button  btnPick, btnCloseOne;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null, materialType = null, vlpdId = null, vlpdTypeId = null, SkipReason = null, skipvlpdId = null;
    VLPDResponseDTO vlpdresponseobj = null;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    ArrayList<String> sloc;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    String VlpdNumber="",assginedid="",ipAdress="",sUniqueRSN="",sPalletNo="";
    Dialog pickingSkipdialog;
    boolean isNewRsn=false;


    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public VNASLocToSLocFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.vna_sloc_to_sloc_fragment, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;

    }

    // Form controls
    private void loadFormControls() {

        rlPick = (RelativeLayout) rootView.findViewById(R.id.rlPick);
        lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);
        lblSKU = (TextView) rootView.findViewById(R.id.lblSKU);
        lblBatch = (TextView) rootView.findViewById(R.id.lblBatch);
        lblPendingQty = (TextView) rootView.findViewById(R.id.lblPendingQty);
        lblHUNo = (TextView) rootView.findViewById(R.id.lblHUNo);
        lblHUSize = (TextView) rootView.findViewById(R.id.lblHUSize);
        lblVLPDNumber = (TextView) rootView.findViewById(R.id.lblVLPDNumber);
        lblScannedBarcode = (TextView) rootView.findViewById(R.id.lblScannedBarcode);

        if(getArguments()!=null){
            lblVLPDNumber.setText(getArguments().getString("SLOCNumber"));
            if(getArguments().getString("VLPDNumber")!=null){
                lblVLPDNumber.setText(getArguments().getString("VLPDNumber"));
            }
        }

        cvScanFromPallet = (CardView) rootView.findViewById(R.id.cvScanFromPallet);
        cvScanToPallet = (CardView) rootView.findViewById(R.id.cvScanToPallet);
        cvScanBarcode = (CardView) rootView.findViewById(R.id.cvScanBarcode);

        ivScanFromPallet = (ImageView) rootView.findViewById(R.id.ivScanFromPallet);
        ivScanToPallet = (ImageView) rootView.findViewById(R.id.ivScanToPallet);
        ivScanBarcode = (ImageView) rootView.findViewById(R.id.ivScanBarcode);

        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);

        etFromPallet = (CustomEditText) rootView.findViewById(R.id.etFromPallet);
        etToPallet = (CustomEditText) rootView.findViewById(R.id.etToPallet);
        etQty = (CustomEditText) rootView.findViewById(R.id.etQty);

        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);

        btnPick = (Button) rootView.findViewById(R.id.btnPick);

        etQty.setEnabled(false);


        sloc = new ArrayList<>();
        common = new Common();

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        btnCloseOne.setOnClickListener(this);
        btnPick.setEnabled(false);

        vlpdresponseobj = new VLPDResponseDTO();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();
        btnPick.setOnClickListener(this);
        btnPick.setTextColor(getResources().getColor(R.color.black));
        btnPick.setBackgroundResource(R.drawable.button_hide);

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        //For Honey well
        AidcManager.create(getActivity(), new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {

                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                try {
                    barcodeReader.claim();
                    HoneyWellBarcodeListeners();

                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnCloseOne:
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnPick:
                break;

            default:
                break;

        }
    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                getScanner = barcodeReadEvent.getBarcodeData();
                ProcessScannedinfo(getScanner);

            }

        });
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {

    }

    public void HoneyWellBarcodeListeners() {

        barcodeReader.addTriggerListener(this);

        if (barcodeReader != null) {
            // set the trigger mode to client control
            barcodeReader.addBarcodeListener(this);
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            } catch (UnsupportedPropertyException e) {
                // Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Enable bad read response
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }

    }

    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (scannedData != null && !common.isPopupActive()) {

            if(!ProgressDialogUtils.isProgressActive()) {

                if (ScanValidator.IsPalletScanned(scannedData)) {
                    if(etFromPallet.getText().toString().isEmpty()){
                        GetSlocVNAPickingandShortingList(scannedData);
                    }
                    else if(etToPallet.getText().toString().isEmpty()){
                        etToPallet.setText(scannedData);
                        cvScanToPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanToPallet.setImageResource(R.drawable.check);
                    }
                    return;
                }

                if (ScanValidator.IsRSNScanned(scannedData) || ScanValidator.IsBundleScanOnBundling(scannedData)) {
                    if(etFromPallet.getText().toString().isEmpty() || etToPallet.getText().toString().isEmpty()){
                        common.showUserDefinedAlertType("Please select from pallet or to pallet",getActivity(),getContext(),"Error");
                    }else{
                        SlocPickandCheck(scannedData);
                    }
                    return;
                }

            }else {
                if(!common.isPopupActive())
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");
                }
                sound.alertWarning(getActivity(),getContext());
            }

        }

    }

    public void clearAllFileds(){
            isNewRsn=false;
            lblSKU.setText("");
            lblBatch.setText("");
            lblHUNo.setText("");
            lblHUSize.setText("");
            lblPendingQty.setText("");
            lblDesc.setText("");

            etFromPallet.setText("");
             etToPallet.setText("");

            cvScanFromPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
            ivScanFromPallet.setImageResource(R.drawable.fullscreen_img);

            cvScanToPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
            ivScanToPallet.setImageResource(R.drawable.fullscreen_img);
    }

    public  void GetSlocVNAPickingandShortingList(final String scannedData) {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.Outbound,getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setUserId(userId);
            vlpdDto.setvLPDNumber(lblVLPDNumber.getText().toString());
            vlpdDto.setPalletNo(scannedData);
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetSlocVNAPickingandShortingList(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetOpenInboundList",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");

            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }
                                etFromPallet.setText("");
                                etToPallet.setText("");
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage,getActivity(),getContext());

                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lVLPD = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVLPD = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                etFromPallet.setText(scannedData);

                                List<VlpdDto> lstDto = new ArrayList<VlpdDto>();


                                for (int i = 0; i < _lVLPD.size(); i++) {
                                    VlpdDto dto = new VlpdDto(_lVLPD.get(i).entrySet());
                                    lstDto.add(dto);
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                if(lstDto.size()>0){
                                    if(lstDto.get(0).getResult().equals("1")){
                                        lblSKU.setText(lstDto.get(0).getMcode());
                                        lblDesc.setText(lstDto.get(0).getDescription());
                                        lblBatch.setText(lstDto.get(0).getBatchNo());
                                        lblPendingQty.setText(lstDto.get(0).getPendingQty());
                                        lblHUNo.setText(lstDto.get(0).getHUNo());
                                        lblHUSize.setText(lstDto.get(0).getHUSize());
                                        VlpdNumber=lstDto.get(0).getvLPDNumber();
                                        assginedid=lstDto.get(0).getAssignedId();
                                        etFromPallet.setText(scannedData);

                                        cvScanFromPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanFromPallet.setImageResource(R.drawable.check);

                                    }else if(lstDto.get(0).getResult().equals("0")){
                                        clearAllFileds();
                                        Common.setIsPopupActive(true);
                                        sPalletNo = scannedData;
                                        new SoundUtils().alertError(getActivity(), getContext());
                                        DialogUtils.showAlertDialog(getActivity(), "Warning", errorMessages.EMC_093, R.drawable.warning_img, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        Common.setIsPopupActive(false);
                                                        GetVLPDPendingPalletCheck();
                                                        break;
                                                }
                                            }
                                        });

                                    }else{
                                        if(lstDto.get(0).getResult().equals("-1")){
                                            clearAllFileds();
                                            common.showUserDefinedAlertType("In-Valid Pallet",getActivity(),getContext(),"Error");
                                        }
                                    }

                                }else{
                                    clearAllFileds();
                                    common.showUserDefinedAlertType("Error while grtting data",getActivity(),getContext(),"Error");
                                }

                            }


                        } catch(Exception ex){
                            try {

                                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetOpenInboundList",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }


                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();

                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetOpenInboundList",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        }catch (Exception ex)
        {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetOpenInboundList",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }

    private void GetVLPDPendingPalletCheck() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setUserId(userId);
            vlpdDto.setvLPDNumber(lblVLPDNumber.getText().toString());
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.GetVLPDPendingPalletCheck(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");

            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                                ProgressDialogUtils.closeProgressDialog();
                                List<LinkedTreeMap<?, ?>> _lVlpd = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVlpd = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VlpdDto vlpdDto1 = null;
                                for (int i = 0; i < _lVlpd.size(); i++) {
                                    vlpdDto1 = new VlpdDto(_lVlpd.get(i).entrySet());
                                }

                                if (vlpdDto1.getResult().equals("-1")) {
/*                                    Common.setIsPopupActive(true);
                                    soundUtils.alertError(getActivity(), getContext());
                                    DialogUtils.showAlertDialog(getActivity(), "Warning", "pending pallets available for this vlpd# "+txtVLPDNumber.getText().toString(), R.drawable.warning_img, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    Common.setIsPopupActive(false);*/

                                    new SoundUtils().alertError(getActivity(), getContext());
                                    Common.setIsPopupActive(true);
                                    DialogUtils.showConfirmDialog(getActivity(), "Alert", "Do you want to move this pallet to priority bin zone?", "Yes", "No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == -1) {
                                                GetPalletValidationandSuggestion("1");
                                                // Toast.makeText(LoginActivity.this, "Yes", Toast.LENGTH_SHORT).show();
                                            }
                                            if (i == -2) {
                                                GetPalletValidationandSuggestion("2");
                                                //  Toast.makeText(LoginActivity.this, "No", Toast.LENGTH_SHORT).show();
                                            }
                                            Common.setIsPopupActive(false);
                                        }
                                    });

/*                                                    break;
                                            }
                                        }
                                    });*/
                                } else {
/*                                    Common.setIsPopupActive(true);
                                    soundUtils.alertError(getActivity(), getContext());
                                    DialogUtils.showAlertDialog(getActivity(), "Warning", "There is no pending pallets for this vlpd# "+txtVLPDNumber.getText().toString(), R.drawable.warning_img, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    Common.setIsPopupActive(false);*/
                                    Common.setIsPopupActive(true);
                                    new SoundUtils().alertError(getActivity(), getContext());
                                    DialogUtils.showConfirmDialog(getActivity(), "Alert", "Do you want to move this pallet to priority bin zone?", "Yes", "No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (i == -1) {
                                                GetPalletValidationandSuggestion("1");
                                                // Toast.makeText(LoginActivity.this, "Yes", Toast.LENGTH_SHORT).show();
                                            }
                                            if (i == -2) {
                                                GetPalletValidationandSuggestion("2");
                                                //  Toast.makeText(LoginActivity.this, "No", Toast.LENGTH_SHORT).show();
                                            }
                                            Common.setIsPopupActive(false);
                                        }
                                    });

/*                                                     break;
                                           }
                                        }
                                    });*/
                                }


                            }

                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    private void GetPalletValidationandSuggestion(final String Type) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setUserId(userId);
            vlpdDto.setType(Type);
            vlpdDto.setvLPDNumber(lblVLPDNumber.getText().toString());
            vlpdDto.setPickedPalletNumber(sPalletNo);
            message.setEntityObject(vlpdDto);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.GetPalletValidationandSuggestion(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");

            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lVlpd = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVlpd = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VlpdDto vlpdDto1 = null;
                                for (int i = 0; i < _lVlpd.size(); i++) {
                                    vlpdDto1 = new VlpdDto(_lVlpd.get(i).entrySet());
                                }

                                if (vlpdDto1.getResult().equals("1") || vlpdDto1.getResult().equals("-1")) {

                                    Bundle bundle = new Bundle();
                                    bundle.putString("VLPDNumber", lblVLPDNumber.getText().toString());
                                    bundle.putString("Pallet", sPalletNo);
                                    bundle.putString("ActualLoc", vlpdDto1.getActvalLocation());
                                    bundle.putString("SuggestedLoc", vlpdDto1.getSuggestedLoc());
                                    bundle.putString("Type", Type);
                                    bundle.putString("isSLoc", "true");

                                    PriorityBinZoneFragment priorityBinZoneFragment = new PriorityBinZoneFragment();
                                    priorityBinZoneFragment.setArguments(bundle);
                                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, priorityBinZoneFragment);

                                } else {
                                    common.showUserDefinedAlertType("No items available on this pallet.", getActivity(), getContext(), "Error");
                                }
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    public  void SlocPickandCheck(final String scannedData) {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.Outbound,getContext());
            OutboundDTO outboundDTO = new OutboundDTO();
            outboundDTO.setUserId(userId);
            outboundDTO.setVlpdNumber(lblVLPDNumber.getText().toString());
            outboundDTO.setHUNumber(lblHUNo.getText().toString());
            outboundDTO.setHUsize(lblHUSize.getText().toString());
            outboundDTO.setPalletNo(etToPallet.getText().toString());
            outboundDTO.setFromPalletno(etFromPallet.getText().toString());
            outboundDTO.setMcode(lblSKU.getText().toString());
            outboundDTO.setSKUPendingQty(lblPendingQty.getText().toString());
            outboundDTO.setBatchNo(lblBatch.getText().toString());
            outboundDTO.setMfgDate("");
            outboundDTO.setExpDate("");
            outboundDTO.setAssignedId(assginedid);
            if(isNewRsn){
                outboundDTO.setUniqueRSN(sUniqueRSN);
                outboundDTO.setNewUniqueRSN(scannedData);
            }else{
                outboundDTO.setUniqueRSN(scannedData);
                outboundDTO.setNewUniqueRSN("");
            }
            message.setEntityObject(outboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.SlocPickandCheck(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetOpenInboundList",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");

            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage,getActivity(),getContext());

                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lOutbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lOutbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                               lblScannedBarcode.setText(scannedData);

                                List<VlpdDto> lstDto = new ArrayList<VlpdDto>();

                                ProgressDialogUtils.closeProgressDialog();
                                for (int i = 0; i < _lOutbound.size(); i++) {
                                    VlpdDto dto = new VlpdDto(_lOutbound.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                if(lstDto.size()>0){
                                    if(lstDto.get(0).getMessage().equals("1")){
                                        isNewRsn=false;
                                        common.setIsPopupActive(true);
                                        new SoundUtils().alertSuccess(getActivity(), getActivity());
                                        DialogUtils.showAlertDialog(getActivity(), "Success", "Success Transfred", R.drawable.success,new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        common.setIsPopupActive(false);
                                                        GetSlocVNAPickingandShortingList(etFromPallet.getText().toString());
                                                        break;
                                                }
                                            }
                                        });
                                       // common.showUserDefinedAlertType("Success Transfred",getActivity(),getContext(),"Success");

                                    }else if(lstDto.get(0).getMessage().equals("-1")){
                                        common.showUserDefinedAlertType("Duplicate RSN Generated",getActivity(),getContext(),"Error");
                                    }else if(lstDto.get(0).getMessage().equals("-2")){
                                        common.showUserDefinedAlertType("Invalid Pallet",getActivity(),getContext(),"Error");
                                    }else if(lstDto.get(0).getMessage().equals("-3")){
                                        common.showUserDefinedAlertType("To-Pallet location or not mapped",getActivity(),getContext(),"Error");
                                    } else if(lstDto.get(0).getMessage().equals("-4")){
                                        common.showUserDefinedAlertType("Do Unbundling",getActivity(),getContext(),"Error");
                                    } else if(lstDto.get(0).getMessage().equals("-5")){
                                        sUniqueRSN=scannedData;
                                        getPrinters();
                                    }else if(lstDto.get(0).getMessage().equals("-6")){
                                        common.showUserDefinedAlertType("Please Contact Support team",getActivity(),getContext(),"Error");
                                    }else{
                                        common.showUserDefinedAlertType(lstDto.get(0).getMessage(),getActivity(),getContext(),"Error");
                                    }
                                }else{
                                    common.showUserDefinedAlertType("Error while getting data",getActivity(),getContext(),"Error");
                                }

                            }


                        } catch(Exception ex){
                            try {

                                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetOpenInboundList",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }


                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();

                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetOpenInboundList",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        }catch (Exception ex)
        {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"GetOpenInboundList",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }


    private void PrintNewRSN() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setUserId(userId);
            vlpdDto.setUniqueRSN(sUniqueRSN);
            vlpdDto.setIpAddress(ipAdress);
            vlpdDto.setMcode(lblSKU.getText().toString());
            vlpdDto.setAssignedId(assginedid);
            vlpdDto.setPickedQty(lblPendingQty.getText().toString());
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.PrintNewRSN(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");

            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                                List<LinkedTreeMap<?, ?>> _lVlpd = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVlpd = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VlpdDto vlpdDto1 = null;
                                for (int i = 0; i < _lVlpd.size(); i++) {
                                    vlpdDto1 = new VlpdDto(_lVlpd.get(i).entrySet());
                                }

                                if (vlpdDto1.getMessage() != null) {

                                    if (vlpdDto1.getMessage().equals("1")) {
                                        pickingSkipdialog.dismiss();
                                        isNewRsn = true;
                                        common.showUserDefinedAlertType("Please Scan New RSN", getActivity(), getContext(), "Warning");
                                    } else {
                                        Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    pickingSkipdialog.dismiss();
                                    common.showUserDefinedAlertType("Unable to print RSN Check network or printer configuration", getActivity(), getContext(), "Error");
                                }

                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void getPrinters() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.LoginUserDTO, getContext());
            LoginUserDTO oLoginDTO = new LoginUserDTO();
            oLoginDTO.setMailID("1");
            message.setEntityObject(oLoginDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetPrinters(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_01", getContext());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                }
                                ProgressDialogUtils.closeProgressDialog();
                                DialogUtils.showAlertDialog(getActivity(), owmsExceptionMessage.getWMSMessage());
                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lPrinters = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPrinters = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<String> lstPrinters = new ArrayList<>();
                                List<PrinterDetailsDTO> lstDto = new ArrayList<PrinterDetailsDTO>();

                                for (int i = 0; i < _lPrinters.size(); i++) {
                                    PrinterDetailsDTO dto = new PrinterDetailsDTO(_lPrinters.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                for (int i = 0; i < lstDto.size(); i++) {
                                    lstPrinters.add(lstDto.get(i).getDeviceIP());
                                }

                                if (lstPrinters == null) {
                                    ProgressDialogUtils.closeProgressDialog();
                                    DialogUtils.showAlertDialog(getActivity(), "No Printers Available");
                                } else {
                                    ProgressDialogUtils.closeProgressDialog();
                                    pickingSkipdialog = new Dialog(getActivity());
                                    pickingSkipdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    pickingSkipdialog.setCancelable(false);
                                    pickingSkipdialog.setContentView(R.layout.pinter_dialog);

                                    TextView btnOk = (TextView) pickingSkipdialog.findViewById(R.id.btnOk);
                                    btnOk.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            PrintNewRSN();
                                        }
                                    });

                                    TextView btnCancel = (TextView) pickingSkipdialog.findViewById(R.id.btnCancel);
                                    btnCancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            pickingSkipdialog.dismiss();
                                        }
                                    });

                                    final SearchableSpinner spinnerSelectPrinter = (SearchableSpinner) pickingSkipdialog.findViewById(R.id.spinnerSelectReason);
                                    ArrayAdapter arrayAdapterSelectPrinter = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstPrinters);
                                    spinnerSelectPrinter.setAdapter(arrayAdapterSelectPrinter);
                                    spinnerSelectPrinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                            ipAdress = spinnerSelectPrinter.getSelectedItem().toString();
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {
                                        }
                                    });


                                    pickingSkipdialog.show();

                                }
                            }
                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getContext());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getContext());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", getContext());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
        }
    }


    // sending exception to the database
    public void logException() {

        try {

            String textFromFile = exceptionLoggerUtils.readFromFile(getActivity());
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Exception, getActivity());
            WMSExceptionMessage wmsExceptionMessage = new WMSExceptionMessage();
            wmsExceptionMessage.setWMSMessage(textFromFile);
            message.setEntityObject(wmsExceptionMessage);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.LogException(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                        } catch (Exception ex) {

                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002", getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();


                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
            }
        }
        catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
            }
            // notifications while paused.
            barcodeReader.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                // Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_vna_sloc_to_sloc));
    }

    //Barcode scanner API
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (barcodeReader != null) {
            // unregister barcode event listener honeywell
            barcodeReader.removeBarcodeListener((BarcodeReader.BarcodeListener) this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener((BarcodeReader.TriggerListener) this);
        }

        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", false);
        getActivity().sendBroadcast(RTintent);
        getActivity().unregisterReceiver(this.myDataReceiver);
        super.onDestroyView();
    }

}