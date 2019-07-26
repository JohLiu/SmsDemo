package com.joh.smsdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private SMSBroadcastReceiver1 smsBr1;
    private IntentFilter intentFilter1;
    private SMSBroadcastReceiver2 smsBr2;
    private IntentFilter intentFilter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
//                .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                .tag(TAG)   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        //注册广播
        smsBr1 = new SMSBroadcastReceiver1();
        intentFilter1 = new IntentFilter("SENT_SMS_ACTION");
        registerReceiver(smsBr1, intentFilter1);

        smsBr2 = new SMSBroadcastReceiver2();
        intentFilter2 = new IntentFilter("DELIVERED_SMS_ACTION");
        registerReceiver(smsBr2, intentFilter2);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsBr1);
        unregisterReceiver(smsBr2);
    }

    @OnClick({R.id.btn_sms_one, R.id.btn_sms_two, R.id.btn_sms_three, R.id.btn_sms_four})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_sms_one:
                Uri smsToUri = Uri.parse("smsto:" + "12345678;123456789");
                Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
                intent.putExtra("sms_body", "短信内容");
                startActivity(intent);
                break;
            case R.id.btn_sms_two:
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setData(Uri.parse("smsto:"));
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", "12345678;123456789");
                smsIntent.putExtra("sms_body", "测试");
                startActivity(smsIntent);
                break;
            case R.id.btn_sms_three:
                Intent sendIntent = new Intent("SENT_SMS_ACTION");
                PendingIntent sendPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sendIntent, 0);

                Intent deliverIntent = new Intent("DELIVERED_SMS_ACTION");
                PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);

                SmsManager smsManager = SmsManager.getDefault();
                List<String> divideContents = smsManager.divideMessage("测试3");
                for (String text : divideContents) {
                    smsManager.sendTextMessage("12345678;123456789", null, text, sendPI, deliverPI);
                }

                break;
            case R.id.btn_sms_four:
                Intent sendIntent2 = new Intent("SENT_SMS_ACTION");
                PendingIntent sendPI2 = PendingIntent.getBroadcast(getApplicationContext(), 0, sendIntent2, 0);

                Intent deliverIntent2 = new Intent("DELIVERED_SMS_ACTION");
                PendingIntent deliverPI2 = PendingIntent.getBroadcast(this, 0, deliverIntent2, 0);

                SmsManager smsManager2 = SmsManager.getDefault();
                List<String> divideContents2 = smsManager2.divideMessage("测试4");
                String[] phone = {"12345678", "123456789"};
                for (String p : phone) {
                    for (String text : divideContents2) {
                        smsManager2.sendTextMessage(p, null, text, sendPI2, deliverPI2);
                    }
                }

                break;
            default:
                break;
        }
    }

    String TAG = getClass().getSimpleName();
    /* 注意，下面两种接收器是有区别的
     * 短信在发送过程中，可能有两种情况用户是接收不到信息的
     * 1.运营商（电信、联通、移动）没有把这条短信发送出去，用户没有接收到
     * 2.运营商发送成功了，但是用户由于某些原因并没有接收到短信
     */

    /**
     * 用于接收发送短信后的回调（即运营商是否发送成功）
     */
    private class SMSBroadcastReceiver1 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "短信发送成功", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    //常见故障(没插卡)
                    Logger.d(intent.getStringExtra("destinationAddress"));
                    Logger.e("SmsManager.RESULT_ERROR_GENERIC_FAILURE" + "-------" + "常见故障");
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    //飞行模式
                    Logger.e("SmsManager.RESULT_ERROR_RADIO_OFF" + "-------" + "飞行模式");
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    //空号
                    Logger.e("SmsManager.RESULT_ERROR_NULL_PDU" + "-------" + "空号");
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    //无服务（没信号）
                    Logger.e("SmsManager.RESULT_ERROR_NO_SERVICE" + "-------" + "无服务（没信号）");
                    break;
                case SmsManager.RESULT_ERROR_LIMIT_EXCEEDED:
                    //超过服务商规定的短信发送条数
                    Logger.e("SmsManager.RESULT_ERROR_LIMIT_EXCEEDED" + "-------" + "超过服务商规定的短信发送条数");
                    break;
                case 6:
                    //开启FDN,即手机设定指定拨号后，只能与设置的几个有限号码通信
                    Logger.e("SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE" + "-------" + "开启FDN,即手机设定指定拨号后，只能与设置的几个有限号码通信");
                    break;
                case SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED:
                    //用户禁止发送短信
                    Logger.e("SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED" + "-------" + "用户禁止发送短信");
                    break;
                case SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED:
                    //用户禁止了应用的发送短信权限
                    Logger.e("SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED" + "-------" + "用户禁止了应用的发送短信权限");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 用于接收对方是否收到短信的回调（即目标用户是否接受成功）
     */
    private class SMSBroadcastReceiver2 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "短信发送成功", Toast.LENGTH_SHORT).show();
        }
    }

}
