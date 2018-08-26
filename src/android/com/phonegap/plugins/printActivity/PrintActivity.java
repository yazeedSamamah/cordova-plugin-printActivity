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

        String base64String = "data:image/png;base64,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Xoy///3vdpAOAOgf3OOGtPCfHf/85z9jb28vHnzwwfjwhz8cERGf/exn4/7774+333473nzzzbh+/Xq88MIL8b3vfS9OnToVX/rSl+I73/lOqe3961//ih/96Efxn//8Jz7+8Y/HmTNnkq2igFoTEbk/03hNl8z4vPTSS72I6L344otHv7t69WovInrPP//8wL/98Y9/3PvABz7Qi4jeJz/5yb7/7uWXX+49/PDDvZ///OeZ//baa6/17rnnnqPj2Wq1es8880ySx8IQEAwFTWBbGGHcuX0jdv73v/8d/e7W/779dyf59re/HcvLy/Hf//43nn/++Xj55Zf7/tuHH344fvvb3+b+m29+85vxpz/9KZ577rn4wQ9+EPfff39cuHAh3n777fSOhdMRJGDoZ5bcc889ERHx6quvHv3u4OAg87tbdLvdOHfuXHzjG9+Ixx57LF577bVYX1+PO++8s+82PvjBD0ZExAsvvBDvvPPO0e/feuut+OlPfxpLS0vxta99LZ588sn44he/GO+880787ne/S2+otOesRlNO5gFhX/Q0F/7T55133onTp0/HG2+8EY888khcvXo17rzzzvj3v/8drVYrHnjggXjttdfic5/7XLz++uvxm9/8JlqtVnz961+Pb33rW0cdxCD++te/xvve976IiHjwwQfjQx/6UNx1113R7Xbj2rVr8cADD8Rzzz0X9957b3z5y1+OX/ziF/HHP/7xSBwpVU5A4+8HFDnVw7j/zPjlL3/Z+9jHPna0f0+fPt377ne/2/vUpz519Lu7776799GPfrT31FNP9a5evTrS63//+9/vRUTvscce6733ve89es077rij95nPfKZ39913Hzu+6+vrSR4HHQCS6QTKrirqEqkXb775Zjz66KPxj3/8I/7whz/EXXfdldsh/OxnP4s///nP8cgjj8QnPvGJNK8VAgAJGPqpApcvX45HH3306B5BGV555ZX4whe+EK+//npsbW3FysqKHUsAIIH+oS78q3O8XnzxxTh37lzhv3n11Vfj97//fRwcHMRLL70U+/v7cccdd8QzzzwTX/nKV+xUAgAB9A934V9vPv3pT8crr7wSERH33ntvPPnkk7G2thb33XefnUMAIIH+AW/cv/785Cc/ibfeeisefPDBWFxctHAfAQDFOwHhj5TxRTA0nlECXfiDAACiAAgA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";
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
