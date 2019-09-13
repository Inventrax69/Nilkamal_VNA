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
import com.inventrax.nilkamal_vna.pojos.InboundDTO;
import com.inventrax.nilkamal_vna.pojos.InventoryDTO;
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.pojos.WMSExceptionMessage;
import com.inventrax.nilkamal_vna.searchableSpinner.SearchableSpinner;
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

public class PickingFragmentHU extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {


    private static final String classCode = "API_FRAG_PUTAWAY";
    private View rootView;

    private RelativeLayout rlStRefSelect, rlLoadPallet;
    private TextView lblStoreRefNo, lblSuggestedLoc, lblPalletConfirm;
    private CardView cvScanPallet, cvScanFromLocation,cvScanToLocation;
    private ImageView ivScanPallet, ivScanFromLocation,ivScanToLocation;
    private TextInputLayout txtInputLayoutPallet, txtInputLayoutLocation;
    private EditText etLocation;
    private SearchableSpinner spinnerSelectStRef;
    private Button btnGo, btnCloseOne, btnCloseTwo,btnSkip,btnClear;

    FragmentUtils fragmentUtils;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    String userId = null;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    String clientId = null;
    ArrayList<String> sloc;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private String materialType = null, PalletType = null, InboundId = null;
    List<InboundDTO> lstInbound = null;
    private String storeRefNo = null;
    private String pallet = null, suggestedLoc = null, loc = null, strRef = null;
    private Boolean isFromPendingPutway;
    public String auditbinLocation = null;
    boolean isPalletScanned,isFromLocationScanned,isToLocationScanned;
    private EditText etFromLocation,etPallet,etToLocation;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public PickingFragmentHU() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.hu_picking, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    public void clearAllFileds(){
/*        etFromLocation.setText("");
        etPallet.setText("");
        etToLocation.setText("");*/
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

    // Form controls
    private void loadFormControls() {

        isPalletScanned=false;
        isFromLocationScanned=false;
        isToLocationScanned=false;

        rlStRefSelect = (RelativeLayout) rootView.findViewById(R.id.rlStRefSelect);
        rlLoadPallet = (RelativeLayout) rootView.findViewById(R.id.rlLoadPallet);

        etFromLocation=(EditText) rootView.findViewById(R.id.etFromLocation);
        etPallet=(EditText) rootView.findViewById(R.id.etPallet);
        etToLocation=(EditText) rootView.findViewById(R.id.etToLocation);

        lblStoreRefNo = (TextView) rootView.findViewById(R.id.lblStoreRefNo);
        lblSuggestedLoc = (TextView) rootView.findViewById(R.id.lblSuggestedLoc);
        lblPalletConfirm = (TextView) rootView.findViewById(R.id.lblPalletConfirm);

        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanFromLocation = (CardView) rootView.findViewById(R.id.cvScanFromLocation);
        cvScanToLocation = (CardView) rootView.findViewById(R.id.cvScanToLocation);

        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanFromLocation = (ImageView) rootView.findViewById(R.id.ivScanFromLocation);
        ivScanToLocation = (ImageView) rootView.findViewById(R.id.ivScanToLocation);

        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutLocation = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLocation);

        etLocation = (EditText) rootView.findViewById(R.id.etLocation);

        spinnerSelectStRef = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectStRef);
        spinnerSelectStRef.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                storeRefNo = spinnerSelectStRef.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnClear=(Button)rootView.findViewById(R.id.btnClear);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);
        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnSkip = (Button) rootView.findViewById(R.id.btnSkip);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        btnCloseOne.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);

        btnGo.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        btnClear.setOnClickListener(this);


        sloc = new ArrayList<>();


        /*lblStoreRefNo.setText(getArguments().getString("Storefno"));
        DefaultSLoc= getArguments().getString("DefaultSLOC");*/

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();


        LoadInbounddetails();
        //LoadPalletType();


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


        if (getArguments() != null) {
            lblStoreRefNo.setText(getArguments().getString("StoreRefNo"));
            clientId = getArguments().getString("ClientId");
            InboundId = getArguments().getString("InboundId");

            pallet = getArguments().getString("pallet");
            loc = getArguments().getString("location");
            strRef = getArguments().getString("stRef");
            suggestedLoc = getArguments().getString("suggestedLoc");
            isFromPendingPutway = getArguments().getBoolean("isFromPendingPutaway");

            if (isFromPendingPutway) {
                rlLoadPallet.setVisibility(View.VISIBLE);
                rlStRefSelect.setVisibility(View.GONE);
            }


            etLocation.setText(loc);
            lblStoreRefNo.setText(strRef);
            storeRefNo = lblStoreRefNo.getText().toString();
            lblSuggestedLoc.setText(suggestedLoc);

/*            if (!etPallet.getText().toString().isEmpty()) {

                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanPallet.setImageResource(R.drawable.check);
            }*/

            if (!etLocation.getText().toString().isEmpty()) {
                cvScanFromLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanFromLocation.setImageResource(R.drawable.check);
            }
        }
    }


    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClear:
                clearAllFileds();
                break;
            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnConfirm:
                //ConfirmBinPosting();
                if(isPalletScanned && isFromLocationScanned && isToLocationScanned){
                    Toast.makeText(getActivity(), "You can Complete putaway", Toast.LENGTH_SHORT).show();
                    // TODO putaway completed ()
                }else{
                    if(!isFromLocationScanned){
                        common.showUserDefinedAlertType(errorMessages.EMC_083, getActivity(), getContext(), "Error");
                        return;
                    }
                    if(!isPalletScanned){
                        common.showUserDefinedAlertType(errorMessages.EMC_0017, getActivity(), getContext(), "Error");
                        return;
                    }
                    if(!isToLocationScanned){
                        common.showUserDefinedAlertType(errorMessages.EMC_084, getActivity(), getContext(), "Error");
                        return;
                    }
                }
                break;
            case R.id.btnGo:
                lblStoreRefNo.setText(storeRefNo);
                CheckInboundRefNumber();
                break;
            case R.id.btnSkip:
                if(isFromLocationScanned && !isPalletScanned && !isToLocationScanned){
                    Toast.makeText(getActivity(), "Skkiped", Toast.LENGTH_SHORT).show();
                    //TODO after Skiping()
                }else{
                    if(isToLocationScanned && isPalletScanned){
                        Toast.makeText(getActivity(), "Already Transfered", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity(), "Please scan From Location and Pallet to skip", Toast.LENGTH_SHORT).show();
                    }
                    //TODO after Skiping()
                }
                break;

            default:
                break;
        }
    }

    ///load putway
    public void LoadInbounddetails() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setMaterialType(materialType);
            inboundDTO.setIsSiteToSiteInward("0");
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetOpenInboundList(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenInboundList_01", getActivity());
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

                                List<InboundDTO> lstDto = new ArrayList<InboundDTO>();
                                List<String> lstInboundNo = new ArrayList<>();


                                for (int i = 0; i < _lInbound.size(); i++) {
                                    InboundDTO dto = new InboundDTO(_lInbound.get(i).entrySet());
                                    lstDto.add(dto);
                                    lstInbound = lstDto;
                                }

                                for (int i = 0; i < lstDto.size(); i++) {
                                    lstInboundNo.add(lstDto.get(i).getStoreRefNo());
                                }

                                ArrayAdapter arrayAdapterStoreRefNo = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstInboundNo);
                                spinnerSelectStRef.setAdapter(arrayAdapterStoreRefNo);
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenInboundList_02", getActivity());
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
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenInboundList_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenInboundList_04", getActivity());
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

    private void CheckInboundRefNumber() {
        rlStRefSelect.setVisibility(View.GONE);
        rlLoadPallet.setVisibility(View.VISIBLE);
/*        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setStoreRefNo(storeRefNo);
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.CheckInboundRefNumber(message);
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

                                InboundDTO dto = null;
                                for (int i = 0; i < _lInbound.size(); i++) {
                                    dto = new InboundDTO(_lInbound.get(i).entrySet());
                                }
                                //    dto.getLocation()
                                if (dto.getValidStorefno()) {
                                    ProgressDialogUtils.closeProgressDialog();
                                    rlStRefSelect.setVisibility(View.GONE);
                                    rlPutaway.setVisibility(View.VISIBLE);
                                }
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }*/

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
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) { }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) { }


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

                        // TODO isValidPallet check palette function
                    }else{
                        common.showUserDefinedAlertType(errorMessages.EMC_083, getActivity(), getContext(), "Error");
                    }

                    return;
                }


                //Location Criteria verification
                if (ScanValidator.IsLocationScanned(scannedData)) {
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
                        // TODO isFromLocation check from location function
                    }else{
                        if(isPalletScanned){
                            if(etToLocation.getText().toString().equals(scannedData)){
                                cvScanToLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanToLocation.setImageResource(R.drawable.check);
                                isToLocationScanned=true;
                            }else{
                                cvScanToLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanToLocation.setImageResource(R.drawable.warning_img);
                                isToLocationScanned=false;
                                common.showUserDefinedAlertType(errorMessages.EMC_086, getActivity(), getContext(), "Error");
                            }

                            // TODO isToLocation check to location function
                        }else{
                            common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_picking));
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