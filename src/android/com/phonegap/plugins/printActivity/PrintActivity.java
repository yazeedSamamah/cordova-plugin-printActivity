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

               

        String base64String = "data:image/png;base64,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";
        String base64Image = base64String.split(",")[1];
        
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        String base64StringInvoice = "data:image/png;base64,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";
        String base64ImageInvoice = base64StringInvoice.split(",")[1];
        
        byte[] decodedStringInvoice = Base64.decode(base64ImageInvoice, Base64.DEFAULT);
        Bitmap bmap = BitmapFactory.decodeByteArray(decodedStringInvoice, 0, decodedStringInvoice.length);   
                       
                        posApiHelper.PrintBmp(bmap);
                        posApiHelper.PrintStr("AYA PAY POS SALES SLIP\n");
                        posApiHelper.PrintStr("- -  - -  - -\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);
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
                        posApiHelper.PrintStr("- -  CUSTOMER COPY  - -\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintBmp(bmp);

                          
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
                              
                      
                  


               
                    
                      


                      

                     

                
               

            callbackContext.success("print  success");
            return true;

        } catch (Exception e) {
            String errMsg = e.getMessage();  
            callbackContext.error(errMsg);
        }
        return false;
    }
    
}
