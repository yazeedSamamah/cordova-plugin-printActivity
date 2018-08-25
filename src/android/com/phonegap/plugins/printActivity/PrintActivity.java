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
    private RadioButton rb_high;
    private RadioButton rb_middle;
    private RadioButton rb_low;
    private RadioButton radioButton_4;
    private RadioButton radioButton_5;
    private Button gb_test;
    private Button gb_unicode;
    private Button gb_barcode;
    private Button btnBmp;
    private final static int ENABLE_RG = 10;
    private final static int DISABLE_RG = 11;
    
    TextView textViewMsg = null;
    TextView textViewGray = null;
    int ret = -1;
    private boolean m_bThreadFinished = true;

    private boolean is_cycle = false;
    private int cycle_num = 0;

    private int RESULT_CODE = 0;
    private static final String DISABLE_FUNCTION_LAUNCH_ACTION = "android.intent.action.DISABLE_FUNCTION_LAUNCH";








    /**
     * Constructor.
     */

   public PrintActivity() {
    }
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("print")) {
            try {
           /*  if (printThread != null && !printThread.isThreadFinished()) {
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
        }
        return false;
    }

    
    //private Pos pos;

    int IsWorking = 0;

    PosApiHelper posApiHelper = PosApiHelper.getInstance();

    Intent mPrintServiceIntent; 


  
   
 
    public void QuitHandler() {
        is_cycle = false;
        gb_test.setEnabled(true);
        gb_barcode.setEnabled(true);
        btnBmp.setEnabled(true);
        gb_unicode.setEnabled(true);
        handlers.removeCallbacks(runnable);
    }


    Handler handlers = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            Log.e(tag, "TIMER log...");
            printThread = new Print_Thread(PRINT_UNICODE);
            printThread.start();

            Log.e(tag, "TIMER log2...");
            if (RESULT_CODE == 0) {
                editor = preferences.edit();
                editor.putInt("count", ++cycle_num);
                editor.commit();
                Log.e(tag, "cycle num=" + cycle_num);
                SendMsg("cycle num =" + cycle_num);
            }
            handlers.postDelayed(this, 9000);

        }
    };

    Print_Thread printThread = null;

    public class Print_Thread extends Thread {

        String content = "1234567890";
        int type;

        public boolean isThreadFinished() {
            return m_bThreadFinished;
        }

        public Print_Thread(int type) {
            this.type = type;
        }

        public void run() {
            Log.d("Print_Thread[ run ]", "run() begin");
            Message msg = Message.obtain();
            Message msg1 = new Message();

            synchronized (this) {

                m_bThreadFinished = false;
                try {
                    ret = posApiHelper.PrintInit();
                } catch (PrintInitException e) {
                    e.printStackTrace();
                    int initRet = e.getExceptionCode();
                    Log.e(tag, "initRer : " + initRet);
                }

                /*Log.e(tag, "init code:" + ret);

                ret = getValue();
                Log.e(tag, "getValue():" + ret);*/

                posApiHelper.PrintSetGray(ret);

//                posApiHelper.PrintSetVoltage(BatteryV * 2 / 100);

                ret = posApiHelper.PrintCheckStatus();
                if (ret == -1) {
                    RESULT_CODE = -1;
                    Log.e(tag, "Lib_PrnCheckStatus fail, ret = " + ret);
                    SendMsg("Error, No Paper ");
                    m_bThreadFinished = true;
                    return;
                } else if (ret == -2) {
                    RESULT_CODE = -1;
                    Log.e(tag, "Lib_PrnCheckStatus fail, ret = " + ret);
                    SendMsg("Error, Printer Too Hot ");
                    m_bThreadFinished = true;
                    return;
                } else if (ret == -3) {
                    RESULT_CODE = -1;
                    Log.e(tag, "voltage = " + (BatteryV * 2));
                    SendMsg("Battery less :" + (BatteryV * 2));
                    m_bThreadFinished = true;
                    return;
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

                
                        SendMsg("PRINT_TEST");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        //0 left，1 Ringht ，2 middle
//                        Print.Lib_PrnSetAlign(0);
                        posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x55);
                        posApiHelper.PrintStr("AYA PAY POS SALES SLIP\n");
                        posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x00);
                        posApiHelper.PrintStr("CUSTOMER COPY\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                        posApiHelper.PrintStr("(MERCHANT NAME):\n");
                        posApiHelper.PrintStr("中国银联直连测试\n");
                        posApiHelper.PrintStr("商户编号(MERCHANT NO):\n");
                        posApiHelper.PrintStr("    001420183990573\n");
                        posApiHelper.PrintStr("终端编号(TERMINAL NO):00026715\n");
                        posApiHelper.PrintStr("操作员号(OPERATOR NO):12345678\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                        //  posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("发卡行(ISSUER):01020001 工商银行\n");
                        posApiHelper.PrintStr("卡号(CARD NO):\n");
                        posApiHelper.PrintStr("    9558803602109503920\n");
                        posApiHelper.PrintStr("收单行(ACQUIRER):03050011民生银行\n");
                        posApiHelper.PrintStr("交易类型(TXN. TYPE):消费/SALE\n");
                        posApiHelper.PrintStr("卡有效期(EXP. DATE):2013/08\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                        //  posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("批次号(BATCH NO)  :000023\n");
                        posApiHelper.PrintStr("凭证号(VOUCHER NO):000018\n");
                        posApiHelper.PrintStr("授权号(AUTH NO)   :987654\n");
                        posApiHelper.PrintStr("日期/时间(DATE/TIME):\n");
                        posApiHelper.PrintStr("    2008/01/28 16:46:32\n");
                        posApiHelper.PrintStr("交易参考号(REF. NO):200801280015\n");
                        posApiHelper.PrintStr("金额(AMOUNT):  RMB:2.55\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                        //  posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("备注/REFERENCE\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                        posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x00);
                        posApiHelper.PrintStr("持卡人签名(CARDHOLDER SIGNATURE)\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                        //  posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("  本人确认以上交易，同意将其计入本卡帐户\n");
                        posApiHelper.PrintStr("  I ACKNOWLEDGE SATISFACTORY RECEIPT\n");
                        posApiHelper.PrintStr("                                         ");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("                                         ");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");

                        SendMsg("Printing... ");
                        ret = posApiHelper.PrintStart();

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

                        Log.d("", "Lib_PrnStart ret = " + ret);

                        if (ret != 0) {
                            RESULT_CODE = -1;
                            Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                            if (ret == -1) {
                                SendMsg("No Print Paper ");
                            } else if(ret == -2) {
                                SendMsg("too hot ");
                            }else if(ret == -3) {
                                SendMsg("low voltage ");
                            }else{
                                SendMsg("Print fail ");
                            }
                        } else {
                            RESULT_CODE = 0;
                            SendMsg("Print Finish ");
                        }

                
                m_bThreadFinished = true;

                Log.e(tag, "goToSleep2...");
            }
        }
    }


    public void SendMsg(String strInfo) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("MSG", strInfo);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case DISABLE_RG:
                    IsWorking = 1;
                    rb_high.setEnabled(false);
                    rb_middle.setEnabled(false);
                    rb_low.setEnabled(false);
                    radioButton_4.setEnabled(false);
                    radioButton_5.setEnabled(false);
                    break;

                case ENABLE_RG:
                    IsWorking = 0;
                    rb_high.setEnabled(true);
                    rb_middle.setEnabled(true);
                    rb_low.setEnabled(true);
                    radioButton_4.setEnabled(true);
                    radioButton_5.setEnabled(true);

                    break;
                default:
                    Bundle b = msg.getData();
                    String strInfo = b.getString("MSG");
                    textViewMsg.setText(strInfo);

                    break;
            }
        }
    };

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


            Log.d("Print_Thread[ run ]", "run() begin");
            Message msg = Message.obtain();
            Message msg1 = new Message();

          //  synchronized () {

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

                /*Log.e(tag, "init code:" + ret);

                ret = getValue();
                Log.e(tag, "getValue():" + ret);*/

                posApiHelper.PrintSetGray(ret);

