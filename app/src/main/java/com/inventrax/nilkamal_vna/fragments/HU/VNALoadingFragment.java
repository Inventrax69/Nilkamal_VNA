package com.inventrax.nilkamal_vna.fragments.HU;

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
import com.inventrax.nilkamal_vna.interfaces.ApiInterface;
import com.inventrax.nilkamal_vna.pojos.ExecutionResponseDTO;
import com.inventrax.nilkamal_vna.pojos.InboundDTO;
import com.inventrax.nilkamal_vna.pojos.ItemInfoDTO;
import com.inventrax.nilkamal_vna.pojos.OutboundDTO;
import com.inventrax.nilkamal_vna.pojos.VLPDLoadingDTO;
import com.inventrax.nilkamal_vna.pojos.VLPDRequestDTO;
import com.inventrax.nilkamal_vna.pojos.VLPDResponseDTO;
import com.inventrax.nilkamal_vna.pojos.VlpdDto;
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.pojos.WMSExceptionMessage;
import com.inventrax.nilkamal_vna.searchableSpinner.SearchableSpinner;
import com.inventrax.nilkamal_vna.services.RestService;
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

public class VNALoadingFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_VNA_LOADING";
    private View rootView;

    private RelativeLayout rlOBDSelect, rlOBDLoading;
    private CardView cvScanRSN;
    private ImageView ivScanRSN;
    private SearchableSpinner spinnerSelectOBDNo;
    private Button btnSubmit, btnExport, btnGo, btnCloseOne, btnCloseTwo;

    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    private String userId = null, obdRefNo = null;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    SoundUtils sound;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private String materialType = null;
    private ErrorMessages errorMessages;

    TextView txtOBDNumber,txtPenQty;
    String ID=null;

    EditText etmCode,etBatchNo,etHuSize,etHuNo;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public VNALoadingFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.hu_vna_loading, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        rlOBDSelect = (RelativeLayout) rootView.findViewById(R.id.rlOBDSelect);
        rlOBDLoading = (RelativeLayout) rootView.findViewById(R.id.rlOBDLoading);


        txtOBDNumber = (TextView) rootView.findViewById(R.id.txtOBDNumber);
        txtPenQty = (TextView) rootView.findViewById(R.id.txtPenQty);


        etmCode = (EditText) rootView.findViewById(R.id.etmCode);
        etBatchNo = (EditText) rootView.findViewById(R.id.etBatchNo);
        etHuSize = (EditText) rootView.findViewById(R.id.etHuSize);
        etHuNo = (EditText) rootView.findViewById(R.id.etHuNo);

        cvScanRSN = (CardView) rootView.findViewById(R.id.cvScanRSN);
        ivScanRSN = (ImageView) rootView.findViewById(R.id.ivScanRSN);

        spinnerSelectOBDNo = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectOBDNo);
        spinnerSelectOBDNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //obdRefNo= spinnerSelectOBDNo.getSelectedItem().toString();
                obdRefNo= lstVLPD.get(i);
                ID=lstId.get(i);
                txtOBDNumber.setText(obdRefNo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnGo = (Button) rootView.findViewById(R.id.btnGo);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        btnCloseOne.setOnClickListener(this);
        btnGo.setOnClickListener(this);


        
        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();

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

        GetOpenVNALOADLIST();
    }
    

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnGo:
                if(!txtOBDNumber.getText().toString().isEmpty()){
                    rlOBDLoading.setVisibility(View.VISIBLE);
                    rlOBDSelect.setVisibility(View.GONE);
                    getVNAPendingLoadingCount();
                }
                break;
                
            default:
                break;
        }
    }
    
    public void ClearFields() { }
    
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
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
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
            // Apply the settings
            barcodeReader.setProperties(properties);
        }

    }

    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (scannedData != null && !Common.isPopupActive()) {

            if(!ProgressDialogUtils.isProgressActive()) {

                if(ScanValidator.IsRSNScanned(scannedData)){
                    if(txtOBDNumber.getText().toString().isEmpty())
                        VNAuniqueRSNLoading(scannedData);
                }

            }else {
                if(!Common.isPopupActive())
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");

                }
                sound.alertWarning(getActivity(),getContext());

            }
        }
    }

    // sending exception to the database
    public void logException() {

        try {

            String textFromFile = ExceptionLoggerUtils.readFromFile(getActivity());

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
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002", getContext());

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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_vna_loading));
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

    List<String> lstVLPD,lstId;
    // To get VLPD Id
    private void GetOpenVNALOADLIST() {
        try {


            List<ItemInfoDTO> lstiteminfo = new ArrayList<>();
            ItemInfoDTO oItem = new ItemInfoDTO();
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            message.setEntityObject(vlpdRequestDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetOpenVNALOADLIST(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDList_01", getActivity());
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

                                List<LinkedTreeMap<?, ?>> _lOBD = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lOBD = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                if (_lOBD.size() > 0) {
                                    List<VLPDResponseDTO> lstDto = new ArrayList<VLPDResponseDTO>();
                                    lstVLPD = new ArrayList<>();
                                    lstId= new ArrayList<>();

                                    VLPDResponseDTO dto = null;
                                    for (int i = 0; i < _lOBD.size(); i++) {
                                        dto = new VLPDResponseDTO(_lOBD.get(i).entrySet());
                                        lstDto.add(dto);
                                        lstVLPD.add(lstDto.get(i).getVLPDNumber());
                                        lstId.add(lstDto.get(i).getID());
                                    }
                                    ProgressDialogUtils.closeProgressDialog();

                                    ArrayAdapter arrayAdapterStoreRefNo = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstVLPD);
                                    spinnerSelectOBDNo.setAdapter(arrayAdapterStoreRefNo);

                                } else {
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                                    //clearAllFileds(); TODO clear all feilds
                                    return;
                                }
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDListByPriority_02", getActivity());
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
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDListByPriority_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDListByPriority_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    private void getVNAPendingLoadingCount() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setID(ID);
            message.setEntityObject(vlpdRequestDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.getVNAPendingLoadingCount(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
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
                                List<LinkedTreeMap<?, ?>> _lRsnList = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lRsnList = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VLPDResponseDTO vlpdResponseDTO=null;

                                for(int i=0;i<_lRsnList.size();i++){
                                    vlpdResponseDTO=new VLPDResponseDTO(_lRsnList.get(i).entrySet());
                                }

                                if(vlpdResponseDTO.getResult().equals("1")){
                                    txtPenQty.setText(vlpdResponseDTO.getLoadRSNCount()+" / "+vlpdResponseDTO.getPickRSNCount());
                                }
                                else if(vlpdResponseDTO.getResult().equals("-2")){
                                    common.showUserDefinedAlertType(vlpdResponseDTO.getErrorMessage(), getActivity(), getContext(), "Error");
                                }else{
                                    common.showUserDefinedAlertType("NO more items", getActivity(), getContext(), "Error");
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

    private void VNAuniqueRSNLoading(final String scannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO=new VLPDRequestDTO();
            vlpdRequestDTO.setID(ID);
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setRSN(scannedData);
            message.setEntityObject(vlpdRequestDTO);

            Log.v("ABCDE_L",new Gson().toJson(message));

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.VNAuniqueRSNLoading(message);
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
                                List<LinkedTreeMap<?, ?>> _lVLPD = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVLPD = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                Log.v("ABCDE_R",new Gson().toJson(_lVLPD));
                                VlpdDto vlpdDto=null;
                                for(int i=0;i<_lVLPD.size();i++){
                                    vlpdDto=new VlpdDto(_lVLPD.get(i).entrySet());
                                }


                                if(vlpdDto.getResult().equals("1")){

                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanRSN.setImageResource(R.drawable.check);
                                    etmCode.setText(vlpdDto.getMcode());
                                    etBatchNo.setText(vlpdDto.getBatchNo());
                                    etHuSize.setText(vlpdDto.getHUSize());
                                    etHuNo.setText(vlpdDto.getHUNo());
                                    txtPenQty.setText(vlpdDto.getLoadRSN()+" / "+vlpdDto.getPickRSN());
                                }else{
                                    common.showUserDefinedAlertType("No more items to pick", getActivity(), getContext(), "Error");
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

}