package com.example.sornanun.binthabard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skyfishjy.library.RippleBackground;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    RippleBackground rippleBackground;
    RelativeLayout positionDetail;
    TextView myLat;
    TextView myLong;
    TextView myAddress;
    TextView myTime;
    FloatingActionButton fab;
    boolean switchLocation = false;

    ImageView internetStatusImage;
    ImageView gpsStatusImage;
    TextView pressImageDesText;
    RelativeLayout internetConnectionBox;
    RelativeLayout gpsConnectionBox;

    String currentUser;
    String currentUserID;

    boolean GPSenabled;
    boolean NetworkEnabled;

    LocationData locationData = new LocationData();
    public static String LOG_Checker = "SornanunCheck";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        TextView txtuser = (TextView) findViewById(R.id.txtuser);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();

            currentUser = email.substring(0, email.length() - 15);
            currentUserID = user.getUid();
        }
        txtuser.setText("ยินดีต้อนรับ " + currentUser);

        myLat = (TextView) findViewById(R.id.txtlatitude);
        myLong = (TextView) findViewById(R.id.txtlongitude);
        myAddress = (TextView) findViewById(R.id.txtaddress);
        myTime = (TextView) findViewById(R.id.txttime);
        positionDetail = (RelativeLayout) findViewById(R.id.positionDetail);
        internetStatusImage = (ImageView) findViewById(R.id.imgInternetStatus);
        gpsStatusImage = (ImageView) findViewById(R.id.imgGPSStatus);
        pressImageDesText = (TextView) findViewById(R.id.pressImageDescriptionText);
        internetConnectionBox = (RelativeLayout) findViewById(R.id.internetConnectionBox);
        internetConnectionBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNetworkAvailable(getBaseContext());
            }
        });
        gpsConnectionBox = (RelativeLayout) findViewById(R.id.gpsConnectionBox);
        gpsConnectionBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isGPSEnable();
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExitDialog();
            }
        });

        rippleBackground = (RippleBackground) findViewById(R.id.content);
        ImageView imageView = (ImageView) findViewById(R.id.centerImage);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchLocation == false) {
                    isNetworkAvailable(getApplicationContext());
                    if (NetworkEnabled) {
                        isGPSEnable();
                        if (GPSenabled && NetworkEnabled) {
                            on();
                        }
                    }
                } else {
                    off();
                }
            }
        });
        setStatusImage();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * Following broadcast receiver is to listen the Location button toggle state in Android.
     */
    private BroadcastReceiver mGpsSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                if (switchLocation == true) {
                    isGPSEnable();
                    if (GPSenabled == false) {
                        off();
                    }
                }
                setStatusImage();
            }
        }
    };

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (switchLocation == true) {
                isNetworkAvailable(getApplicationContext());
                if (NetworkEnabled == false) {
                    off();
                }
            }
            setStatusImage();
        }
    };

    private void on() {
        positionDetail.setVisibility(View.VISIBLE);
        pressImageDesText.setVisibility(View.INVISIBLE);
        myAddress.setText("กำลังระบุตำแหน่งปัจจุบัน...");
        myTime.setText("");

        rippleBackground.startRippleAnimation();
        switchLocation = true;

        Log.d(LOG_Checker,"Status : ON");

        // start service to get location while lock screen
        startService(new Intent(this, LocationService.class));
    }

    private void off() {
        positionDetail.setVisibility(View.INVISIBLE);
        pressImageDesText.setVisibility(View.VISIBLE);

        rippleBackground.stopRippleAnimation();
        switchLocation = false;

        Log.d(LOG_Checker,"Status : OFF");

        stopService(new Intent(this, LocationService.class));
    }

    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(networkStateReceiver);
        unregisterReceiver(mGpsSwitchStateReceiver);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        setStatusImage();

        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST_ACTION));
        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Log.d("SornanunCheck", "onResume");
        if (LocationService.serviceIsRunning == true) {
            positionDetail.setVisibility(View.VISIBLE);
            pressImageDesText.setVisibility(View.INVISIBLE);
            rippleBackground.startRippleAnimation();
            switchLocation = true;
            updateUI();
        }
    }

    private void setStatusImage() {
        if (verifiedInternetConnection() == true) {
            internetStatusImage.setImageResource(R.mipmap.ic_check);
        } else {
            internetStatusImage.setImageResource(R.mipmap.ic_uncheck);
        }
        if (verifiedGPSConnection() == true) {
            gpsStatusImage.setImageResource(R.mipmap.ic_check);
        } else {
            gpsStatusImage.setImageResource(R.mipmap.ic_uncheck);
        }
    }

    private boolean verifiedInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private boolean verifiedGPSConnection() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return statusOfGPS;
    }

    public void showExitDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String message = "คุณต้องการออกจากระบบและปิดแอพพลิเคชั่นใช่หรือไม่ ?";
        builder.setMessage(message)
                .setPositiveButton("ตกลง",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                off();
                                FirebaseAuth.getInstance().signOut();
                                finish();
                            }
                        })
                .setNegativeButton("ยกเลิก",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    public void isGPSEnable() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        GPSenabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!GPSenabled) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final String message = "GPS ไม่ได้เปิดอยู่ กรุณากดตกลง แล้วทำการเปิด GPS";
            builder.setMessage(message)
                    .setTitle("กรุณาเปิด GPS")
                    .setPositiveButton("ตกลง",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton("ยกเลิก",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                    Toast.makeText(getApplicationContext(),
                                            "ไม่สามารถดึงตำแหน่งปัจจุบันของคุณได้ กรุณาเปิด GPS",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
            builder.create().show();
        }
        GPSenabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkEnabled = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();

        if (!NetworkEnabled) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final String message = "อินเทอร์เน็ตไม่ได้เปิดอยู่ กรุณาเชื่อมต่ออินเทอร์เน็ตผ่าน Wifi หรือ 3G หรือ 4G ก่อนดำเนินการอีกครั้ง";
            builder.setMessage(message)
                    .setTitle("กรุณาเชื่อมต่ออินเทอร์เน็ต")
                    .setPositiveButton("ตกลง",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_NETWORK_OPERATOR_SETTINGS));
                                }
                            })
                    .setNegativeButton("ยกเลิก",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                    Toast.makeText(getApplicationContext(),
                                            "คุณยังไม่ได้เปิดการเชื่อมต่ออินเทอร์เน็ต",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
            builder.create().show();
        }
        NetworkEnabled = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void updateUI() {
        if (locationData.getMyLat() == null) {
            myAddress.setText("กำลังระบุตำแหน่งปัจจุบัน...");
            Log.d(LOG_Checker, "update ui but null value");
        } else {
            myLat.setText("ละติจูด " + locationData.getMyLat());
            myLong.setText("ลองจิจูด " + locationData.getMyLong());
            myAddress.setText("สถานที่ " + locationData.getMyAddress());
            myTime.setText("อัพเดทเมื่อ " + DateFormat.getTimeInstance().format(new Date()));
            Log.d(LOG_Checker, "update ui finished");
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SornanunCheck", "Broadcast received");
            updateUI();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
