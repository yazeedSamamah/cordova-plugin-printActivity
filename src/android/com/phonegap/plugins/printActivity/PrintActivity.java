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

        String base64String = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAYAAAAH0CAYAAAA0dPpoAAAgAElEQVR4Xu2duZUlSXJFI3WZAgERsiWYAgMKImSR3QKABDdMNzklAigwqJagSgQw1bp8nJ/rXzzCzTdzM/M7xMw5Ux6+3Gduz5f4kQ/btp2283+dnv+H/0AAAhCAQEACDw8P76N6y/fn/+c982MCAVVnSBCAwPIELpP/G4xzvscAlg8NAEAAApEJpJL/26nPlQFwFBQ5DBgbBCCwIoEiA8AEVgwRxgwBCEQkcJT8z+O92wFcng9FBMKYIAABCKxAIJf8Dw2AncAKIcIYIQCBiAT2kv9tXn84nU4niVNEhMSYIAABCEQkIM3pzwbwvBW4eEf0EgivhkYMD8YEAQhEJVCSy98NABOIGg6MCwIQWIVASfJ/zvlvO4AjA+A+YJXwYZwQgIBXAtJz/8vxXRkAuwCv0tNvCEBgZQI1yf9uB/AGsHQbsTJ4xg4BCEBgNoHanH23A8AEZktJ+xCAAATkBGqT/+4OgPsAOXxKQgACEJhFoCX5HxoA9wGzJKVdCEAAAnkCtef+lzXvHgFxFJQXgBIQgAAEZhFoXf1ndwCYwCxpaRcCEIDAPoEeyb/ZAM4V8EthwhQCEICAHoFeyV9sAEf3AZiAnvC0BAEIrE2gx7l/0R3AVWG+F7R29DF6CEBgKoGeq/+iHQD3AVN1p3EIQGBxAr2Tf5UBHB0HcR+weIQyfAhAYAiBEcm/uwFwHzBEeyqFAAQWJtD73L/6DoD7gIWjkKFDAALqBEYm/+odAPcB6nFAgxCAwIIERh39vOfwy78HUMN3dAdr+sQzEIAABLwT0Mit2U9B5CCO3qLk2uffIQABCEQjoJH8m4+AckdB53/nzaBoocl4IACBkQQ0F9XNO4CcCWAAI0OFuiEAgWgEtFb/3XYAmEC0EGQ8EIDADAKayb+7ATxXyOciZsQNbUIAAs4JzMid3Y6AcrsA7gOcRyfdhwAEhhHQPPe/HER3A2AXMCxGqBgCEAhKYMbqf8gRUG4nwKVw0AhmWBCAQBWBWcl/qAGwE6iKBR6CAAQWIjAz+U8zAO4DFopwhgoBCCQJzDr3H34HcNUAbwUR/hCAAATuCMxe/Q/fAXAfQNRDAAIQuCdgIfmrGQD3AUwBCEAAAi8ErCR/EwbAfQDTAgIQWIWApeSvagBHzocJrBL+jBMC6xKwcOl7S3/ID8GOJLbmgOuGIyOHAAQ0CVjMfeoGYO0MTDMAaAsCEFiTgMXkr34EdCm9VSBrhiejhgAERhGwnOum7AC4DxgVatQLAQhYImDx3P9qId76N4FbYFt2xpZx8SwEIAABD8fd03YAb+GBCTBRIACBiAQ85LbpBuDBJSMGJ2OCAATGEfCQ/KdeAksuhM9l+Hz0uCClZghAoD8B6+f+Zu4AMIH+wUeNEIDAXAJeVv9mdgDcB8wNWFqHAAT6EPCU/M0ZAPcBfYKQWiAAAX0C3pK/SQPABPQDlxYhAIE2Ah6TvzsDOHeYS+G2QOVpCECgLwFPl763IzfxGmhKDq+O2je0qA0CELBOwHOuMmsAHAVZD3v6BwEIeE7+Zo+ALsPKO2CmCAQgEJNAhNxkegdwtAvgPiDmpGJUEPBAwPO5/9UCe+bH4KRCR3Da97H+9cf2y6ffth9Xg3/avp3+uf1dCiRR7q8/ftk+/XZT67fT9s/qSv/a/vjl03ZT5fbUVGfDABsfTfF5r/Lx9+3n91+3vzW2cff4n1+2h89fd2p93H7/+X37tXujt82lddxGjbk3Q6P1RclJ5ncAb/pHAb5tf25fHj5v12mhNRnsTPKnb9up2gFS/Ww3Kt35vMNltxOtOrxU/OeXh2037yfafvz95/Z9kBPsGh8GUB2KcXLRtrkxgKPjIG+vhqYSRFsSSCXrM7GGhJ1avTYZSvV8q3vwcPWdqbJ2nMndnbD7IxLyUX9GtCccqudikZL/c071cASU2wWc/92VCfROrrvJrn5Fm1o5tpmU4rRvScSv3Swea4c2+x7LZHY/GEBxQEY5978cuCsDONoFuDKBzvcAR0cOxYnsOTpSyaPeTIpnWusDd3z3d0JH7OT3HXs7sPMmLHUMt1++Tq97YNcG/rg9Pv7YflxeEWEAxVEWbfXvbgeQ2wn42QX0vGDNrPSqjjMSCcpTwng3AOkR2E5CFrJLn7Pn2659Lpu5bg3w6dv2bft8fS/hSc/sgMcXiJj83RpAhPuAbkcst5P98XF7/PHj4i2jfCK6mz69j6jGz8/rFs5M/vEv2/eSC/DqXVnLbqnnQuANwW2dLzu3f/nHzcU0BiCOyqjJ37UBuDeBVMKpmJS3RvL49LRtX79evWYqP8p4mRP9XykVz7WpBe+PgwTHXikdhTuHEazv4uH1DaO7sVXE2lRxJjUe8dzf9R3AVecfHnbDwv5xUJ/XLG8n9tO3b9v2+fo107Jz5dSqtGIXMWnCtjRbZXwJAyjindhtFT1/OeCDuw8MoDwyoid/9zsA77uA1AVk2Wr91kR6bPedn/+Xz/OPJxLJOKtHqwEkns+2uTPG+8XAxw8BMYDywIh89PNGw91bQCkZ3QrVetZ++/zrtn67+1VwwQq+54q0fM5NfeJ+BzD+CGi74y1oM0Xptp6bYygMoCy03OaUsmH6+h3A0dh8Cta22r5LWG+TvmFVWZUEC4POavH7HZnEOFuOzFqevaR4G0f3/cYA5FHnM5fIxxfmDsD/fUDfN0g+jg4SxiK6mOyVkOqCce5TtczSl+bp9/9vRthpt3V09PPWIgYgi64Vzv1DGsDRfcD536xeClddPJ4HlPmxU92Er0+Csullt1Tbzif9OufhZW6nt8DujpB23u6piwe7eo3q2Uqr/+ec6elTEBLR3QlY+xrhzvn/+8cla86WO61IJTpZKtN+GZ8y5NcRJhJy+gdgNWf/6ZcAUt+VwwDyEecud+SHlC0RzgCOdgI2dwF1r4PeTui71WbFPUDbKvjgcwjZMJQUqEmQR/Xu/4K6+jXM5JdeX/vw9G37+a//dffJ7pd/ldw13I8lGwMXj2AAxzG2YvIPuQN4k9mToOU/QJKs/BIJ7vAeIFG+6MdCFg2gtE91ifg2tRR9Dlp0N5NIXrkd4M0jGMC+Aax27n9JIuQO4GgXcP43czuB0qMX4cfO7lf0Rwmu9fy/NNlKVv2XZWp2AMI+1SbhwyGM/Brn/bhyvx3AAMoNwFyeKJ0ygvJhDcDXUVBZ8t19/fN+KXrzF6kOkmjNj6Cu2hMmW0FQposMNID3BmvauO/t4V8fuy1etMt6eVisP0dA2WjzdFKQHUxFgdAG4McESl6/vC+7f2Z9n5T3ypbtFioibcojtaZUeRTU8DcBxPcOwt1f9liqwnimSDiw0dWT/3N+jPYWUCpePAgtfx1Ucv7/RkF6D9B6/j9wlo6suuPf7N1f9d+YyVGb2aR8r1Pu6OcNH0dA14G08rn/JYmlDeAMwsw5n/SbMoWXf7KVvXynMDIfz6t777xeuBPYSepHyXnXMA7uI2qOfjCA+6gi+X8wWcIAfBwFyT4LcbeSy11gSl4HrfnNwLxsPazl5Ns7FXy3TXiXsHNklDwOuisrbOOVFjuAi6S38xVhM4vBYRGeMMMVjoDehm39KCj/LZqaI4D86l62S1CMymlNlX+ao/wV3pvBif4QTcm9TxoeBvDCxXoO0A79ZXYALkwg9ybOkAtA6T2BdmjOaU9+F3PuX9nbW3sjyv0SueitogZs4ovohjZmPkryX3wHcLQCsHEfcJxQ7hJB9tLwff9/8zro5dl2+TvlMyfx8LaldzHP+f/LDdfz34D/+Aa/uK+ZNjEAMcndgpz7p9EstwOwbQLHb+OU/PT/Su6je4Bu5/+1r1xKJ3fZmbe01rtyBQbQ7+is0PirB3f8YOQdAKt/DOCKgNWA2P8eT8nrn7di7x/zVO8q7uIJA6j9pk/uKIkdQJvjWZ3rbaPq8/SSO4A3dCYDY2+1vt0eNwhfUXwdbDrR/9v2v7982n778RFM9avAGAZQcgfQ9vG8ywmcv6hvne6rXgKbnOOtYnZ8fmkDODoOmvdKWPo44O5LkrnXE2+DJHnU85/b/326/gPyVWfYz21FMICSX2Sn7wCqDLTXXcJBYljRADj3zzsFBrDzTvAZ3SwTuJ+sT9vT9nX72rRST1z2Pj1tX79+vYiSsl1FPrz0S5zZ/c+/V1zEpr6xc+7+kdEmX+EsvatIGWdpHXnOGMAHo1nzOq+SfonlDcDkLuDu176P2+OPH9tH/q9JEIl3yR8ftx8/LlyldFehH6/ZFt8TnfQNqdca059wznOufe6l2Z1d0wAdVjMAjn6yU+W5AAbwyslWwGSOUwqT21so5C4Tq44vZHGmVqr0h1lH3+4X8Tj8ANzejuroU9F506mBuZIB2JrLNWrpPYMBXLC2EziZb8nXrhAPk9WYxKMXyq9r6i8P2+fLU63aDhQwzhlrSRfq72COW1nFADj3L4k2dgBXtCwFz1FSqU8SRzsL/+f/L2JmzFMwP0Qr/9t6Dr8sKmhU+v0gSVWJMisYgKX5WymT+mPsAG6Qmwmi3dV6W6LePfIoWPGqR2lNgzUJufJo7aN7lW9CKbBf2QC49N2fQBhAgo2No6CdZNKapCo+XVyTf+08k0vKbYa6N87ssVCrjoWAoxuAjTlbKIqB4hjAjggElIHopAsQEBBgrgog7eW5lT4HXYqJwColRnkI6BIwc2SrO+xurbEDOEBJcHWLMyqCwBACLNLasGIAGX4EWFuA8TQERhFgbraTxQAEDAk0ASSKQECRAHOyD2wMQMiRgBOCohgEBhPgaLYfYAxAyJKgE4KiGAQGE2Ax1g8wBlDAksArgEVRCAwgwBzsCxUDKORJABYCozgEOhFg7nUCeVENBlDBlECsgMYjEGggwBFsA7yDRzGACq4EYwU0HoFAJQHmWyU4wWMYgABSqghBWQmOxyBQSIAddyGwguIYQAGs26IEZgM8HoWAgABzTACpoQgG0ADv/CgB2giQxyGwQ4C5NT40MIAOjAnUDhCpAgIXBDhi1QkHDKADZ4K1A0SqgIDAAPjjLn3DBAPoxJNdQCeQVLM8AeaSXghgAB1ZE7gdYVLVkgSYQ7qyYwCdeRPAnYFS3TIEOErVlxoD6MycIO4MlOqWIcDiSV9qDGAAc0xgAFSqDE2A5D9HXgxgEHcCehBYqg1HgLkyT1IMYCB7AnsgXKoOQYDd8lwZMYDB/DGBwYCp3i0Bkv986TCAwRoQ5IMBU71bAiyO5kuHAShoQKArQKYJVwSYEzbkwgCUdCDglUDTjHkCzAU7EmEAiloQ+IqwacokAY5EbcmCASjqQfArwqYpkwRYBNmSBQNQ1oMJoAyc5swQIPbNSPHeEQxggiZMhAnQaXIqAWJ+Kv7dxjGASbowISaBp1l1Ahx9qiMXN4gBiFH1Lcik6MuT2uwSYLFjWJsTf2JnmjqYwDT0NKxEgOSvBLqyGXYAleB6PcYE6UWSeqwRILatKXLfHwzAgEZMFAMi0IWuBNjddsU5rDIMYBjasooxgTJelLZLgORvV5vbnmEARrRi0hgRgm40E2Ax04xQrQIMQA11viEmTp4RJWwTIIZt68MOwLg+TCDjAtG9XQLErr/gYAdgUDMmkkFR6NIhAY4wfQYIBmBQNyaTQVHoUpUB8DMj24GDARjVBxMwKgzduiPAjtVvUGAAhrVjYhkWh649EyBGfQcCBmBcPyaYcYEW7h67VP/iYwAONMQEHIi0YBeJS/+iYwAONGSl5UCkxbpI8o8hOAbgREcmnBOhFugmsRhHZAzAkZZMPEdiBe0qu9FYwmIAzvTEBJwJFqi7JP9AYr4OBQNwpimT0JlggbrL4iOQmBiAXzGZiH6189pzYs6rcsf9ZgfgVFcmpFPhHHabWHMomrDLGIAQlMViTEyLqsTqE0eOsfS8HQ0G4FhfJqdj8Zx0nUWGE6Equ4kBVIKz8hgmYEWJeP0g+cfTlB1AQE2ZqAFFnTwkYmqyAErNswNQAj26GSbsaMLr1M+uciGtT/zFhjBqYwJhpJw6EOJoKn7VxtkBqOIe2xgrt7F8V6id5L+Cyh9jxACC6c0EDiao4nCIHUXYRprCAIwI0bMbTOSeNNeoi93jGjrzFtAiOmMCiwjdYZgk/w4QnVbBDsCpcLluM6lzhPj3NwIsFtaNBQwgsPaYQGBxOw2N5N8JpNNqMACnwkm7zQSXklqvHLGxnubcASyoORN9QdEzQ2Z3SEycCbADWCQOMIFFhBYOk3gQggpeDAMILnDuou/87/wYfJEgeB0myX8tvY9GiwEsFAtM/IXE3hkqMUAMXBLAABaLBxLAYoJfDJdz/3W13xs5BrBgTGACC4p+vvB7eEgOnCPANeOBS+BFdWcluJ7wJP/1NJeMmB2AhFLAMiSEgKJy7r+OqJ1GigF0AumxGkzAo2plfWa3V8ZrtdIYwGqK34wXE4gbACT/uNr2GhkG0Iuk03pIEk6FE3QbcxdAWrwIBrB4ADy/CbDzdsj533hDxGeAkPx96qbdawxAm7jR9kgYRoWp6BZaVkBb9BEMYFHhU8MmcfgPBnZz/jXUHAEGoEnbQVuYgAORDrqIfr710+49BqBN3Hh7rCCNC0Ty9yuQwZ5jAAZFmd0lVpGzFShvH83KmfEEfw+AGNghQELxExrs2vxoZa2n7ACsKWKoP5iAITE4+vEhhrNeYgDOBNPsLitLTdp1bWHSddx46oUABkAkHBLABOwGCMnfrjZeeoYBeFFqYj9JNBPhF97RnIvz6217elntEQZgVRlj/cIE7AjCrsyOFt57ggF4V1Cx/5iAImwufW3ADt4LDCC4wD2Hx8qzJ826ujDhOm48lSaAARAZRQRIQEW4uhaGfVecVMZbQMRADQESUQ21tmfYfbXx42l2AMRARwKYQEeYgqrgLYBEkWICHAEVI+OBMwFWpHpxQPLXY71aSxjAaop3HC+JqSPMnapgPJ7xyi1gACur32HsJKgOEAuT/7k4P/Yax32lmjGAldQeNFZMYAxYuI7hSq0fBDAAoqGZAPcBzQjvKiD592dKjfcEMACiogsBTKALxudKSP79WFLTMQEMgAjpRoDE1Y4SI21nSA1yAhiAnBUlBQQwAQGkgyLwa+PH02UEMIAyXpQWECCJCSAlisCtjhtP1RPAAOrZ8eQOAY4xykOD5F/OjCfaCWAA7QypgdVsUwyQ/Jvw8XADAQygAR6PHhMgseUjhN1SnhElxhHAAMaxpWZeaczGACaZRUSBgQQwgIFwqZqPxh3FAMmfGTKbAAYwW4EF2ueY415kkv8Cge9giBiAA5EidJGE96EihhghomOMAQOIoaOLUWACLzLBwUW4LtFJDGAJme0McvXkt/r47UQiPXlejJz4sDiRoEhg5eMPkr9ioNGUiAAGIMJEoZ4EVkyEKxtfz9ihrr4EMIC+PKlNSGA1E1htvMIwoNhkAhjAZAFWbn6VpLjKOFeOZa9jxwC8Kheg3ysci5D8AwRq4CFgAIHF9TC0yAky8tg8xBZ9zBPAAPKMKDGYQMREucLuZnBYUL0CAQxAATJN5AlEM4Fo48krSAmPBDAAj6oF7HOkFTPJP2CABh0SBhBUWI/DimACJH+PkbdunzGAdbU3OXLPCTSCgZkMCjo1jAAGMAwtFdcS8GoCXvtdqxPP+SeAAfjXMOQIvCVTb/0NGTQMqpgABlCMjAc0CHg6TiH5a0QEbYwggAGMoEqdXQh4SKyejKqLKFQSigAGEErOeIOxbgLW+xcvIhhRTwIYQE+a1DWEgNUka7VfQ0Sg0pAEMICQssYalMVjFpJ/rBhbdTQYwKrKOxu3JRMg+TsLHrq7SwADIDjcELCQeC0ZkRvh6KhZAhiAWWnoWIrAbBOY3T5RAYGeBDCAnjSpS4XArCQ8q10VqDSyJAEMYEnZfQ96xjEMyd93zND7NAEMgMhwSUAzIc8wHJei0Gl3BDAAd5LR4TcCWiag1Q7KQkCbAAagTZz2uhIYnZxH198VBpVBoJAABlAIjOK2CIw8niH529Ka3vQngAH0Z0qNygRGJOqRxqKMh+YgsEsAAyA4QhDobQK96wsBmUGEI4ABhJN03QH1Stq96llXCUbuhQAG4EUp+pkl0OPYhuSfxUyBQAQwgEBiMpRtazGBlmdhDwGPBDAAj6rR50MCNat4kj9BtSIBDGBF1RcYc6kJlJZfACFDXIAABrCAyKsOUZrUpeVW5ci44xLAAOJqu/zIJMc6JP/lw2RpABjA0vLHH/xRgpcYRHxCjHBlAhjAyuovMvajRJ9CcDqdFiHDMFcngAGsHgGLjF9qAiT/RQKCYT4TwAAIhCUISAyA5L9EKDDICwIYAOGwDIGcCWAAy4QCA30lgAEQCksR4K2fpeRmsBkCGAAhsgwBdgDLSM1AhQQwACEoivkmkEv+b6PjGMi3zvS+jAAGUMaL0g4JSJM/JuBQXLrcRAADaMLHw9YJlCb/83jYBVhXlf71IoAB9CJJPSYJ5C59c/9uclB0CgKdCGAAnUBSjT0C0uQuLWdvhPQIAm0EMIA2fjxtlEBJUuebQEZFpFvDCWAAwxHTgDaBmoReYhja46E9CIwigAGMIku90wjUJvPa56YNlIYh0EgAA2gEyOO2CLQm8dbnbdGgNxA4JoABECFhCPRI3jXHR2EAMpDlCGAAy0kec8A9E3fPumLSZlRRCGAAUZRcfBw9Vv+XCHvXt7g8DN8oAQzAqDB0S05gVLIeVa98ZJSEwFgCGMBYvtQ+mMDoJD26/sF4qB4ChwQwAALELQGNs3qNNtwKQMfdE8AA3Eu45gA0EzO7gDVjbIVRYwArqBxwjNpJWbu9gJIxJIMEMACDotClYwKzkvGsdokHCIwigAGMIku9QwjMTMKax05D4FEpBG4IYACEhBsCFhLwTANyIxQddUMAA3AjFR21knyt9IOIgEArAQyglSDPqxCwlnSt9UdFBBoJRwADCCdpvAFZTbZW+xUvAhjRKAIYwCiy1NuFgIVz/72BWO5bF/hUEp4ABhBeYt8DtL7Ktt4/3+rT+9EEMIDRhKm/moCX5Oqln9VC8GBYAhhAWGl9D8xbUvXWX9/RQe97EcAAepGknm4EPJ6te+xzN8GoyC0BDMCtdDE77jmRsguIGZORR4UBRFbX4di8J1Hv/XcYMnS5gQAG0ACPR/sSiJI8o4yjr7rUZpEABmBRlQX7FClpej7GWjD0lh4yBrC0/DYGHzFhRhyTjWihFz0JYAA9aVJXFYFIq/9LAFHHVSUyD5kkgAGYlGWdTkVPktHHt06kxhwpBhBTVxejWiU5rjJOF0FHJ68IYAAExBQCK52RrzTWKcFEo9UEMIBqdDzYQmC1VfFq422JDZ7VI4AB6LGmpVcCqybDVcdN4NslgAHY1SZkz1ZPgquPP2RQOx4UBuBYPG9d5yx822DgLWpj9xcDiK2vmdGR+D6kYBdgJiyX7wgGsHwI6AAg6V1zhodO3NHKMQEMgAgZToBkl0YMl+GhRwMZAhgAITKUAEkuswJ7eEgWOJ1OQ3WhcgicCWAAxMEwApz759HCKM+IEuMIYADj2C5fM6t/WQjAScaJUv0JYAD9mVLjeWvJ0UZRHMCrCBeFOxHAADqBpJoPAiSzumiAWx03nqongAHUs+PJBAHOtOvDAnb17HiyjgAGUMeNp3YIsIptCw34tfHj6TICGEAZL0ofECB59QkPOPbhSC15AhhAnhElBARIWgJIBUXgWQCLotUEMIBqdDz4RoCz6/6xANP+TKnxngAGQFQ0ESBRNeE7fBi249hS8wsBDIBIaCLAUUUTvuzD8M0iokADAQygAd7qj5KcdCIAzjqcV2wFA1hR9Q5jJil1gFhQBbwLYFFUTAADEKOi4BsBzqb1YwHm+sxXaBEDWEHlzmNkNdoZqLA6uAtBUUxMAAMQo6LgmQBJaG4cwH8u/2itYwDRFB04HpLPQLgFVaNDASyKHhLAAAgQEQHOoEWYVAqhhQrmJRrBAJaQuX2QrDrbGfasAT160ly3LgxgXe3FIyfZiFGpFkQXVdwhG8MAQsrab1AkmX4sR9SEPiOorlMnBrCO1sUj5ay5GNmUBzCBKdhDNIoBhJCx/yBI/v2ZjqoRrUaRjV8vBhBf46oRsqqswjbtIfSaht51wxiAa/nGdJ5kMobr6FrRbTThePVjAPE0bRoRSaQJ3/SH0W+6BK46gAG4kmtsZzlLHstXo3Y01KAcpw0MII6WzSNh9diM0EQF6GhCBhedwABcyDS+kySN8Yw1W0BPTdp+28IA/GrXrecki24oTVWErqbkMNkZDMCkLHqd4sxYj7V2S2irTdxfexiAP8269phVYlec5irDBMxJYqpDGIApOXQ7Q/LX5T2rNXSeRd5+uxiAfY2G9JCkMASr2UrR26w0UzuGAUzFP6dxjgXmcJ/dKiYwWwF77WMA9jQZ2iOS/1C8pitHe9PyTOkcBjAF+7xGWQXOY2+hZfS3oIKdPmAAdrQY3hMm/3DELhogDlzIpNJJDEAF8/xGmPTzNbDUA+LBkhrz+oIBzGOv1jJnv2qo3TRETLiRamhHMYCheG1UzmrPhg7WekFcWFNEvz8YgD5z1RaZ5Kq43TVGfLiTrGuHMYCuOG1VxuS2pYfV3hAnVpUZ3y8MYDzjKS1wxjsFu9tGMQG30jV1HANowmf3YSa0XW0s9owFg0VVxvcJAxjPWL0Fkr868hANEjchZCwaBAZQhMt+YSaxfY0s95D4saxO/75hAP2ZTquRbfw09KEaxgRCyXk4GAwgiNYk/yBCGhgGsWRABKUuYABKoEc3w6ptNOG16iee1tAbAwigM5M1gIgGh0BcGRSlc5cwgM5AtatjkmoTX6s94iu23hiAY305q3UsnpOuE2NOhKrsJgZQCc7CY6zOLKgQvw+YQFyNMQCn2pL8nQrntNvEm1PhMt3GABzqymR0KFqALhN3AUS8GQIG4ExTtuPOBAvWXdS2iAAAAA2YSURBVEwglqAYgDM9mYDOBAvWXRYgsQTFABzpSfJ3JFbgrhKHccTFAJxoyaRzItQi3SQeYwiNATjQkW23A5EW7CIm4F90DMC4hiR/4wIt3D1i07/4GIBxDVllGRdo8e4Rn74DAAMwrB+Ty7A4dO2dAHHqNxgwAKPaMamMCkO3kgSIV5+BgQEY1I2zVYOi0KUsAUwgi8hcAQzAnCTbxkQyKApdyhJg4ZJFZK4ABmBMEpK/MUHoThEB4rcI1/TCGMB0CT46wOQxJAZdqSZAHFejU38QA1BHnm6Q7bMRIehGFwKYQBeMwyvBAIYjljXAhJFxopQPAixonOh0Op1OProat5ck/7jarjwy4tq++uwAJmvEJJksAM0PJUB8D8XbXDkG0IywvgK2yfXseNIPAUzArlYYwERtmBgT4dO0GgEWOmqoixvCAIqR9XmA5N+HI7X4IIAJ2NQJA5igC8l/AnSanE6AuJ8uwV0HMABlTZgEysBpzhQB4t+UHBsGoKgH22BF2DRllgAmYEcaDEBRCwJfETZNmSXAQsiONBiAkhYkfyXQNOOCAPPBhkwYgIIOBLsCZJpwR4B5MV8yDGCwBmx3BwOmetcEMIG58mEAg/kT4IMBU71rAiyQ5sqHAQzkT/IfCJeqwxBgnsyTEgMYxJ6gHgSWakMSYL7MkRUDGMCdbe0AqFQZngAmoC8xBjCAOYE8ACpVLkGAuaMrMwbQmTcB3Bko1S1FgN2zrtwYQEfeJP+OMKlqWQLMIz3pMYBOrAnaTiCpBgLbtjGfdMIAA+jAmW1rB4hUAYEbApjA+JDAADowJlA7QKQKCAgN4FzsdDrBqwMBDKARIsm/ESCPQ+CAAPNrbHhgAA18Cc4GeDwKASEB5pkQVEUxDKAC2vkRzv0rwfEYBCoIYAIV0ASPYAACSKkiBGQlOB6DQAUBFlwV0ASPYAACSLdFSP4V0HgEAo0EMIFGgInHMYBCpiT/QmAUh0BHAsy/jjDPR9kn3qcSE2UFIkZFQQgMI4AJ9EOLARSwJPAKYFEUAgMJMBf7wMUAhBwJOCEoikFAgQC78T6QMQABR5K/ABJFIKBMgHnZDhwDyDBkpdEeZNQAgVEEMIE2shjAAT+Sf1tw8TQENAhgAvWUMYAKA+DFqfqA40kI9CbAQq2eKAaww45VRX1Q8SQEtAkwX+uIYwAJbgRTXTDxFARmEmDeltPHAG6YsZ0sDyKegIAVAphAmRIYgNAAOPcvCyxKQ2AWAUxATh4DuGBF4MgDh5IQsEqAXbxcGQzglRXJXx40lISAdQLMZ5lCGAB/3EUWKZSCgDMCmEBeMAzgwAA4988HECUgYJkAJnCszvIGQIBYnr70DQJtBLgPwAB2CZD82yYXT0PAAwHm+b5Ky+4AWBl4mLr0EQJ9CGACaY5LGgDJv8+kohYIeCKACdyrhQFcMOHS19N0pq8QKCPAwg8D2FgFlE0aSkMgEgFM4FrNpXYAJP9IU5mxQKCOAHngg9syBoDz100WnoJARAKYwIuqyxsA5/4RpzdjgkCeACawiAEgdH4yUAICqxHgVGABA5ie/P/8sj18/no1t56+nbZ//r3DdBtZd4fulVTx55eH7QZTyeOvZR+3339+3379W8Wjz4/8uX15+Lxdq/VW19P27fTPrYdsH73Tbq+WS9znpueHyWhDHwGZcPiRSXpk3cqBOdUAEhz3h99qMmefuV8UDG1PWUtvza1sAksagOq5/8gkPbJu5Vk8ywBq2338/ef2vWKrod2esoxum1vVBMIagBlBRybpkXUrT+XaxHjdzbLVeWubpUd52u0pS+i6OROnBRMIhjQAM8n/+ViZOwBJXN8mx9oVtqStc5m//vhl+/Tbj/vij79vP7//ul1dI+we2cjvBbTbk3Kg3AcBU3lDSZhwBmDOyTEAUSirGsBff2y/fPptu03/xyv6nQvbp2/bKXejr92eiDiFUgRWM4FQBmAu+bMDEGcZTQNIHcWIjnOSiTx/7KTdnhg6BZMEVjKBJQxA9dL3NqTYAYjSjJ4BJFbyqWOfnV6njnKOj6u02xPhptABAZMLyUGKhTEAs66NAYhCV8sAUglctPp/G0VqF3BgINrtiWBTKEtgFRMIYQBmkz9HQNmJ9lZAxwD+2v745dN2ffcrv8jd6+u27R0Dabcnxk1BAQHTeUXQf0kR9wZg3qnZAUjicNMxgMRxjOQS92YE8lW9dnsi1BQqIBDdBMIawNRz/8sAwwBE003FABLHN1Wvm0rrkZbLEepVT64d/n25S2HXBuDCnTEAUVrRMID7lXv+DZ5052Ure+32RKApVEzA/ClD8Yg+HnBrAC6SP3cA4tB0bwCJi+ChBlDw5pJYBAruEnCTbwo1dGkArhyZHYAoJIs+k1CZ/O7bKL8AfhlM4nI30Sft9kSgKVRNIKIJhDIAM+f+Lu4Ajj5FXD1HLh4sO14pMoDLVsQfZZMlbdnIJW/3aLcn6zml2ghEMwF3BuBOgKJP/7YF5/lp+TvtMQzghZhkJd8zIZ8/8XT79wtu+6DdXnvsUEOegKvTh/xwfP1JSHfJf+cOQKBLdZE1DeCMK7fj0E7I2u1VhwwPFhKIZAJudgBuobMDKJxeR8Vzu5ajnYB2QtZuryNmqsoScLkYTYzKhQG4Tf6mdwDZGDdcIHUG/9Ld/ff6tROydnuG5QratQgm4NoATF763gY7bwENm/7pi+O9XYB2QtZubxhmKj4g4N0EzBuAd8D8QZiR+SO9E0jfg/RMyJK6JGWkbHrWJW2TchICrk8nts32JbD75L9zBCS/qM2E4MjdhST6LZRJMEgfA/VMopK6JGWkAHvWJW2TclICnvOU2R2Ad2d9D56RSXpk3dLon10u9XnmnQ+85V/dlA5G9ikI7fakvadcfwJeTcCdAbg497+Mr5FJemTd/efIoBrlf3Bl5KcZUrsO7fYGAaZaIQGPJmDSADyC3I2RkUl6ZN3CoJ9frMUASn44dzHSxK4jdawn/2x0hqKwvflarN0Dj6cW5gwgVPI3fQeQe6e+dTLnfpjVWv/b87LjmOfSnT6rLF7Za7fXCyn1VBPwlr9MGYBHB81GyshVelPdQQygKMnKdwtHut6d7e9+nE67vWw0UkCBgCcTcGEA7s79XdwBxDCA0mOW+4vZ0p1KwY5jS30zaGx7CvmNJgQEvJiAGQPwAkyg/XWRplV6prWmuiMYQGoMmQ/DpT7NUfBnIcXHP+8nVF+2h89fr4Uc2V5xgPLACAJeTjNMGEDY5G/6DmBE2GvWufM5iGxyTZmGcFWeeuU0+7cJtNvT1IC2jgh4MIHpBuABUlOYN63SR+4AmkbV8eGXRP7f//Fz+/7r34T17u1eZIk8dWyU/5pofSLXbk8IkWIKBKwvbqcaQPjkzw5AMMVuV/JHSfz42Er+B973Pya3JXYQ6QR+9OG522FrtyfAThE1ApZNwKQBuL70vQ0rdgCZiXaQHAumqDz5v1aaOs4paG/LHv3cVKbdXslYKDucgFUTmGYAVoF0jwQMYLgBVH9bqTYplyb/NwLa7XUPZiqsJWD1tGOKASyT/DkCEs6Xyl1AbSK+6lVZ28U7jTsC2u0JJaDYcAIW8566AVh1wmHqswMoQrt33v5Rieyit6jR18L7f5h+TJva7dUw4Zm+BKyZgBkDCHXu3zdmqA0CEAhEwJIJqBqApYEHiieGAgEIOCJg6RREzQBI/o4ilK5CAAJDCVgxARUDsDLYoYpSOQQgAIECAhYWxVMNgHP/gmihKAQgEI7AbBMYbgCzBxguYhgQBCAQisDMHDnUAGYOLFSEMBgIQCAsgZlH5MMMYOagwkYKA4MABEISmLVYHmIAJP+QMcqgIACBgQRmmICqAXDpOzB6qBoCEHBPQNsEuhuA9gDcK84AIAABCLwS0D496WoAJH/iGAIQgEAbAc082s0AtJ2rDTFPQwACELBLQMsEhhsA5/52g4yeQQACdglomEAXA9DoqF2Z6BkEIACB/gQ0TlWaDYDk3194aoQABCBwJjDaBJoMYHTnCAEIQAACqxMYucgeYgCc+68esowfAhDoSWCUCVQbwKgO9YRGXRCAAASiEBiRc6sMYERHoojEOCAAAQiMIDDiyL3YAEZ0YgQs6oQABCAQjUDvxXeRAZD8o4UT44EABLwR6GkCXQyAS19vIUR/IQABzwR6mYDYAHo16Bk6fYcABCBggUCv0xiRAZD8LUhOHyAAAQh8EOhhAlkD6NEIokEAAhCAQH8CrYvzagPg3L+/mNQIAQhAoJRAiwkcGkBLxaWDoDwEIAABCNQRqM3VuwZQW2Fd93kKAhCAAARqCdQe1ScNoLay2s7zHAQgAAEItBGoWbQXGQDn/m0C8TQEIACBkQRKTeDOAEorGDkY6oYABCAAgTICJTn8ygBKHizrEqUhAAEIQECDQMkR/rsBlDykMQjagAAEIACBOgLSxfyzAZD86yDzFAQgAAGrBCQmcGgAXPpalZZ+QQACEMgTyJnAw7Ztp1Q1JP88XEpAAAIQsEwgd7qTNACSv2VJ6RsEIAABOYEjE8AA5BwpCQEIQMAlgT0TuDMAVv8u9aXTEIAABA4JpEzgygBI/kQQBCAAgbgEbk0g+znouCgYGQQgAIG1CGAAa+nNaCEAAQhcEbg0gf8H72uv+QqT8csAAAAASUVORK5CYII=";
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
                        posApiHelper.PrintBmp(bmp);
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
