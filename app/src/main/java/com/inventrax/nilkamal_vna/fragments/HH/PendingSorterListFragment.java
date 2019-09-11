package com.inventrax.nilkamal_vna.fragments.HH;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.inventrax.nilkamal_vna.R;
import com.inventrax.nilkamal_vna.adapters.PendingOutboundListAdapter;
import com.inventrax.nilkamal_vna.common.Common;
import com.inventrax.nilkamal_vna.common.constants.EndpointConstants;
import com.inventrax.nilkamal_vna.common.constants.ErrorMessages;
import com.inventrax.nilkamal_vna.interfaces.ApiInterface;
import com.inventrax.nilkamal_vna.pojos.ItemInfoDTO;
import com.inventrax.nilkamal_vna.pojos.PickPendingItemDTO;
import com.inventrax.nilkamal_vna.pojos.VLPDRequestDTO;
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.pojos.WMSExceptionMessage;
import com.inventrax.nilkamal_vna.services.RestService;
import com.inventrax.nilkamal_vna.util.DialogUtils;
import com.inventrax.nilkamal_vna.util.ExceptionLoggerUtils;
import com.inventrax.nilkamal_vna.util.FragmentUtils;
import com.inventrax.nilkamal_vna.util.NetworkUtils;
import com.inventrax.nilkamal_vna.util.ProgressDialogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingSorterListFragment extends Fragment implements View.OnClickListener {

    private static final String classCode = "API_FRAG_PENDING SORTER LIST";
    private View rootView;

    private Button btnCancel;
    private RecyclerView rvSorterPendingList;
    private LinearLayoutManager linearLayoutManager;

    private Gson gson;
    private WMSCoreMessage core;
    private Common common;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;

    private String materialType = null;

    private String vlpdId = null, vlpdNo = null, loc = null, sku = null, desc = null, reqQty = null, balQty = null, barcode = null, ean = null, qty = null, userId = null;

    public ItemInfoDTO vlpdItem = null;

    Bundle bundle;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (NetworkUtils.isInternetAvailable(getContext())) {
            rootView = inflater.inflate(R.layout.fragment_pending_sorterlist, container, false);
            loadFormControls();
        } else {
            DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            return rootView;
        }
        return rootView;
    }

    public void loadFormControls() {

        rvSorterPendingList = (RecyclerView) rootView.findViewById(R.id.rvSorterPendingList);
        rvSorterPendingList.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());

        // use a linear layout manager
        rvSorterPendingList.setLayoutManager(linearLayoutManager);

        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        // Getting values from Shared preferences
        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        materialType = sp.getString("division", "");

        common = new Common();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();


        // Getting arguments from Bundle
        if (getArguments() != null) {

            if (materialType.equals("HU")) {

                vlpdId = getArguments().getString("vlpdId");
                vlpdNo = getArguments().getString("vlpdNo");
                loc = getArguments().getString("loc");
                sku = getArguments().getString("sku");
                desc = getArguments().getString("desc");
                reqQty = getArguments().getString("reqQty");
                balQty = getArguments().getString("balQty");
                barcode = getArguments().getString("barcode");
                ean = getArguments().getString("ean");
                qty = getArguments().getString("qty");
                vlpdItem = (ItemInfoDTO) getArguments().getSerializable("ItemInfoDto");


            } else if (materialType.equals("HH")) {

                vlpdId = getArguments().getString("vlpdId");
                vlpdNo = getArguments().getString("vlpdNo");
                loc = getArguments().getString("loc");
                sku = getArguments().getString("sku");
                desc = getArguments().getString("desc");
                reqQty = getArguments().getString("reqQty");
                balQty = getArguments().getString("balQty");
                barcode = getArguments().getString("barcode");
                ean = getArguments().getString("ean");
                qty = getArguments().getString("qty");
                vlpdItem = (ItemInfoDTO) getArguments().getSerializable("ItemInfoDto");
            }
        }


        // To get Pending Outbound List
        loadPendingSorterListDetails();


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnCancel:
                goBack();
                break;
            default:
                break;

        }
    }

    public void goBack() {

        // Binding Values to the Bundle
        bundle = new Bundle();


        if (materialType == "HU") {

            bundle.putString("vlpdId", vlpdId);
            bundle.putString("vlpdNo", vlpdNo);
            bundle.putString("loc", loc);
            bundle.putString("sku", sku);
            bundle.putString("desc", desc);
            bundle.putString("balQty", balQty);
            bundle.putString("reqQty", reqQty);
            bundle.putString("barcode", barcode);
            ;
            bundle.putString("ean", ean);
            bundle.putString("qty", qty);
            bundle.putSerializable("ItemInfoDto", vlpdItem);

            SorterPickFragmentHH sorterPickFragmentHH = new SorterPickFragmentHH();
            sorterPickFragmentHH.setArguments(bundle);
            FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, sorterPickFragmentHH);
            return;
        } else {

            bundle.putString("vlpdId", vlpdId);
            bundle.putString("vlpdNo", vlpdNo);
            bundle.putString("loc", loc);
            bundle.putString("sku", sku);
            bundle.putString("desc", desc);
            bundle.putString("balQty", balQty);
            bundle.putString("reqQty", reqQty);
            bundle.putString("barcode", barcode);
            ;
            bundle.putString("ean", ean);
            bundle.putString("qty", qty);
            bundle.putSerializable("ItemInfoDto", vlpdItem);

            SorterPickFragmentHH sorterPickFragmentHH = new SorterPickFragmentHH();
            sorterPickFragmentHH.setArguments(bundle);
            FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, sorterPickFragmentHH);
            return;
        }
    }


    public void loadPendingSorterListDetails() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.VLPDRequestDTO, getContext());
            VLPDRequestDTO vlpdRequestDTO = new VLPDRequestDTO();
            vlpdRequestDTO.setUserID(userId);
            vlpdRequestDTO.setVlpdID(vlpdId);
            message.setEntityObject(vlpdRequestDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetPendingPickItemsFromByDispatchID(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetPendingPickItemsFromByDispatchID_01", getActivity());
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
                            if (core.getType() != null) {
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

                                    List<LinkedTreeMap<?, ?>> _lPickPendingItem = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lPickPendingItem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    List<PickPendingItemDTO> listPickPendingItem = new ArrayList<PickPendingItemDTO>();

                                    for (int i = 0; i < _lPickPendingItem.size(); i++) {
                                        PickPendingItemDTO oPickPendingItem = new PickPendingItemDTO(_lPickPendingItem.get(i).entrySet());
                                        listPickPendingItem.add(oPickPendingItem);

                                    }
                                    // Setting Values to the view
                                    PendingOutboundListAdapter pendingOutboundListAdapter = new PendingOutboundListAdapter(getActivity(), listPickPendingItem);
                                    rvSorterPendingList.setAdapter(pendingOutboundListAdapter);
                                    ProgressDialogUtils.closeProgressDialog();

                                }
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetPendingPickItemsFromByDispatchID_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetPendingPickItemsFromByDispatchID_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "GetPendingPickItemsFromByDispatchID_04", getActivity());
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

                            // if any Exception throws
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    ProgressDialogUtils.closeProgressDialog();
                                    return;
                                }
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                                LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                for (Map.Entry<String, String> entry : _lResultvalue.entrySet()) {
                                    if (entry.getKey().equals("Result")) {
                                        String Result = entry.getValue();
                                        if (Result.equals("0")) {

                                            return;
                                        } else {
                                            exceptionLoggerUtils.deleteFile(getActivity());
                                            ProgressDialogUtils.closeProgressDialog();
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {

                            /*try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002",getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();*/

                            ProgressDialogUtils.closeProgressDialog();
                            Log.d("Message", core.getEntityObject().toString());
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

                // Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_pending_sorterlist));
    }
}