package com.dogusumit.mesaj_yagmuru;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class MainActivity extends AppCompatActivity {

    private static final int PICK_CONTACT = 658;
    private PowerManager.WakeLock wakeLock;

    private EditText editText1;
    private EditText editText2;
    private EditText editText3;
    private EditText editText4;
    private Button button2;
    private TextView textView1;

    private Handler handler;
    private int adet;
    private int gecikme;
    private int gonderilen;
    private boolean devam_ediyor;
    private boolean bildirim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editText1 = (EditText) findViewById(R.id.edittext1);
        editText2 = (EditText) findViewById(R.id.edittext2);
        editText3 = (EditText) findViewById(R.id.edittext3);
        editText4 = (EditText) findViewById(R.id.edittext4);
        Button button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        Button button3 = (Button) findViewById(R.id.button3);
        textView1 = (TextView) findViewById(R.id.textview1);

        devam_ediyor = false;
        bildirim = false;

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent,PICK_CONTACT);
                } catch (Exception e) {
                    toastla(e.getLocalizedMessage());
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!devam_ediyor) {
                        final String telno;
                        adet = 0;
                        gecikme = 0;
                        gonderilen = 0;
                        if (editText1.getText().length() > 0) {
                            telno = editText1.getText().toString();
                            if (editText3.getText().length() > 0) {
                                adet = Integer.parseInt(editText3.getText().toString());
                                if (adet > 0) {
                                    if (editText4.getText().length() > 0) {
                                        gecikme = Integer.parseInt(editText4.getText().toString()) * 60000;
                                        if (gecikme >= 60000) {
                                            mesajGonder(telno, editText2.getText().toString());
                                            ((Button) v).setText(getString(R.string.str8));
                                            devam_ediyor = true;
                                        }
                                    }
                                    else {
                                        toastla(getString(R.string.str18));
                                    }
                                }
                            } else
                                toastla(getString(R.string.str17));
                        } else
                            toastla(getString(R.string.str16));
                    } else {
                        handler.removeCallbacksAndMessages(null);
                        textView1.setText(getString(R.string.str13) + "\t" + gonderilen + "\t" + getString(R.string.str10));
                        ((Button)v).setText(getString(R.string.str7));
                        devam_ediyor = false;
                    }

                    textView1.requestFocus();
                } catch (Exception e) {
                    toastla(e.getLocalizedMessage());
                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    textView1.requestFocus();
                    moveTaskToBack(true);
                    bildirim = true;
                    bildirimGoster();
                } catch (Exception e) {
                    toastla(e.getLocalizedMessage());
                }
            }
        });

        try {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,getString(R.string.app_name));
            wakeLock.acquire();
        } catch (Exception e) {
            toastla(e.getLocalizedMessage());
        }

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void mesajGonder(final String telno, final String mesaj) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                handler = new Handler();
                Runnable r = new Runnable() {
                    public void run() {
                        try {
                            SmsManager smsMgrVar = SmsManager.getDefault();
                            smsMgrVar.sendTextMessage(telno, null, mesaj, null, null);
                            gonderilen++;
                            if (--adet > 0) {
                                textView1.setText(getString(R.string.str11) + "\t" + gonderilen + "\t" + getString(R.string.str10));
                                if (bildirim)
                                    bildirimGoster();
                                handler.postDelayed(this, gecikme);
                            } else {
                                textView1.setText(getString(R.string.str12) + "\t" + gonderilen + "\t" + getString(R.string.str10));
                                button2.setText(getString(R.string.str7));
                                if (bildirim)
                                    bildirimGoster();
                                devam_ediyor = false;
                            }
                        } catch (Exception e) {
                            toastla(e.getLocalizedMessage());
                            button2.setText(getString(R.string.str7));
                            devam_ediyor = false;
                        }
                    }
                };
                handler.post(r);

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 10);
                }
            }
        } catch (Exception e) {
            toastla(e.getLocalizedMessage());
            button2.setText(getString(R.string.str7));
            devam_ediyor = false;
        }
    }

    private void bildirimGoster() {
        try {
            NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(textView1.getText().toString())
                    .setAutoCancel(true);
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(contentIntent);
            NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(PICK_CONTACT, mBuilder.build());
        } catch (Exception e) {
            toastla(e.getLocalizedMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_CONTACT:
                    try {
                        Uri contactUri = data.getData();
                        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                        Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
                        assert cursor != null;
                        cursor.moveToFirst();
                        int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String number = cursor.getString(column);
                        editText1.setText(number);
                        cursor.close();
                    } catch (Exception e) {
                        toastla(getString(R.string.str15));
                    }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        bildirim = false;
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        bildirim = true;
        super.onStop();
    }

    @Override
    protected void onStart() {
        bildirim = false;
        NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(PICK_CONTACT);
        super.onStart();
    }

    private void toastla(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void uygulamayiOyla() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            } catch (Exception ane) {
                toastla(e.getMessage());
            }
        }
    }

    private void marketiAc() {
        try {
            Uri uri = Uri.parse("market://developer?id=" + getString(R.string.play_store_id));
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=" + getString(R.string.play_store_id))));
            } catch (Exception ane) {
                toastla(e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.oyla:
                uygulamayiOyla();
                return true;
            case R.id.market:
                marketiAc();
                return true;
            case R.id.cikis:
                System.exit(0);
                android.os.Process.killProcess(android.os.Process.myPid());
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        wakeLock.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}