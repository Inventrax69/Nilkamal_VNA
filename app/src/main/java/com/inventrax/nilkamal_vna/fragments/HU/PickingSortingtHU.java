package com.inventrax.nilkamal_vna.fragments.HU;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.inventrax.nilkamal_vna.activities.SettingsActivity;
import com.inventrax.nilkamal_vna.common.Common;
import com.inventrax.nilkamal_vna.common.constants.EndpointConstants;
import com.inventrax.nilkamal_vna.common.constants.ErrorMessages;
import com.inventrax.nilkamal_vna.common.constants.ServiceURL;
import com.inventrax.nilkamal_vna.fragments.HomeFragment;
import com.inventrax.nilkamal_vna.interfaces.ApiInterface;
import com.inventrax.nilkamal_vna.pojos.InboundDTO;
import com.inventrax.nilkamal_vna.pojos.ItemInfoDTO;
import com.inventrax.nilkamal_vna.pojos.LoginUserDTO;
import com.inventrax.nilkamal_vna.pojos.PrinterDetailsDTO;
import com.inventrax.nilkamal_vna.pojos.VLPDRequestDTO;
import com.inventrax.nilkamal_vna.pojos.VLPDResponseDTO;
import com.inventrax.nilkamal_vna.pojos.VlpdDto;
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

