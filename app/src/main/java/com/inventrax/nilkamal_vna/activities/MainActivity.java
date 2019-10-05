package com.inventrax.nilkamal_vna.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.honeywell.aidc.BarcodeReader;
import com.inventrax.nilkamal_vna.R;
import com.inventrax.nilkamal_vna.application.AbstractApplication;
import com.inventrax.nilkamal_vna.common.Common;
import com.inventrax.nilkamal_vna.common.constants.EndpointConstants;
import com.inventrax.nilkamal_vna.common.constants.ErrorMessages;
import com.inventrax.nilkamal_vna.fragments.AboutFragment;

import com.inventrax.nilkamal_vna.fragments.DrawerFragment;
import com.inventrax.nilkamal_vna.fragments.HomeFragment;

import com.inventrax.nilkamal_vna.interfaces.ApiInterface;
import com.inventrax.nilkamal_vna.logout.LogoutUtil;
import com.inventrax.nilkamal_vna.model.NavDrawerItem;
import com.inventrax.nilkamal_vna.pojos.LoginUserDTO;
import com.inventrax.nilkamal_vna.pojos.PrinterDetailsDTO;
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.pojos.WMSExceptionMessage;
import com.inventrax.nilkamal_vna.searchableSpinner.SearchableSpinner;
import com.inventrax.nilkamal_vna.services.RestService;
import com.inventrax.nilkamal_vna.util.AndroidUtils;
import com.inventrax.nilkamal_vna.util.DialogUtils;
import com.inventrax.nilkamal_vna.util.ExceptionLoggerUtils;
import com.inventrax.nilkamal_vna.util.FragmentUtils;
import com.inventrax.nilkamal_vna.util.ProgressDialogUtils;
import com.inventrax.nilkamal_vna.util.SharedPreferencesUtils;
import com.inventrax.nilkamal_vna.util.SoundUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements DrawerFragment.FragmentDrawerListener{

    private static final String classCode = "API_ACT_002";
    private Toolbar mToolbar;
    private DrawerFragment drawerFragment;
    private FragmentUtils fragmentUtils;
    private CharSequence[] userRouteCharSequences;
    private List<String> userRouteStringList;
    private String selectedRouteCode,ipAdress="";
    private FragmentActivity fragmentActivity;
    private SharedPreferencesUtils sharedPreferencesUtils,sharedPreferencesUtils1;
    private LogoutUtil logoutUtil;
    private static BarcodeReader barcodeReader;
    Dialog ipAddressdialog;
    private Common common = null;
    private ErrorMessages errorMessages;
    SoundUtils sound = null;
    ExceptionLoggerUtils exceptionLoggerUtils;
    private Gson gson;
    private WMSCoreMessage core;


          public static BarcodeReader getBarcodeObject() {
            return barcodeReader;
          }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            try {

                setContentView(R.layout.activity_main);

                loadFormControls();

            } catch (Exception ex) {
               // Logger.Log(MainActivity.class.getName(), ex);
            }
        }

        public void loadFormControls() {
            try {

                logoutUtil = new LogoutUtil();
                 mToolbar = (Toolbar) findViewById(R.id.toolbar);

                fragmentUtils = new FragmentUtils();

                common = new Common();
                errorMessages = new ErrorMessages();
                exceptionLoggerUtils = new ExceptionLoggerUtils();
                sound = new SoundUtils();
                gson = new GsonBuilder().create();
                core = new WMSCoreMessage();

                fragmentActivity = this;

                new ProgressDialogUtils(this);

                AbstractApplication.FRAGMENT_ACTIVITY = this;

                setSupportActionBar(mToolbar);

               /* if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                    getSupportActionBar().setIcon(R.mipmap.ic_launcher);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }*/

                View logoView = AndroidUtils.getToolbarLogoIcon(mToolbar);

                if (logoView != null)
                    logoView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentUtils.replaceFragmentWithBackStack(fragmentActivity, R.id.container_body, new HomeFragment());
                        }
                    });

                drawerFragment = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
                drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
                drawerFragment.setDrawerListener(this);

                sharedPreferencesUtils = new SharedPreferencesUtils("LoginActivity", getApplicationContext());
                sharedPreferencesUtils1 = new SharedPreferencesUtils("SettingsActivity", getApplicationContext());

                userRouteStringList = new ArrayList<>();

                userRouteCharSequences = userRouteStringList.toArray(new CharSequence[userRouteStringList.size()]);
                logoutUtil.setActivity(this);
                logoutUtil.setFragmentActivity(fragmentActivity);
                logoutUtil.setSharedPreferencesUtils(sharedPreferencesUtils);
                // display the first navigation drawer view on app launch
                displayView(0, new NavDrawerItem(false, "Home"));

            } catch (Exception ex) {
                DialogUtils.showAlertDialog(this, "Error while loading form controls");
                return;
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);

            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement

            switch (id) {
                case R.id.action_logout: {

                   logoutUtil.doLogout();

                }
                break;

                /*
                case R.id.action_settings: {
                    FragmentUtils.replaceFragmentWithBackStack(this, R.id.container_body, new SettingsFragment());
                }
                */

                case R.id.action_about: {

                   FragmentUtils.replaceFragmentWithBackStack(this, R.id.container_body, new AboutFragment());

                }

                case R.id.action_ipaddress: {
                    getPrinters();
                }
                break;

            }

            return super.onOptionsItemSelected(item);
        }


        @Override
        public void onDrawerItemSelected(View view, int position, NavDrawerItem menuItem) {

            displayView(position, menuItem);
        }



    private void displayView(int position, NavDrawerItem menuItem) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);

