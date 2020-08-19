package com.inventrax.nilkamal_vna.fragments.HU;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.inventrax.nilkamal_vna.fragments.PendingInboundListFragment;
import com.inventrax.nilkamal_vna.interfaces.ApiInterface;
import com.inventrax.nilkamal_vna.pojos.DimensionsDTO;
import com.inventrax.nilkamal_vna.pojos.ExecutionResponseDTO;
import com.inventrax.nilkamal_vna.pojos.InboundDTO;
import com.inventrax.nilkamal_vna.pojos.InventoryDTO;
import com.inventrax.nilkamal_vna.pojos.StorageLocationDTO;
import com.inventrax.nilkamal_vna.pojos.VNALoadingDTO;
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.pojos.WMSExceptionMessage;
import com.inventrax.nilkamal_vna.searchableSpinner.SearchableSpinner;
import com.inventrax.nilkamal_vna.services.RestService;
import com.inventrax.nilkamal_vna.util.CustomEditText;
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


public class NewBundlingFragmentHU extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_RSN_GOODS_HU";
    private View rootView;
    private RelativeLayout rlReceive, rlPrint;
    private TextView lblStoreRefNo, lblInboundQty, lblScannedSku, lblDesc, lblBundle;
    private CardView  cvScanSku;
    private ImageView  ivScanSku;
    private CustomEditText etRSN, etLength, etBreadth, etHeight, etWeight, etVolume,etBundlePrint;
    private EditText  etStackCount, etPrintQty, etPrinterIP;
    private Button btnConfirmLBH,  btnClose;
    SoundUtils soundUtils = null;
    FragmentUtils fragmentUtils;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null;
    String DefaultSloc = null;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String storageloc = null, clientId = null, InboundId = null, MaterialMasterId = null, materialType = null;
    ArrayList<String> sloc;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private boolean IsPalletLoading = false;
    private boolean IsReceivingBin = false;
    private InboundDTO oInboundDataDTO = null;
    double palletMaxVolume = 0;
    double palletLoadedVolume = 0;
    int palletMaxWeight = 0;
    int palletLoadedWeight = 0;
    public String auditbinLocation = null;
    Dialog validPalletDialog;

    private String pallet = null, location = null, rsn = null, L = null, B = null, H = null, W = null, box = null, qty = null, vol = null, twt = null, caseString = null, sku = null, desc = null, count = null, ipAddress = null, printerIPAddress = null;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public NewBundlingFragmentHU() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.hu_fragment_new_bundling, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();

        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlReceive = (RelativeLayout) rootView.findViewById(R.id.rlReceive);
        rlPrint = (RelativeLayout) rootView.findViewById(R.id.rlPrint);

        lblStoreRefNo = (TextView) rootView.findViewById(R.id.lblStoreRefNo);
        lblInboundQty = (TextView) rootView.findViewById(R.id.lblInboundQty);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblDesc = (TextView) rootView.findViewById(R.id.lblDesc);
        lblBundle = (TextView) rootView.findViewById(R.id.lblBundle);

        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);

        etRSN = (CustomEditText) rootView.findViewById(R.id.etRSN);
        etLength = (CustomEditText) rootView.findViewById(R.id.etLength);
        etBreadth = (CustomEditText) rootView.findViewById(R.id.etBreadth);
        etHeight = (CustomEditText) rootView.findViewById(R.id.etHeight);
        etWeight = (CustomEditText) rootView.findViewById(R.id.etWeight);
        etVolume = (CustomEditText) rootView.findViewById(R.id.etVolume);

        etStackCount = (EditText) rootView.findViewById(R.id.etStackCount);
        etPrintQty = (EditText) rootView.findViewById(R.id.etPrintQty);
        etPrinterIP = (EditText) rootView.findViewById(R.id.etPrinterIP);

        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnConfirmLBH = (Button) rootView.findViewById(R.id.btnConfirmLBH);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        SharedPreferences spPrinterIP = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
        ipAddress = spPrinterIP.getString("printerIP", "");

        btnClose.setOnClickListener(this);
        btnConfirmLBH.setOnClickListener(this);

        btnConfirmLBH.setTextColor(getResources().getColor(R.color.black));
        btnConfirmLBH.setBackgroundResource(R.drawable.button_hide);

        sloc = new ArrayList<>();

        if (getArguments() != null) {

            lblStoreRefNo.setText(getArguments().getString("StoreRefNo"));
            clientId = getArguments().getString("ClientId");
            InboundId = getArguments().getString("InboundId");

            pallet = getArguments().getString("pallet");
            location = getArguments().getString("location");
            rsn = getArguments().getString("rsn");
            L = getArguments().getString("L");
            B = getArguments().getString("B");
            H = getArguments().getString("H");
            vol = getArguments().getString("V");
            W = getArguments().getString("W");
            box = getArguments().getString("box");
            qty = getArguments().getString("qty");
            twt = getArguments().getString("twt");
            caseString = getArguments().getString("caseString");
            sku = getArguments().getString("sku");
            desc = getArguments().getString("desc");
            count = getArguments().getString("count");

            etRSN.setText(rsn);
            etLength.setText(L);
            etBreadth.setText(B);
            etHeight.setText(H);
            etWeight.setText(W);
            etVolume.setText(vol);

            lblScannedSku.setText(sku);
            lblDesc.setText(desc);
            lblInboundQty.setText(count);

            if (!etRSN.getText().toString().isEmpty()) {

                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanSku.setImageResource(R.drawable.check);
            }

        }

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();
        oInboundDataDTO = new InboundDTO();
        soundUtils = new SoundUtils();

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

        Common.setIsPopupActive(false);
        getBundleNumber();

    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnClose:
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnPrint:
                if (!lblStoreRefNo.getText().toString().isEmpty()) {
                    etPrinterIP.setText(ipAddress);
                    rlReceive.setVisibility(View.GONE);
                    rlPrint.setVisibility(View.VISIBLE);
                }

                break;
            case R.id.btnConfirmLBH:
                UpdateLBH();
                break;
            case R.id.btnPrintBarcode:
                break;

            case R.id.btnClosePrint:
                rlPrint.setVisibility(View.GONE);
                rlReceive.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }

    public void CloseBundle() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setBundleRSN(lblBundle.getText().toString());
            inboundDTO.setIpAddress(ipAddress);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.CloseBundle(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrinteMatressBundle_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lCore = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lCore = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();



                                ExecutionResponseDTO oExecutionResponseDto = null;
                                for (int i = 0; i < _lCore.size(); i++) {

                                    oExecutionResponseDto = new ExecutionResponseDTO(_lCore.get(i).entrySet());


                                }

                                if(oExecutionResponseDto.getMessage().equalsIgnoreCase("Success")){

                                    etRSN.setText("");
                                    lblDesc.setText("");
                                   // lblBatch.setText("");
                                   // lblQty.setText("");
                                    lblScannedSku.setText("");

                                    lblBundle.setText("");


                                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                    ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                    common.showUserDefinedAlertType("Bundle number closed successfully",getActivity(),getActivity(),"Success");

                                                /*rlBundle.setVisibility(View.GONE);
                                                rlPrint.setVisibility(View.VISIBLE);*/

                                    etBundlePrint.setText(lblBundle.getText().toString());

                                }else {
                                    common.showUserDefinedAlertType(oExecutionResponseDto.getMessage(),getActivity(),getActivity(),"Error");
                                }
                                ProgressDialogUtils.closeProgressDialog();


                                // common.showUserDefinedAlertType(errorMessages.EMC_0049, getActivity(), getContext(), "Success");
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrinteMatressBundle_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrinteMatressBundle_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "PrinteMatressBundle_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }



    public void ClearFields() {


        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);

        etVolume.setText("");
        etBreadth.setText("");
        etHeight.setText("");
        etLength.setText("");
        etWeight.setText("");
        etRSN.setText("");


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
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
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
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Enable bad read response
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }

    }

    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (!ProgressDialogUtils.isProgressActive()) {

            if (scannedData != null && !Common.isPopupActive()) {

                // Checking For RSN Scan
                if (ScanValidator.IsRSNScanned(scannedData)) {

                    if (btnConfirmLBH.isEnabled()) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0008, getActivity(), getContext(), "Warning");
                        btnConfirmLBH.setTextColor(getResources().getColor(R.color.white));
                        btnConfirmLBH.setBackgroundResource(R.drawable.button_shape);
                        etWeight.setEnabled(true);
                        etLength.setEnabled(true);
                        etHeight.setEnabled(true);
                        etBreadth.setEnabled(true);
                        return;
                    } else {
                        etRSN.setText(scannedData);
                        //UniqueRSNMappingWithbundle();
                        return;
                    }
                }
            }

            if(ScanValidator.IsBatchRSN(scannedData)){
                PrintEcomLabelsForFurniture(scannedData);
                return;
            }else{
                soundUtils.alertWarning(getActivity(), getContext());
            }



        } else {

            if (!common.isPopupActive()) {
                common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

            }
            soundUtils.alertWarning(getActivity(), getContext());
        }


    }


    public void getBundleNumber() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetBundleNumber(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenInboundList", getActivity());
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

                            if (core != null) {

                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                    }
                                    lblBundle.setText("");
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                                } else {

                                    core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                    List<LinkedTreeMap<?, ?>> _lst = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lst = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    ExecutionResponseDTO dto = null;
                                    for (int i = 0; i < _lst.size(); i++) {
                                        dto = new ExecutionResponseDTO(_lst.get(i).entrySet());
                                    }


                                    if (dto.getMessage() != null && !dto.getMessage().isEmpty()) {
                                        lblBundle.setText(dto.getMessage());
                                    }else {
                                        lblBundle.setText("");
                                    }

                                    ProgressDialogUtils.closeProgressDialog();
                                }
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenInboundList", getActivity());
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
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenInboundList", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenInboundList", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void UniqueRSNMappingWithbundle() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setBundleRSN(lblBundle.getText().toString());
            inboundDTO.setUserId(userId);
            inboundDTO.setUniqueRSN(lblScannedSku.getText().toString());
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.UniqueRSNMappingWithbundle(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureMatressBundlePacking_01", getActivity());
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
                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSku.setImageResource(R.drawable.invalid_cross);
                                lblScannedSku.setText("");
                                ProgressDialogUtils.closeProgressDialog();

                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                List<LinkedTreeMap<?, ?>> _lCore = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lCore = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                VNALoadingDTO vnaLoadingDTO = null;
                                for (int i = 0; i < _lCore.size(); i++) {

                                    vnaLoadingDTO = new VNALoadingDTO(_lCore.get(i).entrySet());

                                }
                                if (vnaLoadingDTO != null) {

                                    if (vnaLoadingDTO.getResult().equalsIgnoreCase("Success")) {
                                        etRSN.setText(vnaLoadingDTO.getMcode());
                                        lblDesc.setText(vnaLoadingDTO.getMDescreiption());
                                        //lblBatch.setText(vnaLoadingDTO.getBatchNo());
                                        //lblQty.setText(vnaLoadingDTO.getQty());

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.check);

                                    } else {
                                        etRSN.setText("");
                                        lblDesc.setText("");
                                        //lblBatch.setText("");
                                       // lblQty.setText("");
                                        lblScannedSku.setText("");

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.invalid_cross);

                                        common.showUserDefinedAlertType(vnaLoadingDTO.getResult(),getActivity(),getActivity(),"Error");


                                    }

                                }
                                ProgressDialogUtils.closeProgressDialog();

                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureMatressBundlePacking_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureMatressBundlePacking_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "CaptureMatressBundlePacking_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void UpdateLBH() {

        try {

            if (etLength.getText().toString().equals("") || etLength.getText().toString().equals("0")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0010, getActivity(), getContext(), "Error");
                // txtLength.BackColor = Color.Yellow;
                return;
            }
            if (etBreadth.getText().toString().equals("") || etBreadth.getText().toString().equals("0")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0011, getActivity(), getContext(), "Error");
                // txtBreadth.BackColor = Color.Yellow;
                return;
            }
            if (etHeight.getText().toString().equals("") || etHeight.getText().toString().equals("0")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0012, getActivity(), getContext(), "Error");
                // txtHeight.BackColor = Color.Yellow;
                return;
            }
            if (etWeight.getText().toString().equals("") || etWeight.getText().toString().equals("0")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0013, getActivity(), getContext(), "Error");
                // txtWeight.BackColor = Color.Yellow;
                return;
            }
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            List<DimensionsDTO> lstdimensions = new ArrayList<>();
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setClientID(clientId);
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            DimensionsDTO odimensions = new DimensionsDTO();
            odimensions.setLength(etLength.getText().toString());
            odimensions.setBreadth(etBreadth.getText().toString());
            odimensions.setHeight(etHeight.getText().toString());
            odimensions.setWeight(etWeight.getText().toString());
            lstdimensions.add(odimensions);
            inboundDTO.setDimensionsDTO(lstdimensions);
            inboundDTO.setUniqueRSN(etRSN.getText().toString());
            inboundDTO.setInboundID(oInboundDataDTO.getInboundID());
            //inboundDTO.setHUNumber(etBox.getText().toString().split("[/]")[0]);
            //inboundDTO.setHUsize(etBox.getText().toString().split("[/]")[1]);
            inboundDTO.setBoxQuantity(oInboundDataDTO.getBoxQuantity());
            inboundDTO.setMaterialMasterId(oInboundDataDTO.getMaterialMasterId());
            inboundDTO.setMaterialType(materialType);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.UpdateLBH(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateLBH_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lDimensions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lDimensions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<InboundDTO> lstDto = new ArrayList<>();

                                if (_lDimensions != null) {
                                    InboundDTO oInboundDTO = null;
                                    for (int i = 0; i < _lDimensions.size(); i++) {

                                        oInboundDTO = new InboundDTO(_lDimensions.get(i).entrySet());
                                        lstDto.add(oInboundDTO);

                                    }

                                    common.showUserDefinedAlertType(errorMessages.EMC_0014, getActivity(), getContext(), "Success");
                                    ProgressDialogUtils.closeProgressDialog();
                                    etLength.setEnabled(false);
                                    etBreadth.setEnabled(false);
                                    etHeight.setEnabled(false);
                                    etWeight.setEnabled(false);
                                    btnConfirmLBH.setEnabled(false);
                                    btnConfirmLBH.setTextColor(getResources().getColor(R.color.black));
                                    btnConfirmLBH.setBackgroundResource(R.drawable.button_hide);
                                    //ValidateWeightAndVolume(oInboundDataDTO);

                                }

                            }


                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateLBH_02", getActivity());
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
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateLBH_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "UpdateLBH_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    //Clearing UI Elements
    private void ClearUI() {
        lblScannedSku.setText("");
        lblDesc.setText("");
        etLength.setText("");
        etBreadth.setText("");
        etHeight.setText("");
        etWeight.setText("");
        etRSN.setText("");
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
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

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
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_rsngooods));
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void PrintEcomLabelsForFurniture(String scannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setSKU(scannedData.split("[,]")[0]);
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            inboundDTO.setIpAddress(ipAddress);
            if(scannedData.split("[,]")[2].split("[.]").length==3){
                inboundDTO.setBoxQuantity("1");
            }else{
                inboundDTO.setBoxQuantity(scannedData.split("[,]")[2]);
            }
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.PrintEcomLabelsForFurniture(message);
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

                                List<LinkedTreeMap<?, ?>> _lInbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InboundDTO inboundDTO1=null;
                                for(int i=0;i<_lInbound.size();i++){
                                    inboundDTO1=new InboundDTO(_lInbound.get(i).entrySet());
                                }

                                if(inboundDTO1.getResult().equals("Success")){
                                    // TODO Nothing
                                }else{
                                    common.showUserDefinedAlertType("Print Failed", getActivity(), getContext(), "Error");
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




}