public class PickingSortingtHU extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_PICKING_SORTING";
    private View rootView;
    private CardView cvScanPartNo,cvScanPallet,cvScanDockLocation;
    private ImageView ivScanPartNo, ivScanPallet, ivScanDockLocation;
    private EditText etPartNo,etDockLocation;
    Button btnClear, btnSkip,btnCloseLoadPallet,btnGo,btnExport,btnCloseExport;
    private Common common = null;
    SoundUtils soundUtils = null;
    String scanner = null,vlpdId = null;
    String getScanner = null;
    IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    private String userId = null, stRefNo = null, palletType = null, materialType = null;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    public Bundle bundle;
    boolean isPalletScanned,isPartNoScanned,isDockLocationScanned;
    RelativeLayout rlVLPDSelect,rlSorting,rlExport;
    private SearchableSpinner spinnerSelectVLPDNo;
    ItemInfoDTO vlpdItem = null;
    String storageVLPDNo;
    boolean IsSkipItem = false;
    TextView txtVLPDNumber,txtMcode,txtBatchNo,txtDockName,txtPendingQty,txtHuNo,txtHuSize;
    public VlpdDto mVlpdDto;

    public  String sNewUniqueRSN="",sPalletNo="",ipAdress="";

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public PickingSortingtHU() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_picking_sorting, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {


        isPalletScanned=false;
        isPartNoScanned=false;
        isDockLocationScanned=false;

        cvScanPallet=(CardView)rootView.findViewById(R.id.cvScanPallet);
        cvScanPartNo=(CardView)rootView.findViewById(R.id.cvScanPartNo);
        cvScanDockLocation=(CardView)rootView.findViewById(R.id.cvScanDockLocation);

        ivScanPallet=(ImageView)rootView.findViewById(R.id.ivScanPallet);
        ivScanPartNo=(ImageView)rootView.findViewById(R.id.ivScanPartNo);
        ivScanDockLocation=(ImageView)rootView.findViewById(R.id.ivScanDockLocation);

        etPartNo=(EditText) rootView.findViewById(R.id.etPartNo);
        etDockLocation=(EditText) rootView.findViewById(R.id.etDockLocation);

        txtVLPDNumber=(TextView) rootView.findViewById(R.id.txtVLPDNumber);
        txtMcode=(TextView) rootView.findViewById(R.id.txtMcode);
        txtBatchNo=(TextView) rootView.findViewById(R.id.txtBatchNo);
        txtDockName=(TextView) rootView.findViewById(R.id.txtDockName);
        txtPendingQty=(TextView) rootView.findViewById(R.id.txtPendingQty);
        txtHuNo=(TextView) rootView.findViewById(R.id.txtHuNo);
        txtHuSize=(TextView) rootView.findViewById(R.id.txtHuSize);

        rlVLPDSelect=(RelativeLayout) rootView.findViewById(R.id.rlVLPDSelect);
        rlSorting=(RelativeLayout) rootView.findViewById(R.id.rlSorting);
        rlExport=(RelativeLayout) rootView.findViewById(R.id.rlExport);

        spinnerSelectVLPDNo=(SearchableSpinner) rootView.findViewById(R.id.spinnerSelectVLPDNo);

        btnClear=(Button)rootView.findViewById(R.id.btnClear);
        btnSkip=(Button)rootView.findViewById(R.id.btnSkip);
        btnCloseLoadPallet=(Button)rootView.findViewById(R.id.btnCloseLoadPallet);
        btnGo=(Button)rootView.findViewById(R.id.btnGo);
        btnExport=(Button)rootView.findViewById(R.id.btnExport);
        btnCloseExport=(Button)rootView.findViewById(R.id.btnCloseExport);



        rlVLPDSelect.setVisibility(View.VISIBLE);
        rlSorting.setVisibility(View.GONE);
        rlExport.setVisibility(View.GONE);

        btnClear.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        btnCloseLoadPallet.setOnClickListener(this);
        btnGo.setOnClickListener(this);
        btnExport.setOnClickListener(this);
        btnCloseExport.setOnClickListener(this);
        spinnerSelectVLPDNo.setOnItemSelectedListener(this);

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
        soundUtils = new SoundUtils();
        mVlpdDto=new VlpdDto();

        bundle = new Bundle();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);


        GetAllOpenVLPDList();

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

    }

    public void clearAllFileds(){
        etPartNo.setText("");
        etDockLocation.setText("");
        isPalletScanned=false;
        isPartNoScanned=false;
        isDockLocationScanned=false;
        cvScanPartNo.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanPartNo.setImageResource(R.drawable.fullscreen_img);
        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);
        cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanDockLocation.setImageResource(R.drawable.fullscreen_img);
    }

    // To get VLPD Id
    private void GetAllOpenVLPDList() {
        try {

            vlpdId = "";
            List<ItemInfoDTO> lstiteminfo = new ArrayList<>();
            ItemInfoDTO oItem = new ItemInfoDTO();
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            message.setEntityObject(vlpdRequestDTO);
            Log.v("QWERTY",new Gson().toJson(message));

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetOpenVLPDList(message);
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

                                List<LinkedTreeMap<?, ?>> _lVLPD = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVLPD = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                Log.v("QWERTY",new Gson().toJson(_lVLPD));
                                if (_lVLPD.size() > 0) {
                                    List<VLPDResponseDTO> lstDto = new ArrayList<VLPDResponseDTO>();
                                    List<String> lstVLPD = new ArrayList<>();

                                    VLPDResponseDTO dto = null;
                                    for (int i = 0; i < _lVLPD.size(); i++) {
                                        dto = new VLPDResponseDTO(_lVLPD.get(i).entrySet());
                                        lstDto.add(dto);
                                        lstVLPD.add(lstDto.get(i).getVLPDNumber());
                                    }
                                    ProgressDialogUtils.closeProgressDialog();

                                    ArrayAdapter arrayAdapterStoreRefNo = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstVLPD);
                                    spinnerSelectVLPDNo.setAdapter(arrayAdapterStoreRefNo);

                                } else {
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                                    clearAllFileds();
                                    return;
                                }
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDListByPriority_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDListByPriority_03", getActivity());
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

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClear:
                clearAllFileds();
                break;
            case R.id.btnSkip:
                break;
            case R.id.btnCloseExport:
                rlVLPDSelect.setVisibility(View.GONE);
                rlSorting.setVisibility(View.VISIBLE);
                rlExport.setVisibility(View.GONE);
                break;
            case R.id.btnExport:
                // Todo check VLDP# List
                rlVLPDSelect.setVisibility(View.GONE);
                rlSorting.setVisibility(View.GONE);
                rlExport.setVisibility(View.VISIBLE);
                break;
            case R.id.btnGo:
                // TODO select VLDP#
                txtVLPDNumber.setText(storageVLPDNo);
                rlVLPDSelect.setVisibility(View.GONE);
                rlSorting.setVisibility(View.VISIBLE);
                rlExport.setVisibility(View.GONE);
                break;
            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
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
                    GetVNAPickingandShortingList(scannedData);
                    return;
                }

                if (ScanValidator.IsLocationScanned(scannedData)) {
                    if(isPalletScanned){
                        etDockLocation.setText(scannedData);
                        cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanDockLocation.setImageResource(R.drawable.check);
                        isDockLocationScanned=true;
                    }else{
                            common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                    }
                    // TODO isValidDockLocation Check DockLocation Function
                    return;
                }

                if (ScanValidator.IsRSNScanned(scannedData)) {
                    if(isPalletScanned && isDockLocationScanned){
                        PickandCheck(scannedData);
                    }else{
                        if(!isPalletScanned)
                            common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                        else
                            common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Error");
                    }
                    // TODO isValidRSN Check RSN Function
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_picking_sorting));
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

        storageVLPDNo=spinnerSelectVLPDNo.getSelectedItem().toString();
        txtVLPDNumber.setText(storageVLPDNo);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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

    private void GetVNAPickingandShortingList(final String scannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setVLPDNumber(txtVLPDNumber.getText().toString());
            inboundDTO.setPalletNo(scannedData);
            message.setEntityObject(inboundDTO);
            Log.v("QWERTY",new Gson().toJson(message));
            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.GetVNAPickingandShortingList(message);
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
                                List<LinkedTreeMap<?, ?>> _lVlpd = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVlpd= (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                Log.v("QWERTY",new Gson().toJson(_lVlpd));

                                VlpdDto vlpdDto=null;
                                for(int i=0;i<_lVlpd.size();i++){
                                    vlpdDto=new VlpdDto(_lVlpd.get(i).entrySet());
                                }
                                mVlpdDto=vlpdDto;

                                if(vlpdDto.getResult().equals("1")){
                                    txtMcode.setText(vlpdDto.getMcode());
                                    txtBatchNo.setText(vlpdDto.getBatchNo());
                                    txtDockName.setText(vlpdDto.getDockName());
                                    txtHuNo.setText("HUNo: "+vlpdDto.getHUNo());
                                    txtHuSize.setText("HUSize: "+vlpdDto.getHUSize());
                                    txtPendingQty.setText("Qty: "+vlpdDto.getPendingQty());
                                    sPalletNo=scannedData;
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);
                                    isPalletScanned=true;
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

    private void PickandCheck(String scannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setAssignedId(mVlpdDto.getAssignedId());
            vlpdDto.setMcode(mVlpdDto.getMcode());
            vlpdDto.setDescription(mVlpdDto.getDescription());
            vlpdDto.setDockName(mVlpdDto.getDockName());
            vlpdDto.setvLPDNumber(mVlpdDto.getvLPDNumber());
            vlpdDto.setBatchNo(mVlpdDto.getBatchNo());
            vlpdDto.setMfgDate(mVlpdDto.getMfgDate());
            vlpdDto.setExpDate(mVlpdDto.getExpDate());
            vlpdDto.setSKUPendingQty(mVlpdDto.getPendingQty());
            vlpdDto.setHUNumber(mVlpdDto.getHUNo());
            vlpdDto.setDockLocation(etDockLocation.getText().toString());
            vlpdDto.setHUSize(mVlpdDto.getHUSize());
            vlpdDto.setUniqueRSN(scannedData);
            vlpdDto.setNewUniqueRSN(sNewUniqueRSN);
            vlpdDto.setUserId(userId);
            vlpdDto.setPalletNo(sPalletNo);
            vlpdDto.setStorageLocation(mVlpdDto.getStorageLocation());

            message.setEntityObject(vlpdDto);

            Log.v("ABCDE",new Gson().toJson(message));

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.PickandCheck(message);
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
                                List<LinkedTreeMap<?, ?>> _lVlpd = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVlpd = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                VlpdDto vlpdDto1=null;

                                for(int i=0;i<_lVlpd.size();i++){
                                    vlpdDto1=new VlpdDto(_lVlpd.get(i).entrySet());
                                }


                                if(vlpdDto1.getMessage().toString()!=null){
                                    if(vlpdDto1.getMessage().equals("-1")){
                                        // TODO 'Scan New RSN'
                                        getPrinters();
                                    }
                                    if(vlpdDto1.getMessage().equals("-2")){
                                        // TODO 'Invalid RSN'
                                        common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Error");
                                    }
                                    if(vlpdDto1.getMessage().equals("-3")){
                                        // TODO 'Invalid Dock '
                                        common.showUserDefinedAlertType(errorMessages.EMC_091, getActivity(), getContext(), "Error");
                                    }
                                    if(vlpdDto1.getMessage().equals("-4")){
                                        // TODO 'Error : While picking'
                                        common.showUserDefinedAlertType(errorMessages.EMC_092, getActivity(), getContext(), "Error");
                                    }
                                    if(vlpdDto1.getMessage().equals("1")){
                                        // TODO Have to GetVNAPickingandShortingList again
                                        cvScanPartNo.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPartNo.setImageResource(R.drawable.check);
                                        isPartNoScanned=true;
                                    }
                                }else{
                                    // TODO Alret No more items to pick for this pallet
                                    common.showUserDefinedAlertType(errorMessages.EMC_093, getActivity(), getContext(), "Error");
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


    private void PrintNewRSN() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setUserId(userId);
            vlpdDto.setUniqueRSN("1909170003/1/1/070019");
            vlpdDto.setIpAddress(ipAdress);
            vlpdDto.setMcode(mVlpdDto.getMcode());
            vlpdDto.setAssignedId(mVlpdDto.getAssignedId());
            vlpdDto.setPickedQty(mVlpdDto.getPendingQty());
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
                                List<LinkedTreeMap<?, ?>> _lVlpd = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVlpd = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VlpdDto vlpdDto1=null;
                                for(int i=0;i<_lVlpd.size();i++){
                                    vlpdDto1=new VlpdDto(_lVlpd.get(i).entrySet());
                                }

                                if(vlpdDto1.getMessage()!=null){

                                    if(vlpdDto1.getMessage().equals("1")){
                                        pickingSkipdialog.dismiss();
                                        Toast.makeText(getActivity(), "Printer Succesfully", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
                                    }

                                }else{
                                    //TODO NO Message
                                }




                                         pickingSkipdialog.dismiss();
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
    Dialog pickingSkipdialog;
    public void getPrinters() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.LoginUserDTO,  getContext());
            LoginUserDTO oLoginDTO = new LoginUserDTO();
            oLoginDTO.setMailID("1");
            message.setEntityObject(oLoginDTO);


            Log.v("ABCDE_P",new Gson().toJson(message));

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
                                    final SearchableSpinner spinnerSelectPrinter=(SearchableSpinner) pickingSkipdialog.findViewById(R.id.spinnerSelectReason);
                                    ArrayAdapter arrayAdapterSelectPrinter = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstPrinters);
                                    spinnerSelectPrinter.setAdapter(arrayAdapterSelectPrinter);
                                    spinnerSelectPrinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                            ipAdress=spinnerSelectPrinter.getSelectedItem().toString();
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

}