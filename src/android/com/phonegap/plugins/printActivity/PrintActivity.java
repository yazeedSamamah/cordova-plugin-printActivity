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

import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;


import android.R;
import android.os.RemoteException;
import com.google.zxing.BarcodeFormat;

import java.util.Timer;
import vpos.apipackage.PosApiHelper;
import vpos.apipackage.PrintInitException;




import android.app.Notification;
import android.app.PendingIntent;




import vpos.apipackage.ByteUtil;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;


import com.sunmi.TransBean;
import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

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
     private Activity activity;
    private IntentFilter filter;
    private int voltage_level;
    private int BatteryV;
    SharedPreferences preferences;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
     
    private IWoyouService woyouService;

    private ServiceConnection connService = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            woyouService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            woyouService = IWoyouService.Stub.asInterface(service);
        }
    };

          ICallback callback = new ICallback.Stub() {

        @Override
        public void onRunResult(boolean success) throws RemoteException {
        }

        @Override
        public void onReturnString(final String value) throws RemoteException {
        }

        @Override
        public void onRaiseException(int code, final String msg)
                throws RemoteException {
        }
    };
   /* Private ServiceConnection connService = new ServiceConnection() {

@Override
Public void onServiceDisconnected(ComponentName name) {
Toast. MakeText (this, "the service disconnected", Toast. LENGTH_LONG), show ();
SetButtonEnable (false);
WoyouService = null;
 Try {
 Thread.sleep (2000);
 } catch (interruptedexe) {
 // TODO auto-generated catch block 
     E.p rintStackTrace ();
 }
 Binding ();
}

@Override
Public void onServiceConnected (the ComponentName name, IBinder service) {
 WoyouService = IWoyouService. Stub. AsInterface (service);
 SetButtonEnable (true);
 Try {
 ServiceVersion = woyouService. GetServiceVersion ();
Info. SetText ("service version :" + serviceVersion + "\n");
 } catch (RemoteException e) {
 // TODO auto-generated catch block
 E.p rintStackTrace ();
 }
}

Private void Binding () {
Intent Intent = new Intent ();
Intent. SetPackage ("woyou.Aidlservice.Jiuiv5 ");
Intent. SetAction ("woyou.Aidlservice.Jiuiv5.IWoyouService ");
BindService (intent, connService, Context. BIND_AUTO_CREATE);
}*/
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
             readNfcCard(callbackContext);
            return true;
        }else if (action.equals("efawateerPrint")) {
              try {
      /*if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return false;
        }*/

        printEfawateer(callbackContext,args);
         
           } catch (IOException e) {
           //test
               Log.e(tag, e.getMessage());
               e.printStackTrace();
           }
            return true;
        }else if(action.equals("testPrint")){

       printPos(callbackContext);
 
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
  boolean printPos(CallbackContext callbackContext , JSONArray args) throws IOException {
      Intent intent = new Intent();
     try {    intent.setPackage("woyou.aidlservice.jiuiv5");
        intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        activity.startService(intent);//启动打印服务
        activity.bindService(intent, connService, Context.BIND_AUTO_CREATE);
 
       
                woyouService.printerSelfChecking(callback);//这里使用的AIDL方式打印
                            callbackContext.success("print  success");
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
  callbackContext.error(" fail,  " +  e.printStackTrace());
                return false ;
            }
        }
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
                        posApiHelper.PrintStr("CLIENT ACCOUNT #N:");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr(args.getString(9));
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
    

     boolean readNfcCard(CallbackContext callbackContext){
          long time = System.currentTimeMillis();
                while (System.currentTimeMillis() < time + 10000) {
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
     callbackContext.error( "NFC timeout");
        return true;
    }
     boolean printEfawateer(CallbackContext callbackContext , JSONArray args) throws IOException {
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
                        posApiHelper.PrintStr("BILLING NUMBER #N:");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr(args.getString(9));
                        posApiHelper.PrintStr("\n");     
                        posApiHelper.PrintStr("SERVICE TYPE:");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr(args.getString(10));
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
}
