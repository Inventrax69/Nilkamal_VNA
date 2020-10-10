package com.inventrax.nilkamal_vna.login;

import com.inventrax.nilkamal_vna.application.AbstractApplication;
import com.inventrax.nilkamal_vna.util.SharedPreferencesUtils;

public class LoginInteractorImpl implements LoginInteractor {

    private SharedPreferencesUtils sharedPreferencesUtils;
    private OnLoginFinishedListener listener;
    private boolean isRememberEnabled;
    String userName="",password="";

    public LoginInteractorImpl(){

        sharedPreferencesUtils = new SharedPreferencesUtils("LoginActivity", AbstractApplication.get());

    }

    @Override
    public void login(final String username, final String password, boolean isRememberEnabled, final OnLoginFinishedListener listener) {

        try {



            this.listener = listener;
            this.isRememberEnabled = isRememberEnabled;
            this.userName = username;
            this.password = password;



        }catch (Exception ex){
          //  Logger.Log(LoginInteractorImpl.class.getName());
            listener.onLoginError("Error while login");
            return;
        }

    }



}


