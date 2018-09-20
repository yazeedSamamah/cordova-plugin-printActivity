package com.phonegap.plugins.printActivity;

import java.io.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Base64 ;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.R;

import com.google.zxing.BarcodeFormat;

import java.util.Timer;
import vpos.apipackage.PosApiHelper;
import vpos.apipackage.PrintInitException;

import com.setImage.FakeR;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;



import vpos.apipackage.ByteUtil;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
public class PrintActivity extends CordovaPlugin {

      public String tag = "PrintActivity";
    final int PRINT_TEST = 0;
    final int PRINT_UNICODE = 1;
    final int PRINT_BMP = 2;
    final int PRINT_BARCODE = 4;
    final int PRINT_CYCLE = 5;
    final int PRINT_LONGER = 7;
    final int PRINT_OPEN = 8;

    private RadioGroup rg = null;
    private Timer timer;
    private Timer timer2;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private int voltage_level;
    private int BatteryV;
    SharedPreferences preferences;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    
   // TextView textViewMsg = null;
   // TextView textViewGray = null;
    int ret = -1;
    private boolean m_bThreadFinished = true;

   // private boolean is_cycle = false;
  ///  private int cycle_num = 0;

    private int RESULT_CODE = 0;
   // private static final String DISABLE_FUNCTION_LAUNCH_ACTION = "android.intent.action.DISABLE_FUNCTION_LAUNCH";








    /**
     * Constructor.
     */

