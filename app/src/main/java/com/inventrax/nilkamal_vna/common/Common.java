package com.inventrax.nilkamal_vna.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.inventrax.nilkamal_vna.R;
import com.inventrax.nilkamal_vna.common.constants.EndpointConstants;
import com.inventrax.nilkamal_vna.pojos.WMSCoreAuthentication;
import com.inventrax.nilkamal_vna.pojos.WMSCoreMessage;
import com.inventrax.nilkamal_vna.pojos.WMSExceptionMessage;
import com.inventrax.nilkamal_vna.util.AndroidUtils;
import com.inventrax.nilkamal_vna.util.DateUtils;
import com.inventrax.nilkamal_vna.util.DialogUtils;
import com.inventrax.nilkamal_vna.util.SoundUtils;

/**
 * Created by Prasanna.ch on 06/14/2018.
 */

public class Common {
    private WMSCoreMessage core;
    private Gson gson;
    String userId = null;

    private SoundUtils soundUtils;
    public static boolean isPopupActive;

    public static boolean isPopupActive()
    {
        return isPopupActive;
    }

    public static void setIsPopupActive(boolean isPopupActive) {
        Common.isPopupActive = isPopupActive;
    }




    // commom Authentication Object
    public WMSCoreMessage SetAuthentication(EndpointConstants Constant, Context context) {

        SharedPreferences sp = context.getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        WMSCoreMessage message = new WMSCoreMessage();
        WMSCoreAuthentication token = new WMSCoreAuthentication();
        token.setAuthKey(AndroidUtils.getDeviceSerialNumber().toString());
        token.setUserId(userId);
        token.setAuthValue("");
        token.setLoginTimeStamp(DateUtils.getTimeStamp().toString());
        token.setAuthToken("");
        token.setRequestNumber(1);
        message.setType(Constant);
        message.setAuthToken(token);
        return message;
    }


    public void showUserDefinedAlertType(String errorCode, Activity activity, Context context,String alerttype) {

        soundUtils = new SoundUtils();
        if (alerttype.equals("Critical Error")) {
            setIsPopupActive(true);
            soundUtils.alertCriticalError(activity, context);
            DialogUtils.showAlertDialog(activity, "Critical Error", errorCode, R.drawable.link_break, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (alerttype.equals("Error")) {
            setIsPopupActive(true);
            soundUtils.alertError(activity, context);
            DialogUtils.showAlertDialog(activity, "Error", errorCode, R.drawable.cross_circle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (alerttype.equals("Warning")) {
            setIsPopupActive(true);
            soundUtils.alertWarning(activity, context);
            DialogUtils.showAlertDialog(activity, "Warning", errorCode, R.drawable.warning_img,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (alerttype.equals("Success")) {
            setIsPopupActive(true);
            soundUtils.alertSuccess(activity, context);
            DialogUtils.showAlertDialog(activity, "Success", errorCode, R.drawable.success,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });
            return;
        }

    }


    public void showAlertType(WMSExceptionMessage wmsExceptionMessage, Activity activity, Context context) {

        soundUtils = new SoundUtils();
        if (wmsExceptionMessage.isShowAsCriticalError()) {
            setIsPopupActive(true);
            soundUtils.alertCriticalError(activity, context);
            DialogUtils.showAlertDialog(activity, "Critical Error", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.cross_circle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (wmsExceptionMessage.isShowAsError()) {
            setIsPopupActive(true);
            soundUtils.alertError(activity, context);
            DialogUtils.showAlertDialog(activity, "Error", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.cross_circle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (wmsExceptionMessage.isShowAsWarning()) {
            setIsPopupActive(true);
            soundUtils.alertWarning(activity, context);
            DialogUtils.showAlertDialog(activity, "Warning", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.warning_img,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (wmsExceptionMessage.isShowAsSuccess()) {
            setIsPopupActive(true);
            soundUtils.alertSuccess(activity, context);
            DialogUtils.showAlertDialog(activity, "Success", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.success,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });
            return;
        }

    }
}