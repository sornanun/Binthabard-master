package com.example.sornanun.binthabard;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.sornanun.binthabard.HomeActivity.LOG_Checker;

public class LocationService extends Service {

    private static final int TEN_SECONDS = 1000 * 10;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    public static final String BROADCAST_ACTION = "UpdateLocation";

    FirebaseController firebaseController = new FirebaseController();
    LocationData locationData = new LocationData();

    public static boolean serviceIsRunning = false;

    Intent intent;
    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        intent = new Intent(BROADCAST_ACTION);
        Log.d(LOG_Checker, "Service started");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TEN_SECONDS;
        boolean isSignificantlyOlder = timeDelta < -TEN_SECONDS;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceIsRunning = false;
        Log.d(LOG_Checker, "Stop Service done");
        locationManager.removeUpdates(listener);
        firebaseController.removeLacationFromFirebase();
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
            Log.i("**********", "Location changed");
            if (isBetterLocation(loc, previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();

                String latitude = String.valueOf(loc.getLatitude());
                String longitude = String.valueOf(loc.getLongitude());
                String address = getLocation(loc.getLatitude(), loc.getLongitude());

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String updateTime = sdf.format(new Date());

                locationData.setMyLat(latitude);
                locationData.setMyLong(longitude);
                locationData.setMyAddress(address);
                locationData.setMyUpdateTime(updateTime);

                Log.d(LOG_Checker,"Set location finished");

                firebaseController.saveOrUpdateLocationToFirebase(
                        locationData.getMyLat(),
                        locationData.getMyLong(),
                        locationData.getMyAddress(),
                        locationData.getMyUpdateTime());

                Log.d(LOG_Checker, "Update to firebase finished");
                sendBroadcast(intent);
            }
        }

        public String getLocation(Double latitute, Double longitute) {
            Geocoder geocoder;
            List<Address> yourAddresses;
            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                yourAddresses = geocoder.getFromLocation(latitute, longitute, 1);
                if (yourAddresses.size() > 0) {
                    String yourAddress = yourAddresses.get(0).getAddressLine(0);
                    String yourCity = yourAddresses.get(0).getAddressLine(1);
                    String yourCountry = yourAddresses.get(0).getAddressLine(2);
                    return yourAddress + " " + yourCity + " " + yourCountry;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void onProviderDisabled(String provider) {
            //Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider) {
            //Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }
}