   public PrintActivity() {
    }
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("print")) {
            try {
      /*if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return false;
        }*/

        printText(callbackContext,args);
         
           } catch (IOException e) {
           //test
               Log.e(tag, e.getMessage());
               e.printStackTrace();
           }
            return true;
        } else if (action.equals("readNfcCard")) {
             try {
             readNfcCard(callbackContext,args);
               } catch (IOException e) {
           //test
               Log.e(tag, e.getMessage());
               e.printStackTrace();
           }
            return true;
        }

      return false;
    }

    
    //private Pos pos;

    int IsWorking = 0;

    PosApiHelper posApiHelper = PosApiHelper.getInstance();

    Intent mPrintServiceIntent; 


  
   



    Handler handlers = new Handler();
  

    public class BatteryReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            voltage_level = intent.getExtras().getInt("level");// ��õ�ǰ����
            Log.e("wbw", "current  = " + voltage_level);
            BatteryV = intent.getIntExtra("voltage", 0);  //电池电压
            Log.e("wbw", "BatteryV  = " + BatteryV);
            Log.e("wbw", "V  = " + BatteryV * 2 / 100);
            //  m_voltage = (int) (65+19*voltage_level/100); //放大十倍
            //   Log.e("wbw","m_voltage  = " + m_voltage );
        }
    }
 /* public static Bitmap decodeBase64(String input)
  {
    byte[] decodedBytes = Base64.decode(input, 0);
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
  }*/
   //This will send data to bluetooth printer
    boolean printText(CallbackContext callbackContext , JSONArray args) throws IOException {
        try {
              m_bThreadFinished = false;
                try {
                    ret = posApiHelper.PrintInit();
                } catch (PrintInitException e) {
                    e.printStackTrace();
                    int initRet = e.getExceptionCode();
                    Log.e(tag, "initRer : " + initRet);
                    callbackContext.error(initRet);
                    return false;
                }
                posApiHelper.PrintSetGray(ret);
                ret = posApiHelper.PrintCheckStatus();
                if (ret == -1) {
                    RESULT_CODE = -1;
                    Log.e(tag, "Lib_PrnCheckStatus fail, ret = " + ret);
                  //  SendMsg("Error, No Paper ");
                    m_bThreadFinished = true;
                    callbackContext.error( "Lib_PrnCheckStatus fail, ret = " + ret);
                    return false;
                } else if (ret == -2) {
                    RESULT_CODE = -1;
                    Log.e(tag, "Lib_PrnCheckStatus fail, ret = " + ret);
                 //   SendMsg("Error, Printer Too Hot ");
                    m_bThreadFinished = true;
                    callbackContext.error("Lib_PrnCheckStatus fail, ret = " + ret);
                    return false;
                } else if (ret == -3) {
                    RESULT_CODE = -1;
                    Log.e(tag, "voltage = " + (BatteryV * 2));
                  //  SendMsg("Battery less :" + (BatteryV * 2));
                    m_bThreadFinished = true;
                    callbackContext.error("Battery low" );
                    return false;
                }
                /*
                else if (voltage_level < 5) {
                    RESULT_CODE = -1;
                    Log.e(tag, "voltage_level = " + voltage_level);
                    SendMsg("Battery capacity less : " + voltage_level);
                    m_bThreadFinished = true;
                    return;
                }*/
                else {
                    RESULT_CODE = 0;
                }

                 String paymentStatus = args.getString(7);
         if(paymentStatus == "success"){
    paymentStatus  =  "data:image/png;base64,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";
    }else{
    paymentStatus =  "data:image/png;base64,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";
    };
        String base64String = paymentStatus ;
        String base64Image = base64String.split(",")[1];
        
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        String base64StringInvoice = "data:image/png;base64,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";
        String base64ImageInvoice = base64StringInvoice.split(",")[1];
        
        byte[] decodedStringInvoice = Base64.decode(base64ImageInvoice, Base64.DEFAULT);
        Bitmap bmap = BitmapFactory.decodeByteArray(decodedStringInvoice, 0, decodedStringInvoice.length);   
                       
                        posApiHelper.PrintBmp(bmap);
                        posApiHelper.PrintStr("- -  - -  - -\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);
                        posApiHelper.PrintStr("CLIENT ACCOUNT #N:" +  args.getString(9));
                        posApiHelper.PrintStr("- -  - -  - -\n");
                        posApiHelper.PrintStr("COMPANY NAME : " +  args.getString(0));                   
                        posApiHelper.PrintStr("MERCHANT NAME : " + args.getString(1) );
                        posApiHelper.PrintStr("MERCHANT ID : " + args.getString(2));
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("- -  - -  - -\n");
                        posApiHelper.PrintStr("INVOICE NO : " + args.getString(3));
                        posApiHelper.PrintStr("DATE : " + args.getString(4));
                        posApiHelper.PrintStr("TIME : " + args.getString(6));
                        posApiHelper.PrintStr("TOTAL :" + args.getString(5) +  " JD");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("- -" + args.getString(8) + "- -\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintBmp(bmp);
                        posApiHelper.PrintStr("");
                        posApiHelper.PrintStr("");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                          
                            ret = posApiHelper.PrintStart();

                          

                            Log.d("", "Lib_PrnStart ret = " + ret);
                             if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                callbackContext.error("No Print Paper");
                                return false ;
                            } else if(ret == -2) {
                                callbackContext.error("too hot");
                                return false;
                            }else if(ret == -3) {
                                callbackContext.error("low voltage");
                                return false;
                            }else{
                                callbackContext.error("Print fail");
                                return false;
                            }
                        } 
                                             
            m_bThreadFinished = true;
            callbackContext.success("print  success");
            return true;

        } catch (Exception e) {
            String errMsg = e.getMessage();  
            callbackContext.error(errMsg);
        }
        return false;
    }
    

     boolean readNfcCard(CallbackContext callbackContext , JSONArray args) throws IOException {
       try {
          long time = System.currentTimeMillis();
          String nfcOn = args.getString(0) ;
                while (System.currentTimeMillis() < time + 100000 ||   nfcOn !=  "true") {
        Log.e("nfc", "heyp nfc Picc_Open start!");
        byte[] NfcData_Len = new byte[5];
        byte[] Technology = new byte[25];
        byte[] NFC_UID = new byte[56];
        byte[] NDEF_message = new byte[500];

        int ret = posApiHelper.PiccNfc( NfcData_Len, Technology, NFC_UID, NDEF_message);
              if (ret == 0) {
              //callbackContext.error( "read NFC card fail");
             //  return false;

        int TechnologyLength = NfcData_Len[0] & 0xFF;
        int NFC_UID_length = NfcData_Len[1] & 0xFF;
        int NDEF_message_length = (NfcData_Len[3] & 0xFF) + (NfcData_Len[4] & 0xFF);
        byte[] NDEF_message_data = new byte[NDEF_message_length];
        byte[] NFC_UID_data = new byte[NFC_UID_length];
        System.arraycopy(NFC_UID, 0, NFC_UID_data, 0, NFC_UID_length);
        System.arraycopy(NDEF_message, 0, NDEF_message_data, 0, NDEF_message_length);
        String NDEF_message_data_str = new String(NDEF_message_data);
        String NDEF_str = null;
        posApiHelper.SysBeep();
        callbackContext.success(ByteUtil.bytearrayToHexString(NFC_UID_data, NFC_UID_data.length));
        return true;
        }
    /*    if (!TextUtils.isEmpty(NDEF_message_data_str)) {
            NDEF_str = NDEF_message_data_str.substring(NDEF_message_data_str.indexOf("en")+2,NDEF_message_data_str.length());
        }*/

      
           /* posApiHelper.SysBeep();
            //successCount ++;
            if (!TextUtils.isEmpty(NDEF_str)) {
                textViewMsg.setText("TYPE: " + new String(Technology).substring(0, TechnologyLength) + "\n"
                        + "UID: " + + "\n"
                        + NDEF_str);
            }else{
                textViewMsg.setText("TYPE: " + new String(Technology).substring(0, TechnologyLength) + "\n"
                        + "UID: " + ByteUtil.bytearrayToHexString(NFC_UID_data, NFC_UID_data.length));
            }*/

        

        
    }
      } catch (Exception e) {
      callbackContext.error( "NFC red error");
        return false;
        }
  callbackContext.error( "NFC timeout");
        return true;
    }
}