/*        switch (menuItem.getTitle()) {
            case "Home": {
                fragment = new HomeFragment();
                title = "Home";
            }
            break;


            default:
                break;
        }*/

        if (menuItem.getTitle().equals("Home")) {
            fragment = new HomeFragment();
            title = "Home";
        }

        if (fragment != null) {

            FragmentUtils.replaceFragmentWithBackStack(this, R.id.container_body, fragment);

            // set the toolbar title
            getSupportActionBar().setTitle(title);

        }
    }

    public void getPrinters() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.LoginUserDTO,  MainActivity.this);
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
                // DialogUtils.showAlertDialog(MainActivity.this, "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_01", MainActivity.this);
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, MainActivity.this, MainActivity.this, "Error");
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
                                DialogUtils.showAlertDialog(MainActivity.this, owmsExceptionMessage.getWMSMessage());
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
                                    DialogUtils.showAlertDialog(MainActivity.this, "No Printers Available");
                                } else {
                                    ProgressDialogUtils.closeProgressDialog();
                                    ipAddressdialog = new Dialog(MainActivity.this);
                                    ipAddressdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    ipAddressdialog.setCancelable(false);
                                    ipAddressdialog.setContentView(R.layout.pinter_dialog1);

                                    TextView btnOk = (TextView) ipAddressdialog.findViewById(R.id.btnOk);
                                    btnOk.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            sharedPreferencesUtils1.removePreferences("url");
                                            sharedPreferencesUtils1.savePreference("url", ipAdress);
                                            ipAddressdialog.dismiss();
                                        }
                                    });

                                    TextView btnCancel = (TextView) ipAddressdialog.findViewById(R.id.btnCancel);
                                    btnCancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ipAddressdialog.dismiss();
                                        }
                                    });
                                    final SearchableSpinner spinnerSelectPrinter=(SearchableSpinner) ipAddressdialog.findViewById(R.id.spinnerSelectReason);
                                    ArrayAdapter arrayAdapterSelectPrinter = new ArrayAdapter(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, lstPrinters);
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
                                    ipAddressdialog.show();

                                }
                            }
                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", MainActivity.this);
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, MainActivity.this, MainActivity.this, "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", MainActivity.this);
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, MainActivity.this, MainActivity.this, "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", MainActivity.this);
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0002, MainActivity.this, MainActivity.this, "Error");
        }
    }

    public void logException() {

        try {
            String textFromFile = ExceptionLoggerUtils.readFromFile(MainActivity.this);
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Exception, MainActivity.this);
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
                // DialogUtils.showAlertDialog(MainActivity.this, "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, MainActivity.this, MainActivity.this, "Error");
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
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002", MainActivity.this);

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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, MainActivity.this, MainActivity.this, "Error");
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0003, MainActivity.this, MainActivity.this, "Error");
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, MainActivity.this, MainActivity.this, "Error");
        }
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(((DrawerLayout)findViewById(R.id.drawer_layout)).isDrawerOpen(GravityCompat.START)) {
            ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        }else{
/*            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
            }else{
                if (doubleBackToExitPressedOnce) {
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory( Intent.CATEGORY_HOME );
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                    finish();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                final Toast toast = Toast.makeText(this, "Please click Back again to exit", Toast.LENGTH_SHORT);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 1000);

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 3000);
            }*/
        }
    }

        @Override
        protected void onPause() {

            super.onPause();

        }

        @Override
        protected void onResume() {
            super.onResume();

       /* try {

            EnterpriseDeviceManager enterpriseDeviceManager = (EnterpriseDeviceManager)getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);

            RestrictionPolicy restrictionPolicy = enterpriseDeviceManager.getRestrictionPolicy();

            restrictionPolicy.allowSettingsChanges(false);

        }catch (Exception ex){

        }*/
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
        }

        private void initiateBackgroundServices() {



        }

}
