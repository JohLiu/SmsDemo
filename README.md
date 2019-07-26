# SmsDemo
批量发送短信的几种方式

```
Uri smsToUri = Uri.parse("smsto:" + "12345678;123456789");
Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
intent.putExtra("sms_body", "短信内容");
startActivity(intent);

Intent smsIntent = new Intent(Intent.ACTION_VIEW);
smsIntent.setData(Uri.parse("smsto:"));
smsIntent.setType("vnd.android-dir/mms-sms");
smsIntent.putExtra("address", "12345678;123456789");
smsIntent.putExtra("sms_body", "测试");
startActivity(smsIntent);

// 长短信发送方式1
Intent sendIntent = new Intent("SENT_SMS_ACTION");
PendingIntent sendPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sendIntent, 0);
Intent deliverIntent = new Intent("DELIVERED_SMS_ACTION");
PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);
SmsManager smsManager = SmsManager.getDefault();
List<String> divideContents = smsManager.divideMessage("测试3");
for (String text : divideContents) {
   smsManager.sendTextMessage("12345678;123456789", null, text, sendPI, deliverPI);
}
// 长短信发送方式2
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
```