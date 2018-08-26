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

        String base64String = "data:image/png;base64,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";
        String base64Image = base64String.split(",")[1];
        
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
         ret = posApiHelper.PrintBmp(bmp);
                      if (ret == 0) {
                            posApiHelper.PrintStr("\n\n\n");
                            posApiHelper.PrintStr("                                         \n");
                            posApiHelper.PrintStr("                                         \n");
                        posApiHelper.PrintStr("法语:Bonjour! Ça fait longtemps!\n");
                        posApiHelper.PrintStr("日语:こんにちは！久しぶり！\n");
                        posApiHelper.PrintStr("俄语:Привет! Давно не виделись!\n");
                        posApiHelper.PrintStr("韩语:안녕하세요! 긴 시간은 더 볼 수 없습니다!\n");
                       
                        posApiHelper.PrintStr("法语:Bonjour! Ça fait longtemps!\n");
                        posApiHelper.PrintStr("日语:こんにちは！久しぶり！\n");
                        posApiHelper.PrintStr("俄语:Привет! Давно не виделись!\n");
                        posApiHelper.PrintStr("韩语:안녕하세요! 긴 시간은 더 볼 수 없습니다!\n");
                          
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
                              
                        } else {
                               callbackContext.error("Lib_PrnBmp Failed" + ret);
                                return false;
            
                        }
                  

                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x33);
                        posApiHelper.PrintStr("AYA PAY POS SALES SLIP\n");
                    //    posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x00);
                      //  posApiHelper.PrintStr("商户存根MERCHANT COPY\n");
                        posApiHelper.PrintStr("------------------------------\n");
                        posApiHelper.PrintStr("------------------------------\n");
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
                        posApiHelper.PrintStr("                                         ");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                       
               
                    
                      


                      

                     

                
                m_bThreadFinished = true;

            callbackContext.success("Data Sent");
            return true;

        } catch (Exception e) {
            String errMsg = e.getMessage();  
            callbackContext.error(errMsg);
        }
        return false;
    }
    
}
