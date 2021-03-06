package com.inventrax.nilkamal_vna.fragments.HU;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
import com.inventrax.nilkamal_vna.adapters.PalletAdapter;
import com.inventrax.nilkamal_vna.common.Common;
import com.inventrax.nilkamal_vna.common.constants.EndpointConstants;
import com.inventrax.nilkamal_vna.common.constants.ErrorMessages;
import com.inventrax.nilkamal_vna.fragments.HomeFragment;
import com.inventrax.nilkamal_vna.interfaces.ApiInterface;
import com.inventrax.nilkamal_vna.pojos.InboundDTO;
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
    private CardView cvScanPartNo, cvScanPallet, cvScanDockLocation, cvScanNewRSN;
    private ImageView ivScanPartNo, ivScanPallet, ivScanDockLocation, ivScanNewRSN;
    private EditText etPartNo, etDockLocation, etPallet, etNewQty;
    Button btnClear, btnSkip, btnCloseLoadPallet, btnGo, btnExport, btnCloseExport, btnCloseOne;
    private Common common = null;
    SoundUtils soundUtils = null;
    String scanner = null, vlpdId = null;
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
    ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    public Bundle bundle;
    boolean isPalletScanned, isPartNoScanned, isDockLocationScanned, isNewRsn;
    RelativeLayout rlVLPDSelect, rlSorting, rlExport;
    private SearchableSpinner spinnerSelectVLPDNo;
    String storageVLPDNo = "";
    TextView txtVLPDNumber, txtMcode, txtDockName, txtPendingQty, txtDesc;
    EditText txtBatchNo, txtHuNo, txtHuSize;
    public VlpdDto mVlpdDto;
    public String sNewUniqueRSN = "", sPalletNo = "", ipAdress = "", sUniqueRSN = "", sPendingQty = "", sDockName = "";
    LinearLayout layoutnewRsn;
    Dialog pickingSkipdialog;
    String VLPDNumber = "", Pallet = "", ActualLoc = "", SuggestedLoc = "", Type = "";
    RecyclerView rvPickingSortingList;
    private SearchableSpinner spinnerSelectReason;
    String skipReason;
    RadioButton radioWithOutMRP,radioWithMRP;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public PickingSortingtHU() {
    }

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

        isPalletScanned = false;
        isPartNoScanned = false;
        isDockLocationScanned = false;
        isNewRsn = false;

        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanPartNo = (CardView) rootView.findViewById(R.id.cvScanPartNo);
        cvScanDockLocation = (CardView) rootView.findViewById(R.id.cvScanDockLocation);
        cvScanNewRSN = (CardView) rootView.findViewById(R.id.cvScanNewRSN);

        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanPartNo = (ImageView) rootView.findViewById(R.id.ivScanPartNo);
        ivScanDockLocation = (ImageView) rootView.findViewById(R.id.ivScanDockLocation);
        ivScanNewRSN = (ImageView) rootView.findViewById(R.id.ivScanNewRSN);

        etPartNo = (EditText) rootView.findViewById(R.id.etPartNo);
        etNewQty = (EditText) rootView.findViewById(R.id.etNewQty);
        etPallet = (EditText) rootView.findViewById(R.id.etPallet);
        etDockLocation = (EditText) rootView.findViewById(R.id.etDockLocation);
        txtBatchNo = (EditText) rootView.findViewById(R.id.txtBatchNo);
        txtHuNo = (EditText) rootView.findViewById(R.id.txtHuNo);
        txtHuSize = (EditText) rootView.findViewById(R.id.txtHuSize);

        txtVLPDNumber = (TextView) rootView.findViewById(R.id.txtVLPDNumber);
        txtMcode = (TextView) rootView.findViewById(R.id.txtMcode);
        txtDesc = (TextView) rootView.findViewById(R.id.txtDesc);
        txtDockName = (TextView) rootView.findViewById(R.id.txtDockName);
        txtPendingQty = (TextView) rootView.findViewById(R.id.txtPendingQty);

        rlVLPDSelect = (RelativeLayout) rootView.findViewById(R.id.rlVLPDSelect);
        rlSorting = (RelativeLayout) rootView.findViewById(R.id.rlSorting);
        rlExport = (RelativeLayout) rootView.findViewById(R.id.rlExport);

        rvPickingSortingList = (RecyclerView) rootView.findViewById(R.id.rvPickingSortingList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPickingSortingList.setLayoutManager(linearLayoutManager);
        rvPickingSortingList.setHasFixedSize(true);

        layoutnewRsn = (LinearLayout) rootView.findViewById(R.id.layoutnewRsn);

        layoutnewRsn.setVisibility(View.INVISIBLE);

        spinnerSelectVLPDNo = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectVLPDNo);

        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnSkip = (Button) rootView.findViewById(R.id.btnSkip);
        btnCloseLoadPallet = (Button) rootView.findViewById(R.id.btnCloseLoadPallet);
        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnExport = (Button) rootView.findViewById(R.id.btnExport);
        btnCloseExport = (Button) rootView.findViewById(R.id.btnCloseExport);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);

        btnClear.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        btnCloseOne.setOnClickListener(this);
        btnCloseLoadPallet.setOnClickListener(this);
        btnGo.setOnClickListener(this);
        btnExport.setOnClickListener(this);
        btnCloseExport.setOnClickListener(this);
        spinnerSelectVLPDNo.setOnItemSelectedListener(this);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");
        SharedPreferences spPrinterIP = getActivity().getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);

        try {
            if (getArguments().getString("VLPDNumber") != null) {
                clearAllFileds();
                VLPDNumber = getArguments().getString("VLPDNumber");
                Pallet = getArguments().getString("Pallet");
                ActualLoc = getArguments().getString("ActualLoc");
                SuggestedLoc = getArguments().getString("SuggestedLoc");
                Type = getArguments().getString("Type");
                txtVLPDNumber.setText(VLPDNumber);
                rlVLPDSelect.setVisibility(View.GONE);
                rlSorting.setVisibility(View.VISIBLE);
                rlExport.setVisibility(View.GONE);
                // GetVNAPickingandShortingList(Pallet);
            } else {
                rlVLPDSelect.setVisibility(View.VISIBLE);
                rlSorting.setVisibility(View.GONE);
                rlExport.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            rlVLPDSelect.setVisibility(View.VISIBLE);
            rlSorting.setVisibility(View.GONE);
            rlExport.setVisibility(View.GONE);
        }

        common = new Common();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();
        soundUtils = new SoundUtils();
        mVlpdDto = new VlpdDto();

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
        clearAllFileds();
    }

    public void clearAllFileds() {
        ProgressDialogUtils.closeProgressDialog();
        Common.setIsPopupActive(false);
        etPartNo.setText("");
        etPallet.setText("");
        etNewQty.setText("");
        etDockLocation.setText("");
        txtMcode.setText("");
        txtBatchNo.setText("");
        txtDockName.setText("");
        txtDesc.setText("");
        txtHuNo.setText("");
        txtHuSize.setText("");
        txtPendingQty.setText("");
        isPalletScanned = false;
        isPartNoScanned = false;
        isDockLocationScanned = false;
        isNewRsn = false;
        sNewUniqueRSN = "";
        sPalletNo = "";
        ipAdress = "";
        sUniqueRSN = "";
        cvScanPartNo.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanPartNo.setImageResource(R.drawable.fullscreen_img);
        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);
        cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanDockLocation.setImageResource(R.drawable.fullscreen_img);
        cvScanNewRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanNewRSN.setImageResource(R.drawable.fullscreen_img);
        layoutnewRsn.setVisibility(View.INVISIBLE);
    }

    public void clearAllFileds1() {
        etPartNo.setText("");
        etNewQty.setText("");
        txtMcode.setText("");
        txtBatchNo.setText("");
        txtDockName.setText("");
        txtHuNo.setText("");
        txtHuSize.setText("");
        txtPendingQty.setText("");
        isPartNoScanned = false;
        isDockLocationScanned = false;
        isNewRsn = false;
        sNewUniqueRSN = "";
        sPalletNo = "";
        ipAdress = "";
        sUniqueRSN = "";
        cvScanPartNo.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanPartNo.setImageResource(R.drawable.fullscreen_img);
        cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanDockLocation.setImageResource(R.drawable.fullscreen_img);
        cvScanNewRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanNewRSN.setImageResource(R.drawable.fullscreen_img);
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

                                if (_lVLPD.size() > 0) {
                                    List<VLPDResponseDTO> lstDto = new ArrayList<VLPDResponseDTO>();
                                    List<String> lstVLPD = new ArrayList<>();

                                    VLPDResponseDTO dto = null;
                                    for (int i = 0; i < _lVLPD.size(); i++) {
                                        dto = new VLPDResponseDTO(_lVLPD.get(i).entrySet());
                                        lstDto.add(dto);
                                        lstVLPD.add(lstDto.get(i).getVLPDNumber());
                                    }

                                    ArrayAdapter arrayAdapterStoreRefNo = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstVLPD);
                                    spinnerSelectVLPDNo.setAdapter(arrayAdapterStoreRefNo);

                                } else {
                                    common.showUserDefinedAlertType(errorMessages.EMC_039, getActivity(), getContext(), "Error");
                                    clearAllFileds();
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
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetOpenVLPDListByPriority_04", getActivity());
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
                if (isPalletScanned) {
                    //TODO SKIP
                    final Dialog pickingSkipdialog = new Dialog(getActivity());
                    pickingSkipdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    pickingSkipdialog.setCancelable(false);
                    pickingSkipdialog.setContentView(R.layout.skip_dialog);

                    TextView btnOk = (TextView) pickingSkipdialog.findViewById(R.id.btnOk);
                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PickingandShortingSkip();
                            pickingSkipdialog.dismiss();
                        }
                    });

                    TextView btnCancel = (TextView) pickingSkipdialog.findViewById(R.id.btnCancel);
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pickingSkipdialog.dismiss();
                        }
                    });
                    final List<String> lstSkipReason = new ArrayList<>();
                    lstSkipReason.add("Damage");
                    lstSkipReason.add("Not Found");
                    spinnerSelectReason = (SearchableSpinner) pickingSkipdialog.findViewById(R.id.spinnerSelectReason);
                    ArrayAdapter arrayAdapterSelectReason = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstSkipReason);
                    spinnerSelectReason.setAdapter(arrayAdapterSelectReason);
                    spinnerSelectReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            skipReason = spinnerSelectReason.getSelectedItem().toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            skipReason = lstSkipReason.get(0);
                        }
                    });
                    pickingSkipdialog.show();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Warning");
                }

                break;
            case R.id.btnCloseExport:
                rlVLPDSelect.setVisibility(View.GONE);
                rlSorting.setVisibility(View.VISIBLE);
                rlExport.setVisibility(View.GONE);
                break;
            case R.id.btnExport:
                // TODO check VLDP# List
                rlVLPDSelect.setVisibility(View.GONE);
                rlSorting.setVisibility(View.GONE);
                rlExport.setVisibility(View.VISIBLE);
                ExportPendingPallet();
                break;
            case R.id.btnGo:
                if (!txtVLPDNumber.getText().toString().isEmpty()) {
                    txtVLPDNumber.setText(storageVLPDNo);
                    rlVLPDSelect.setVisibility(View.GONE);
                    rlSorting.setVisibility(View.VISIBLE);
                    rlExport.setVisibility(View.GONE);
                } else {
                    common.showUserDefinedAlertType("Please select VLPD#", getActivity(), getContext(), "Warning");
                }
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

    private void ExportPendingPallet() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setvLPDNumber(txtVLPDNumber.getText().toString());
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
            ProgressDialogUtils.showProgressDialog("Please Wait");
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.ExportPendingPallet(message);

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
                                List<VlpdDto> vlpdDtoList = new ArrayList<>();
                                for (int i = 0; i < _lVlpd.size(); i++) {
                                    vlpdDto1 = new VlpdDto(_lVlpd.get(i).entrySet());
                                    vlpdDtoList.add(vlpdDto1);
                                }

                                rvPickingSortingList.setAdapter(null);
                                PalletAdapter palletAdapter = new PalletAdapter(getContext(), vlpdDtoList);
                                rvPickingSortingList.setAdapter(palletAdapter);
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

                if (rlExport.getVisibility() == View.VISIBLE) {
                    return;
                }

                if (ScanValidator.IsPalletScanned(scannedData)) {
                    GetVNAPickingandShortingList(scannedData);
                    return;
                }

                if (ScanValidator.IsLocationScanned(scannedData)) {
                    if (isPalletScanned) {
                        PickingandShortingDockValidation(scannedData);
                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Warning");
                    }
                    return;
                }

                if (ScanValidator.IsBundleScanOnBundling(scannedData)) {
                    if (isPalletScanned && isDockLocationScanned) {
                        if (isNewRsn) {
                            PickandCheck(scannedData, "1");
                        } else {
                            PickandCheck(scannedData, "1");
                        }
                    } else {
                        if (!isPalletScanned)
                            common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Warning");
                        else
                            common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Warning");
                    }
                    return;
                }


                if (ScanValidator.IsRSNScanned(scannedData)) {
                    if (isPalletScanned && isDockLocationScanned) {
                        if (isNewRsn) {
                            PickandCheck(scannedData, "0");
                        } else {
                            PickandCheck(scannedData, "0");
                        }
                    } else {
                        if (!isPalletScanned)
                            common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Warning");
                        else
                            common.showUserDefinedAlertType(errorMessages.EMC_0015, getActivity(), getContext(), "Warning");
                    }
                    return;
                }


            } else {
                if (!Common.isPopupActive()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_081, getActivity(), getContext(), "Error");
                }
                sound.alertWarning(getActivity(), getContext());

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

        storageVLPDNo = spinnerSelectVLPDNo.getSelectedItem().toString();
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


                                VlpdDto vlpdDto = null;
                                for (int i = 0; i < _lVlpd.size(); i++) {
                                    vlpdDto = new VlpdDto(_lVlpd.get(i).entrySet());
                                }

                                mVlpdDto = vlpdDto;

                                if (vlpdDto.getResult().equals("1") && vlpdDto.getResult() != null) {
                                    if (!vlpdDto.getPendingQty().equals("0.00")) {
                                        txtMcode.setText(vlpdDto.getMcode());
                                        txtBatchNo.setText(vlpdDto.getBatchNo());
                                        txtDockName.setText(vlpdDto.getDockName());
                                        txtHuNo.setText(vlpdDto.getHUNo());
                                        txtHuSize.setText(vlpdDto.getHUSize());
                                        txtDesc.setText(vlpdDto.getDescription());
                                        txtPendingQty.setText("Qty: " + vlpdDto.getPendingQty());
                                        etPallet.setText(scannedData);
                                        sPalletNo = scannedData;
                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPallet.setImageResource(R.drawable.check);
                                        isPalletScanned = true;
                                        if (!sDockName.equals(vlpdDto.getDockName())) {
                                            cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                            ivScanDockLocation.setImageResource(R.drawable.fullscreen_img);
                                            etDockLocation.setText("");
                                            isDockLocationScanned = false;
                                            sDockName = vlpdDto.getDockName();
                                        }
                                    } else {
                                        clearAllFileds();
                                        common.showUserDefinedAlertType("Qty limit exceeded", getActivity(), getContext(), "Error");
                                    }
                                } else {
                                    Common.setIsPopupActive(true);
                                    clearAllFileds();
                                    sPalletNo = scannedData;
                                    soundUtils.alertError(getActivity(), getContext());
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

    private void PickandCheck(final String scannedData, final String rsnType) {

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
            if (isNewRsn) {
                vlpdDto.setUniqueRSN(sUniqueRSN);
                vlpdDto.setNewUniqueRSN(scannedData);
            } else {
                vlpdDto.setUniqueRSN(scannedData);
                vlpdDto.setNewUniqueRSN(sNewUniqueRSN);
            }
            vlpdDto.setUserId(userId);
            vlpdDto.setPalletNo(sPalletNo);
            vlpdDto.setStorageLocation(mVlpdDto.getStorageLocation());
            vlpdDto.setRSNType(rsnType);

            message.setEntityObject(vlpdDto);

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

                                sUniqueRSN = scannedData;
                                ProgressDialogUtils.closeProgressDialog();
                                if (vlpdDto1.getMessage() != null) {
                                    if (vlpdDto1.getMessage().equals("-1")) {
                                        if (rsnType.equalsIgnoreCase("0")) {
                                            etPartNo.setText(scannedData);
                                            cvScanPartNo.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanPartNo.setImageResource(R.drawable.check);
                                            isPartNoScanned = true;
                                            getPrinters();
                                        } else {
                                            common.showUserDefinedAlertType("Please do un-bundle for this bundle number" + " " + scannedData, getActivity(), getContext(), "Warning");
                                        }
                                    }
                                    if (vlpdDto1.getMessage().equals("-2")) {
                                        common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Error");
                                    }
                                    if (vlpdDto1.getMessage().equals("-3")) {
                                        common.showUserDefinedAlertType(errorMessages.EMC_091, getActivity(), getContext(), "Error");
                                    }
                                    if (vlpdDto1.getMessage().equals("-4")) {
                                        common.showUserDefinedAlertType(errorMessages.EMC_092, getActivity(), getContext(), "Error");
                                    }
                                    if (vlpdDto1.getMessage().equals("1")) {
                                        if (isNewRsn) {
                                            cvScanNewRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanNewRSN.setImageResource(R.drawable.check);
                                            isNewRsn = true;
                                        } else {
                                            etPartNo.setText(scannedData);
                                            cvScanPartNo.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanPartNo.setImageResource(R.drawable.check);
                                            isPartNoScanned = true;
                                        }
                                        isNewRsn = false;
                                        layoutnewRsn.setVisibility(View.INVISIBLE);
                                        cvScanNewRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanNewRSN.setImageResource(R.drawable.fullscreen_img);
                                        GetVNAPickingandShortingList(sPalletNo);
                                    }

                                } else {

                                    Common.setIsPopupActive(true);
                                    soundUtils.alertError(getActivity(), getContext());
                                    DialogUtils.showAlertDialog(getActivity(), "Warning", errorMessages.EMC_093, R.drawable.warning_img, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    Common.setIsPopupActive(false);
                                                    clearAllFileds();
                                                    break;
                                            }
                                        }
                                    });
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
            vlpdDto.setUniqueRSN(sUniqueRSN);
            vlpdDto.setIpAddress(ipAdress);
            vlpdDto.setMcode(mVlpdDto.getMcode());
            vlpdDto.setAssignedId(mVlpdDto.getAssignedId());
            vlpdDto.setPickedQty(mVlpdDto.getPendingQty());
            if(radioWithMRP.isChecked()){
                vlpdDto.setPrintType("With M.R.P");
            }else{
                vlpdDto.setPrintType("Without M.R.P");
            }
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
                                        layoutnewRsn.setVisibility(View.VISIBLE);
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
                                    pickingSkipdialog.setContentView(R.layout.pinter_dialog_wrp);

                                    TextView btnOk = (TextView) pickingSkipdialog.findViewById(R.id.btnOk);

                                    radioWithOutMRP = (RadioButton) pickingSkipdialog.findViewById(R.id.radioWithOutMRP);
                                    radioWithMRP = (RadioButton) pickingSkipdialog.findViewById(R.id.radioWithMRP);

                                    btnOk.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            PrintNewRSN();
                                        }
                                    });

                                    radioWithOutMRP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                            if(b){
                                                radioWithOutMRP.setChecked(true);
                                                radioWithMRP.setChecked(false);
                                            }
                                        }
                                    });

                                    radioWithMRP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                            if(b){
                                                radioWithOutMRP.setChecked(false);
                                                radioWithMRP.setChecked(true);
                                            }
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


    private void GetVLPDPendingPalletCheck() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDDTO, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setUserId(userId);
            vlpdDto.setvLPDNumber(txtVLPDNumber.getText().toString());
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

                                    soundUtils.alertError(getActivity(), getContext());
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
                                    soundUtils.alertError(getActivity(), getContext());
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
            vlpdDto.setvLPDNumber(txtVLPDNumber.getText().toString());
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
                                    bundle.putString("VLPDNumber", txtVLPDNumber.getText().toString());
                                    bundle.putString("Pallet", sPalletNo);
                                    bundle.putString("ActualLoc", vlpdDto1.getActvalLocation());
                                    bundle.putString("SuggestedLoc", vlpdDto1.getSuggestedLoc());
                                    bundle.putString("Type", Type);
                                    bundle.putString("isSLoc", "false");

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


    private void PickingandShortingDockValidation(final String scannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setUserId(userId);
            vlpdDto.setDockLocation(scannedData);
            vlpdDto.setDockNumber(txtDockName.getText().toString());
            message.setEntityObject(vlpdDto);

            Log.v("ABCDE", new Gson().toJson(message));

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.PickingandShortingDockValidation(message);
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

                                List<LinkedTreeMap<?, ?>> _lOutbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lOutbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VlpdDto outboundDTO = null;
                                for (int i = 0; i < _lOutbound.size(); i++) {
                                    outboundDTO = new VlpdDto(_lOutbound.get(i).entrySet());
                                }
                                ProgressDialogUtils.closeProgressDialog();
                                if (outboundDTO.getMessage().equals("1")) {
                                    etDockLocation.setText(scannedData);
                                    cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDockLocation.setImageResource(R.drawable.check);
                                    isDockLocationScanned = true;
                                } else {
                                    etDockLocation.setText(scannedData);
                                    cvScanDockLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDockLocation.setImageResource(R.drawable.warning_img);
                                    isDockLocationScanned = false;
                                    common.showUserDefinedAlertType("Invalid dock location", getActivity(), getContext(), "Warning");
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


    private void PickingandShortingSkip() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setUserId(userId);
            vlpdDto.setAssignedId(mVlpdDto.getAssignedId());
            vlpdDto.setQuntity(mVlpdDto.getPendingQty());
            vlpdDto.setSkipReason(skipReason);
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method

                call = apiService.PickingandShortingSkip(message);
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
                                List<LinkedTreeMap<?, ?>> _lvlpd = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lvlpd = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                VlpdDto vlpdDto1 = null;
                                for (int i = 0; i < _lvlpd.size(); i++) {
                                    vlpdDto1 = new VlpdDto(_lvlpd.get(i).entrySet());
                                }

                                if (vlpdDto1.getMessage().equals("1")) {
                                    GetVNAPickingandShortingList(sPalletNo);
                                } else {
                                    common.showUserDefinedAlertType("Error while skip", getActivity(), getContext(), "Error");
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