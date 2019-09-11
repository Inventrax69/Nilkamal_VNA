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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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
import com.inventrax.nilkamal_vna.common.constants.ErrorMessages;
import com.inventrax.nilkamal_vna.fragments.HomeFragment;
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.util.ExceptionLoggerUtils;
import com.inventrax.nilkamal_vna.util.FragmentUtils;
import com.inventrax.nilkamal_vna.util.ProgressDialogUtils;
import com.inventrax.nilkamal_vna.util.ScanValidator;
import com.inventrax.nilkamal_vna.util.SoundUtils;

import java.util.HashMap;
import java.util.Map;

public class SortingtHU extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {


    private static final String classCode = "API_FRAG_PUTAWAY";
    private View rootView;
    private CardView cvScanPartNo,cvScanPallet,cvScanDockLocation;

    private ImageView ivScanPartNo, ivScanPallet, ivScanDockLocation;
    private TextInputLayout txtInputLayoutSourceBin, txtInputLayoutPallet, txtInputLayoutCount, txtInputLayoutQty,
            txtInputLayoutSourcePallet, txtInputLayoutDestPallet, txtInputLayoutCountBinMap, txtInputLayoutDestBin,
            txtInputLayoutOldRsn, txtInputLayoutNewRsn, txtInputLayoutQtyPrint, txtInputLayoutPrinterIP;
    private EditText etPartNo,etQuantity,etToLocation,etDockLocation;
    private Button btnClear, btnSkip,btnCloseLoadPallet;

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
    boolean isPalletScanned,isPartNoScanned,isDockLocationScanned;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public SortingtHU() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_sorting, container, false);
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
        etQuantity=(EditText) rootView.findViewById(R.id.etQuantity);
        etToLocation=(EditText) rootView.findViewById(R.id.etToLocation);
        etDockLocation=(EditText) rootView.findViewById(R.id.etDockLocation);



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
        etPartNo.setText("");
        etQuantity.setText("");
        etToLocation.setText("");
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


    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClear:
                clearAllFileds();
                break;
            case R.id.btnSkip:
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
                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanPallet.setImageResource(R.drawable.check);
                        isPalletScanned=true;
                        // TODO isValidPallet check palette function
                    return;
                }

                if (ScanValidator.IsRSNScanned(scannedData)) {
                    if(isPalletScanned){
                        cvScanPartNo.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanPartNo.setImageResource(R.drawable.check);
                        isPartNoScanned=true;
                    }else{
                        common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                    }
                    // TODO isValidRSN check RSN function
                    return;
                }

                if (ScanValidator.IsLocationScanned(scannedData)) {
                    if(isPalletScanned && isPartNoScanned){
                        cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanDockLocation.setImageResource(R.drawable.check);
                        isDockLocationScanned=true;
                    }else{
                        if(!isPalletScanned)
                        common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                        else
                        common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Error");
                    }
                    // TODO isValidDockLocation check DockLocation function
                    return;
                }




/*                //Location Criteria verification
                if (ScanValidator.IsLocationScanned(scannedData)) {
                    if(!isPalletScanned){
                        cvScanPartNo.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanPartNo.setImageResource(R.drawable.check);
                        isFromLocationScanned=true;
                    }else{
                        if(isPalletScanned){
                            cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanDockLocation.setImageResource(R.drawable.check);
                            isToLocationScanned=true;
                        }else{
                            common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                        }
                    }
                    return;
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_083, getActivity(), getContext(), "Error");
                }*/
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_sorting));
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