//                posApiHelper.PrintSetVoltage(BatteryV * 2 / 100);

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

                
                    //    SendMsg("PRINT_TEST");
                    //    msg.what = DISABLE_RG;
                    //handler.sendMessage(msg);

                        //0 left，1 Ringht ，2 middle
//                        Print.Lib_PrnSetAlign(0);
                      //  Bitmap bmp = decodeBase64(args.getString(6));
                      
                  /*     Resources activityRes =  cordova.getActivity().getResources();
                       int iconId = activityRes.getIdentifier("metrolinx1bitdepth", "drawable", cordova.getActivity().getPackageName());
                       Bitmap bmp = BitmapFactory.decodeResource(activityRes, iconId);
                          ret = posApiHelper.PrintBmp(bmp);
                          if (ret != 0){
                             Resources activityRess =  cordova.getActivity().getApplicationContext().getResources();
                              int iconIdd = activityRess.getIdentifier("metrolinx1bitdepth", "drawable", cordova.getActivity().getPackageName());
                  Bitmap bmpm  = BitmapFactory.decodeResource(activityRess,iconIdd);
                        //     callbackContext.error("Lib_PrnBmp Failed  " + bmp);
                            //    return false; 
                   //   Bitmap bmpt = BitmapFactory.decodeResource(activityRes, R.mipmap.metrolinx1bitdepth);
                        ret = posApiHelper.PrintBmp(bmpm);
                            if(ret != 0){
                                Resources activityResss = PrintActivity.this.cordova.getActivity().getResources();        
                                 int iconIddd = activityResss.getIdentifier("metrolinx1bitdepth", "drawable", cordova.getActivity().getPackageName());      
                         Bitmap bmpmm  =  BitmapFactory.decodeResource(activityResss, iconIddd);
                          ret = posApiHelper.PrintBmp(bmpmm);

                          if(ret != 0){
                //      Bitmap bmpmmm  =  BitmapFactory.decodeResource(cordova.getActivity().getApplicationContext().getResources(),R.drawable.metrolinx1bitdepth);
                 //        ret = posApiHelper.PrintBmp(bmpmmm);
                     /*    if(ret != 0){
                             callbackContext.error("Lib_PrnBmp Failed" + ret);
                                return false;  
                         }

                         int resourceID = cordova.getActivity().getResources().getIdentifier("metrolinx1bitdepth", "drawable",cordova.getActivity().getPackageName());
                         Bitmap bbicon =  BitmapFactory.decodeResource(cordova.getActivity().getResources(),resourceID);
                           ret = posApiHelper.PrintBmp(bbicon);
                           if(ret != 0){
                            callbackContext.error("Lib_PrnBmp Failed" + ret);
                                return false;  
                           }
                            
                          }
                               

                            }
                          }*/

    
                  

                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x33);
                            String base64String = "data:image/png;base64,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"
