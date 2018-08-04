package com.phonegap.plugins.printActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;


import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;





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

import com.google.zxing.BarcodeFormat;

import java.util.Timer;
import vpos.apipackage.PosApiHelper;
import vpos.apipackage.PrintInitException;


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
         //    try {
             if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return false;
        }

        printThread = new Print_Thread(PRINT_TEST);
        printThread.start();
       //     } catch (IOException e) {
              //  Log.e(tag, e.getMessage());
            //    e.printStackTrace();
      //      }
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

                switch (type) {

                    case PRINT_LONGER:
                        SendMsg("PRINT LONG");

                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        String stringg = " a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?";
                        //  Print.Lib_PrnStr(string + "\n");
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);

//                      for(int i=0;i<50;i++)
//                      {
//                          Print.Lib_PrnStr(Integer.toString(i)+ " "+stringg + "\n");
//                      }
                        posApiHelper.PrintStr("a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >? a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >? a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?\n");
//                      ret = Print.Lib_PrnStr(" a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?");
                        posApiHelper.PrintBarcode(content, 360, 120, BarcodeFormat.CODE_128);
                        posApiHelper.PrintStr("CODE_128 : " + content + "\n\n");
                        posApiHelper.PrintBarcode(content, 240, 240, BarcodeFormat.QR_CODE);
                        posApiHelper.PrintStr("QR_CODE : " + content + "\n\n");
                        posApiHelper.PrintStr("发卡行(ISSUER):01020001 工商银行\n");
                        posApiHelper.PrintStr("卡号(CARD NO):\n");
                        posApiHelper.PrintStr("    9558803602109503920\n");
                        posApiHelper.PrintStr("收单行(ACQUIRER):03050011民生银行\n");
                        posApiHelper.PrintStr("交易类型(TXN. TYPE):消费/SALE\n");
                        posApiHelper.PrintStr("卡有效期(EXP. DATE):2013/08\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                        posApiHelper.PrintStr("                                         ");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");
                        posApiHelper.PrintStr("\n");

                        SendMsg("Printing... ");
                        ret = posApiHelper.PrintStart();

                        Log.e(tag, "PrintStart ret = " + ret);

                        msg1.what = ENABLE_RG;
                        handler.sendMessage(msg1);

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

                        break;

                    case PRINT_TEST:
                        SendMsg("PRINT_TEST");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        //0 left，1 Ringht ，2 middle
//                        Print.Lib_PrnSetAlign(0);
                        posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);
                        posApiHelper.PrintStr("POS签购单/POS SALES SLIP\n");
                        posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x00);
                        posApiHelper.PrintStr("商户存根MERCHANT COPY\n");
                        posApiHelper.PrintStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                        posApiHelper.PrintStr("商户名称(MERCHANT NAME):\n");
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

                        break;


                    case PRINT_CYCLE:
                        SendMsg("PRINT_CYCLE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                        for (long dd = 0; dd < 100; dd++) {
                            posApiHelper.PrintStr("0 1 2 3 4 5 6 7 8 9 A B C D E\n");
                        }

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

                        break;

                    case PRINT_UNICODE:
                        SendMsg("PRINT_UNICODE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);

                        posApiHelper.PrintStr("中文:你好，好久不见。\n");
                        posApiHelper.PrintStr("英语:Hello, Long time no see\n");
                        posApiHelper.PrintStr("西班牙语:España, ¡Hola! Cuánto tiempo sin verte!\n");
//                        // posApiHelper.PrintStr("阿拉伯语:مرحبا! وقت طويل لا رؤية!\n");
                        posApiHelper.PrintStr("法语:Bonjour! Ça fait longtemps!\n");
//                        posApiHelper.PrintStr("日语:こんにちは！久しぶり！\n");
//                        posApiHelper.PrintStr("俄语:Привет! Давно не виделись!\n");
//                        posApiHelper.PrintStr("韩语:안녕하세요! 긴 시간은 더 볼 수 없습니다!\n");
//                        String string1 = "А а, Б б, В в, Г г, Д д, Е е, Ё ё, Ж ж, З з, И и, Й й, К к, Л л, М м, Н н, О о, Ө ө, П п, Р р, С с, Т т, У у, Ү ү, Ф ф, Х х, Ц ц, Ч ч, Ш ш, Щ щ, Ъ ъ, Ь ь, Ы ы, Э э, Ю ю, Я я";
//                        posApiHelper.PrintStr(string1 + "\n");
//                        posApiHelper.PrintStr("                                         ");
//                        posApiHelper.PrintStr("\n");
//                        posApiHelper.PrintStr("                                         ");
//                        posApiHelper.PrintStr("\n");

                        SendMsg("Printing... ");
//                        ret = posApiHelper.PrintStart();
                        ret = posApiHelper.PrintCtnStart();

                        for (int i = 1; i < 6; i++) {
                            posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x33);
                            posApiHelper.PrintStr("打印第：" + i + "次\n");
                            posApiHelper.PrintStr("打印第：" + i + "次\n");
                            posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);
                            posApiHelper.PrintStr("商户存根MERCHANT COPY\n");
                            posApiHelper.PrintStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                            posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                            posApiHelper.PrintStr("商户名称(MERCHANT NAME):\n");
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

                            ret = posApiHelper.PrintCtnStart();

                            if (ret != 0) break;
                        }

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

                        ret = posApiHelper.PrintCheckStatus();
                        Log.e("liuhao", "PrintCheckStatus = " + ret);

                        ret = posApiHelper.PrintClose();

                        break;

                    case PRINT_OPEN:
                        SendMsg("PRINT_BARCODE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        SendMsg("Print Open... ");
                        try {
                            ret = posApiHelper.PrintOpen();
                        } catch (PrintInitException e) {
                            e.printStackTrace();
                        }

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

                        break;



                    case PRINT_BARCODE:
                        SendMsg("PRINT_BARCODE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        posApiHelper.PrintBarcode(content, 360, 120, BarcodeFormat.CODE_128);
                        posApiHelper.PrintStr("CODE_128 : " + content + "\n\n");
                        posApiHelper.PrintBarcode(content, 240, 240, BarcodeFormat.QR_CODE);
                        posApiHelper.PrintStr("QR_CODE : " + content + "\n\n");
//                  posApiHelper.PrintBarcode(content, 360, 120, BarcodeFormat.CODE_39);
//                  posApiHelper.PrintStr("CODE_39 : " + content + "\n\n");
                        posApiHelper.PrintStr("                                        \n");
                        posApiHelper.PrintStr("                                         \n");
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

                        break;

                    default:
                        break;
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

   
    
}