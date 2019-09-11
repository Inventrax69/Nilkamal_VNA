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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cipherlab.barcode.GeneralString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.pojos.WMSExceptionMessage;
import com.inventrax.nilkamal_vna.services.RestService;
import com.inventrax.nilkamal_vna.util.ExceptionLoggerUtils;
import com.inventrax.nilkamal_vna.util.FragmentUtils;
import com.inventrax.nilkamal_vna.util.ProgressDialogUtils;
import com.inventrax.nilkamal_vna.util.ScanValidator;
import com.inventrax.nilkamal_vna.util.SoundUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PutAwayFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_PUTAWAY_HU";
    private View rootView;
    private CardView cvScanFromLocation,cvScanPallet,cvScanToLocation;

    private ImageView ivScanFromLocation, ivScanPallet, ivScanToLocation;
    private TextInputLayout txtInputLayoutSourceBin, txtInputLayoutPallet, txtInputLayoutCount, txtInputLayoutQty,
            txtInputLayoutSourcePallet, txtInputLayoutDestPallet, txtInputLayoutCountBinMap, txtInputLayoutDestBin,
            txtInputLayoutOldRsn, txtInputLayoutNewRsn, txtInputLayoutQtyPrint, txtInputLayoutPrinterIP;
    private EditText etFromLocation,etPallet,etToLocation;
    private Button btnClear, btnSkip,btnTranfer,btnCloseLoadPallet;

    private Common common = null;
    SoundUtils soundUtils = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    private String _oldRSNNumber = null;
    private double _availableSetQty = 0;
    private double _partialDispatchQty = 0;
    private String userId = null, stRefNo = null, palletType = null, materialType = null;
    private Boolean IsFromLocationScanned = false, IsFromPalletScanned = false, IsRSNScanned = false, IsEANScanned = false, IsValidLocationorPallet = false, IsPalletScanned = false, Isscannedpalletitem = false, IsToPalletScanned = false;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    public Bundle bundle;
    boolean isPalletScanned,isFromLocationScanned,isToLocationScanned;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public PutAwayFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_putaway, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {

        isPalletScanned=false;
        isFromLocationScanned=false;
        isToLocationScanned=false;

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
        btnTranfer=(Button)rootView.findViewById(R.id.btnTranfer);
        btnCloseLoadPallet=(Button)rootView.findViewById(R.id.btnCloseLoadPallet);


        btnClear.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        btnTranfer.setOnClickListener(this);
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
        soundUtils = new SoundUtils();

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


    }

    public void clearAllFileds(){
        etFromLocation.setText("");
        etPallet.setText("");
        etToLocation.setText("");
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

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClear:
                clearAllFileds();
                break;
            case R.id.btnSkip:
                if(isFromLocationScanned && isPalletScanned && !isToLocationScanned){
                    Toast.makeText(getActivity(), "Skkiped", Toast.LENGTH_SHORT).show();
                    //TODO after Skiping()
                }else{
                    if(isToLocationScanned){
                        Toast.makeText(getActivity(), "Already Transfered", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity(), "Please scan From Location and Pallet to skip", Toast.LENGTH_SHORT).show();
                    }
                    //TODO after Skiping()
                }
                break;
            case R.id.btnTranfer:
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_putaway));
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