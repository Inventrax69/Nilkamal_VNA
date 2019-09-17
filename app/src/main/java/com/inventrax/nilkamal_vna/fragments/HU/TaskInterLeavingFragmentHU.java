package com.inventrax.nilkamal_vna.fragments.HU;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import com.inventrax.nilkamal_vna.pojos.InboundDTO;
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.pojos.WMSExceptionMessage;
import com.inventrax.nilkamal_vna.services.RestService;
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

public class TaskInterLeavingFragmentHU extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_TASK_INTER_LEAVING";
    private View rootView;
    private CardView cvScanFromLocation,cvScanPallet,cvScanToLocation;
    private ImageView ivScanFromLocation,ivScanPallet, ivScanToLocation;
    private EditText etFromLocation,etPallet,etToLocation;
    Button btnClear, btnSkip,btnCloseLoadPallet;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    private String userId = null, stRefNo = null, palletType = null, materialType = null;
    private Boolean IsFromLocationScanned = false, IsFromPalletScanned = false, IsRSNScanned = false, IsEANScanned = false, IsValidLocationorPallet = false, IsPalletScanned = false, Isscannedpalletitem = false, IsToPalletScanned = false;

    // For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    public Bundle bundle;
    boolean isPalletScanned,isFromLocationScanned,isToLocationScanned;
    boolean isPicking,isPutaway;
    TextView tvStRef;
    public String SuggestionType;
    public String inOutId="1";

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public TaskInterLeavingFragmentHU() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_task_inter_leaving, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        isPalletScanned=false;
        isFromLocationScanned=false;
        isToLocationScanned=false;

        tvStRef=(TextView)rootView.findViewById(R.id.tvStRef);
        cvScanFromLocation=(CardView)rootView.findViewById(R.id.cvScanFromLocation);
        cvScanPallet=(CardView)rootView.findViewById(R.id.cvScanPallet);
        cvScanToLocation=(CardView)rootView.findViewById(R.id.cvScanToLocation);

        ivScanFromLocation=(ImageView)rootView.findViewById(R.id.ivScanFromLocation);
        ivScanPallet=(ImageView)rootView.findViewById(R.id.ivScanPallet);
        ivScanToLocation=(ImageView)rootView.findViewById(R.id.ivScanToLocation);

        etFromLocation=(EditText) rootView.findViewById(R.id.etFromLocation);
        etPallet=(EditText) rootView.findViewById(R.id.etPallet);
        etToLocation=(EditText) rootView.findViewById(R.id.etToLocation);

        btnClear=(Button)rootView.findViewById(R.id.btnClear);
        btnSkip=(Button)rootView.findViewById(R.id.btnSkip);
        btnCloseLoadPallet=(Button)rootView.findViewById(R.id.btnCloseLoadPallet);

        btnClear.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        btnCloseLoadPallet.setOnClickListener(this);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");
        SharedPreferences spPrinterIP = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);


        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();

        bundle = new Bundle();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        // For Honey well
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

        radioButtonClicks();



    }

    private void radioButtonClicks() {
        ((RadioButton)rootView.findViewById(R.id.radioAuto)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllFileds1();
                SuggestionType="1";
                inOutId="1";
                setSuggestionTypeApi(SuggestionType);

/*                if(!inOutId.equals("1")){

                    clearAllFileds1();
                }else{
                    isPutaway=true;
                    isPicking=false;
                    clearAllFileds1();
                }*/

            }
        });
        ((RadioButton)rootView.findViewById(R.id.radioAuto)).performClick();
        ((RadioButton)rootView.findViewById(R.id.radioPicking)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SuggestionType="3";
                inOutId="2";
                clearAllFileds1();
                setSuggestionTypeApi(SuggestionType);

            }
        });
        ((RadioButton)rootView.findViewById(R.id.radioPutaway)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SuggestionType="2";
                inOutId="1";
                clearAllFileds1();
                isPutaway=true;
                isPicking=false;
                tvStRef.setText("Put Away");

            }
        });
    }

    public void setSuggestionTypeApi(String suggestionType){

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setSuggestionType(suggestionType);
            inboundDTO.setInoutId(inOutId);
            message.setEntityObject(inboundDTO);

            Log.v("ABCDE_OpertionType",new Gson().toJson(message));

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.VNASuggestion(message);
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
                                List<LinkedTreeMap<?, ?>> _lInbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InboundDTO dto=null;
                                for (int i = 0; i < _lInbound.size(); i++) {
                                    dto = new InboundDTO(_lInbound.get(i).entrySet());
                                }

                                if(dto.getInout()!=null){

                                    if(SuggestionType.equals("1")){

                                        if(dto.getInoutId().equals("1")){
                                            isPicking=false;
                                            isPutaway=true;
                                            inOutId="1";
                                        }else{
                                            isPicking=true;
                                            isPutaway=false;
                                            inOutId="2";
                                            etFromLocation.setText(dto.getPickedLocation());
                                            etPallet.setText(dto.getPalletNo());
                                            etToLocation.setText(dto.getSuggestedLocation());
                                        }

                                    }else if(SuggestionType.equals("2")){

                                        isPicking=false;
                                        isPutaway=true;

                                    }else{
                                        isPicking=true;
                                        isPutaway=false;
                                        etFromLocation.setText(dto.getPickedLocation());
                                        etPallet.setText(dto.getPalletNo());
                                        etToLocation.setText(dto.getSuggestedLocation());


                                    }

/*                                    if(dto.getInoutId().equals("1")){
                                        isPicking=false;
                                        isPutaway=true;
                                        if(inOutId.equals("2")){
                                            etFromLocation.setText(dto.getPickedLocation());
                                            etPallet.setText(dto.getPalletNo());
                                            etToLocation.setText(dto.getSuggestedLocation());
                                        }
                                    }else{
                                        if(inOutId.equals("2")){
                                            etFromLocation.setText(dto.getPickedLocation());
                                            etPallet.setText(dto.getPalletNo());
                                            etToLocation.setText(dto.getSuggestedLocation());
                                        }
                                        if(dto.getInout().equals("PUTWAY"))
                                        {
                                            isPicking=false;
                                            isPutaway=true;
                                        }else{
                                            isPicking=true;
                                            isPutaway=false;
                                        }

                                    }*/

                                    if(isPicking)
                                        tvStRef.setText("Picking");
                                    else
                                        tvStRef.setText("Put Away");
                                }else{
                                    clearAllFileds1();
                                    common.showUserDefinedAlertType(errorMessages.EMC_089, getActivity(), getContext(), "Error");
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

    private void UpsertBintoBinTransfer(String scannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setLocation(etFromLocation.getText().toString());
            inboundDTO.setPalletNo(etPallet.getText().toString());
            inboundDTO.setToLocation(etToLocation.getText().toString());
            inboundDTO.setPutwayType("0");
            inboundDTO.setInoutId(inOutId);
            inboundDTO.setSuggestionType(SuggestionType);
            if(isPicking)
            inboundDTO.setInout("2");
            else
            inboundDTO.setInout("1");
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.UpsertBintoBinTransfer(message);
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
                                List<LinkedTreeMap<?, ?>> _lInbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                InboundDTO dto=null;
                                for (int i = 0; i < _lInbound.size(); i++) {
                                    dto = new InboundDTO(_lInbound.get(i).entrySet());
                                }

                                if(dto.getResult().equals("Successfully Transfer")){
                                    cvScanToLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanToLocation.setImageResource(R.drawable.check);
                                    isToLocationScanned=true;
                                    inOutId=dto.getInoutId();
                                    Common.setIsPopupActive(true);
                                    sound.alertSuccess(getActivity(), getContext());
                                    DialogUtils.showAlertDialog(getActivity(), "Success", "Successfully Transfer", R.drawable.success,new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    Common.setIsPopupActive(false);
                                                    setSuggestionTypeApi(SuggestionType);
                                                    clearAllFileds();
                                                    break;
                                            }
                                        }
                                    });
                                }else{
                                    common.showUserDefinedAlertType(dto.getResult(), getActivity(), getContext(), "Error");
                                }

                                ProgressDialogUtils.closeProgressDialog();
                                //Successfully Transfer

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

    private void CheckPalletandLocationValidation(final String scannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setMaterialType(materialType);
            inboundDTO.setIsSiteToSiteInward("0");
            inboundDTO.setLocation(etFromLocation.getText().toString());
            inboundDTO.setPalletNo(etPallet.getText().toString());
            message.setEntityObject(inboundDTO);



            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.CheckPalletandLocationValidation(message);
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
                                List<LinkedTreeMap<?, ?>> _lInbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InboundDTO dto = null;
                                for (int i = 0; i < _lInbound.size(); i++) {
                                    dto = new InboundDTO(_lInbound.get(i).entrySet());
                                }

                                if(dto.getResult().equals("Valid Pallet")){
                                    etToLocation.setText(dto.getToLocation());
                                    etPallet.setText(scannedData);
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);
                                    isPalletScanned=true;
                                    ProgressDialogUtils.closeProgressDialog();
                                }else{
                                    common.showUserDefinedAlertType(dto.getResult(), getActivity(), getContext(), "Error");
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

    public void clearAllFileds(){
        isPalletScanned=false;
        isFromLocationScanned=false;
        isToLocationScanned=false;
        cvScanFromLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanFromLocation.setImageResource(R.drawable.fullscreen_img);
        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);
        cvScanToLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanToLocation.setImageResource(R.drawable.fullscreen_img);
    }

    public void clearAllFileds1(){
        isPalletScanned=false;
        isFromLocationScanned=false;
        isToLocationScanned=false;
        cvScanFromLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanFromLocation.setImageResource(R.drawable.fullscreen_img);
        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);
        cvScanToLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanToLocation.setImageResource(R.drawable.fullscreen_img);
        etToLocation.setText("");
        etFromLocation.setText("");
        etPallet.setText("");
        tvStRef.setText("");

    }

    private void GetVNAPutawaySuggestion(final String scannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setLocation(etFromLocation.getText().toString());
            inboundDTO.setPalletNo(scannedData);
            inboundDTO.setInoutId("1");
            inboundDTO.setIsSiteToSiteInward("0");
            message.setEntityObject(inboundDTO);


            Log.v("ABCDE_R",new Gson().toJson(message));
            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.GetVNAPutawaySuggestion(message);
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
                                List<LinkedTreeMap<?, ?>> _lInbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                Log.v("ABCDE_R",new Gson().toJson(_lInbound));
                                InboundDTO dto = null;
                                for (int i = 0; i < _lInbound.size(); i++) {
                                    dto = new InboundDTO(_lInbound.get(i).entrySet());
                                }

                                if(dto.getResult()!=null){
                                    if(!dto.getResult().equals("-1")){
                                        etToLocation.setText(dto.getToLocation());
                                        etPallet.setText(scannedData);
                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPallet.setImageResource(R.drawable.check);
                                        isPalletScanned=true;
                                    }else{
                                        common.showUserDefinedAlertType("Invaild Location or pallet", getActivity(), getContext(), "Error");
                                    }
                                }else{
                                    common.showUserDefinedAlertType("Invaild Location or pallet", getActivity(), getContext(), "Error");
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
    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClear:
                    clearAllFileds1();
                break;
            case R.id.btnSkip:
                if(isPicking){
                    if(isFromLocationScanned && !isPalletScanned && !isToLocationScanned){
                        Toast.makeText(getActivity(), "Skkiped", Toast.LENGTH_SHORT).show();
                        //TODO after Skipping()
                    }else{
                        if(isToLocationScanned && isPalletScanned){
                            Toast.makeText(getActivity(), "Already Transfered", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(), "Please scan From Location and Pallet to skip", Toast.LENGTH_SHORT).show();
                        }
                        //TODO setError messages
                    }
                }else{
                    if(isFromLocationScanned && isPalletScanned && !isToLocationScanned){
                        Toast.makeText(getActivity(), "Skkiped", Toast.LENGTH_SHORT).show();
                        //TODO after Skipping()
                    }else{
                        if(isToLocationScanned){
                            Toast.makeText(getActivity(), "Already Transfered", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(), "Please scan From Location and Pallet to skip", Toast.LENGTH_SHORT).show();
                        }
                        //TODO setError messages
                    }
                }
                break;
            case R.id.btnCloseLoadPallet:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
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

            if (!ProgressDialogUtils.isProgressActive()) {

                if (ScanValidator.IsPalletScanned(scannedData)) {
                    if(isPicking){
                        if(isFromLocationScanned){
                            if(etPallet.getText().toString().equals(scannedData)){
                                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanPallet.setImageResource(R.drawable.check);
                                isPalletScanned=true;
                            }else{
                                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanPallet.setImageResource(R.drawable.warning_img);
                                isPalletScanned=false;
                                common.showUserDefinedAlertType(errorMessages.EMC_087, getActivity(), getContext(), "Error");
                            }
                        }else{
                            common.showUserDefinedAlertType(errorMessages.EMC_083, getActivity(), getContext(), "Error");
                        }
                    }else{
                        if(isFromLocationScanned){
                            //etPallet.setText(scannedData);
                            //CheckPalletandLocationValidation(scannedData);
                            GetVNAPutawaySuggestion(scannedData);
                        }else{
                            common.showUserDefinedAlertType(errorMessages.EMC_083, getActivity(), getContext(), "Error");
                        }
                    }

                    return;
                }

                //Location Criteria verification
                if (ScanValidator.IsLocationScanned(scannedData)) {
                    if(isPicking){
                        if(!isFromLocationScanned){
                            if(etFromLocation.getText().toString().equals(scannedData)){
                                cvScanFromLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanFromLocation.setImageResource(R.drawable.check);
                                isFromLocationScanned=true;

                            }else{
                                cvScanFromLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanFromLocation.setImageResource(R.drawable.warning_img);
                                isFromLocationScanned=false;
                                common.showUserDefinedAlertType(errorMessages.EMC_085, getActivity(), getContext(), "Error");
                            }
                        }else{
                            if(isPalletScanned){
                                if(etToLocation.getText().toString().equals(scannedData)){
                                    UpsertBintoBinTransfer(scannedData);
                                }else{
                                    cvScanToLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanToLocation.setImageResource(R.drawable.warning_img);
                                    isToLocationScanned=false;
                                    common.showUserDefinedAlertType(errorMessages.EMC_086, getActivity(), getContext(), "Error");
                                }

                            }else{
                                common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                            }
                        }
                    }else{
                        if(!isFromLocationScanned){
                            cvScanFromLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanFromLocation.setImageResource(R.drawable.check);
                            isFromLocationScanned=true;
                            etFromLocation.setText(scannedData);
                        }else{
                            if(isPalletScanned){
                                if(etToLocation.getText().toString().equals(scannedData)){

                                    UpsertBintoBinTransfer(scannedData);
                                }else{
                                    common.showUserDefinedAlertType(errorMessages.EMC_086, getActivity(), getContext(), "Error");
                                }
                            }else{
                                common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                            }
                        }
                    }

                    return;
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_task_inter_location));
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

        //storageloc=spinnerSelectSloc.getSelectedItem().toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}