package com.inventrax.nilkamal_vna.util;



public class ScanValidator {



    public static boolean IsRSNScanned(String scannedData)
    {
        if ((scannedData.split("[/]").length==4 && scannedData.split("[/]", 2)[0].length()==10)
                || (scannedData.length() == 17 && scannedData.substring(0, 1).equals("A") && isNumeric(scannedData.substring(1, 10)))
                || (scannedData.length() == 17 && scannedData.substring(0, 1).equals("D") && isNumeric(scannedData.substring(1, 10)))
                || (scannedData.length() == 17 && scannedData.substring(0, 1).equals("H") && isNumeric(scannedData.substring(1, 10)))
                )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean IsBatchRSN(String scannedData)
    {
        if (scannedData.split("[,]").length==3 )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean IsBundleRSN(String scannedData)
    {
        if (scannedData.split("[_]").length==2 )
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    public static boolean IsLocationScanned(String scannedData)
    {
        if ((scannedData.length() == 7 || scannedData.length()  == 8) && (isNumeric(scannedData.substring(0, 2))))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static Boolean IsDockScanned(String scanneddata)
    {
        if (scanneddata.split("[-]").length > 2 && scanneddata.startsWith("00-00-"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static Boolean IsPalletScanned(String scanneddata)
    {
        if (scanneddata.length() == 8 &&((scanneddata.substring(0,1).equals("H"))|| (scanneddata.substring(0,1).equals("P"))))
        {
            return true;
        }
        else
        {
            return false;
        }
    }



    public static boolean isNumeric(String ValueToCheck)
    {

        try
        {
            Double result = Double.parseDouble(ValueToCheck);
            return true;
        }
        catch(Exception ex)
        {
            return false;
        }
    }
    public static boolean IsMatressBundleScanned(String scannedData)
    {
        if ((scannedData.substring(2, 5).equals("VLP")  || scannedData.substring(2, 5).equals("vlp"))&& scannedData.split("[_]")[0].length()==10)
        {
            return true;
        }else
        {
            return false;
        }
    }

}