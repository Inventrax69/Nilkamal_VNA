package com.inventrax.nilkamal_vna.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.inventrax.nilkamal_vna.R;
import com.inventrax.nilkamal_vna.common.Common;
import com.inventrax.nilkamal_vna.common.constants.EndpointConstants;
import com.inventrax.nilkamal_vna.common.constants.ErrorMessages;
import com.inventrax.nilkamal_vna.fragments.HU.BundlingFragment;
import com.inventrax.nilkamal_vna.fragments.HU.VNASLocToSLocFragment;
import com.inventrax.nilkamal_vna.interfaces.ApiInterface;
import com.inventrax.nilkamal_vna.pojos.InboundDTO;
import com.inventrax.nilkamal_vna.pojos.StorageLocationDTO;
import com.inventrax.nilkamal_vna.pojos.VlpdDto;
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.pojos.WMSExceptionMessage;
import com.inventrax.nilkamal_vna.searchableSpinner.SearchableSpinner;
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


public class VNASLocToSLocListFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_SLOC_SLOC_LIST";
    private View rootView;
    private TextView tvSelectSloc;
    private SearchableSpinner spinnerSelectSloc;
    private Button btnGo,btnCancel;
    private Gson gson;
    private WMSCoreMessage core;
    private String Ssloc=null;
    List<List<StorageLocationDTO> >sloc;
    List<String>  lstStorageloc ;
    List<VlpdDto> lstVLPD=null;
    private Common common;
    String userId =null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private String materialType=null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (NetworkUtils.isInternetAvailable(getContext())) {
            rootView = inflater.inflate(R.layout.hu_fragment_vna_sloc_to_sloc, container, false);
            loadFormControls();
        }
        else {
            DialogUtils.showAlertDialog(getActivity(),"Please enable internet");
            // soundUtils.alertSuccess(LoginActivity.this,getBaseContext());
            return rootView;
        }
        return rootView;
    }

    public void loadFormControls()
    {
        if (NetworkUtils.isInternetAvailable(getContext())){

            tvSelectSloc= (TextView)rootView.findViewById(R.id.tvSelectStRef);

            spinnerSelectSloc=(SearchableSpinner)rootView.findViewById(R.id.spinnerSelectSloc);
            spinnerSelectSloc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Ssloc= spinnerSelectSloc.getSelectedItem().toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


            btnGo=(Button)rootView.findViewById(R.id.btnGo);
            btnGo.setOnClickListener(this);
            btnCancel=(Button)rootView.findViewById(R.id.btnCancel);
            btnCancel.setOnClickListener(this);

            gson = new GsonBuilder().create();
            core= new WMSCoreMessage();
            sloc = new ArrayList<>();
            lstStorageloc= new ArrayList<String>();

            SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
            userId = sp.getString("RefUserId", "");
            materialType = sp.getString("division","");

            common= new Common();
            exceptionLoggerUtils= new ExceptionLoggerUtils();
            errorMessages = new ErrorMessages();
            lstVLPD= new ArrayList<VlpdDto>();

            GetSlocVLPDList();

        }else {
            DialogUtils.showAlertDialog(getActivity(),errorMessages.EMC_0025);
            // soundUtils.alertSuccess(LoginActivity.this,getBaseContext());
            return;
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnGo:
                if(Ssloc==null || Ssloc.isEmpty()){
                    common.showUserDefinedAlertType("Please select Vlpd Number",getActivity(),getContext(),"Error");
                }else{
                    Bundle bundle = new Bundle();
                    bundle.putString("SLOCNumber",Ssloc);
                    VNASLocToSLocFragment vnasLocToSLocFragment = new VNASLocToSLocFragment();
                    vnasLocToSLocFragment.setArguments(bundle);
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, vnasLocToSLocFragment);
                }
                break;
            case R.id.btnCancel:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            default:
                break;
        }

    }




    public  void GetSlocVLPDList() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.VLPDDTO,getContext());
            VlpdDto vlpdDto = new VlpdDto();
            vlpdDto.setUserId(userId);
            message.setEntityObject(vlpdDto);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetSlocVLPDList(message);
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

                                List<LinkedTreeMap<?, ?>> _lVLPD = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lVLPD = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<VlpdDto> lstDto = new ArrayList<VlpdDto>();
                                List<String> lstVlpdNo = new ArrayList<>();

                                for (int i = 0; i < _lVLPD.size(); i++) {
                                    VlpdDto dto = new VlpdDto(_lVLPD.get(i).entrySet());
                                    lstDto.add(dto);
                                    lstVLPD = lstDto;
                                }

                                for (int i = 0; i < lstDto.size(); i++) {
                                    lstVlpdNo.add(lstDto.get(i).getvLPDNumber());
                                }

                                ProgressDialogUtils.closeProgressDialog();
                                ArrayAdapter arrayAdapterSsloc = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstVlpdNo);
                                spinnerSelectSloc.setAdapter(arrayAdapterSsloc);

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
                common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");
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
                                    common.showAlertType(owmsExceptionMessage, getActivity(),getContext());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {

                // Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_vna_sloc_to_sloc));
    }

}