;

        String base64Image = base64String.split(",")[1];
        
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
          posApiHelper.PrintBmp(bmp);
                    //    posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x00);
                      //  posApiHelper.PrintStr("商户存根MERCHANT COPY\n");
                     posApiHelper.PrintStr("- -  CUSTOMER COPY  - -\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);
                        posApiHelper.PrintStr("COMPANY NAME : " +  args.getString(0));                   
                        posApiHelper.PrintStr("MERCHANT NAME : " + args.getString(1) );
                        posApiHelper.PrintStr("MERCHANT ID : " + args.getString(2));
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                        posApiHelper.PrintStr("INVOICE NO : " + args.getString(3));
                        posApiHelper.PrintStr("DATE : " + args.getString(4));
                        posApiHelper.PrintStr("TOTAL :" + args.getString(5) + " JD");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);
               
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("- -  CUSTOMER COPY  - -\n");
                    //   posApiHelper.PrintStr( new String(arabic.getBytes("WINDOWS-1256"),"WINDOWS-1256" ) + "\n");
                        //  posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("                                         ");
                        posApiHelper.PrintStr("\n");
                        if(args[6] == "yes"){
                        String base64StringInvoice = "data:image/png;base64,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
"
;
                        }else{
                       String base64StringInvoice = "data:image/png;base64,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
"
;
                        }
                    
                     String base64ImageInvoice = base64String.split(",")[1];
        
               byte[] decodedStringInvoice = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap bmpp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
          posApiHelper.PrintBmp(bmpp);
                        posApiHelper.PrintStr("                                         ");
                        posApiHelper.PrintStr("\n");
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
                              
                      
                      


                      

                     

                
              

            callbackContext.success("Data Sent");
            return true;

        } catch (Exception e) {
            String errMsg = e.getMessage();  
            callbackContext.error(errMsg);
        }
        return false;
    }
    
}
