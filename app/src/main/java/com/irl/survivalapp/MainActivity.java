package com.irl.survivalapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * Survival application. Renders a simple to use UI with functions to open a compass,
 * the users location on Google Maps and the possibility to store an emergency contact
 * that the user can send emergency SMS to in case of danger.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DEBUG";

    private Button compassBtn;
    private Button mapBtn;
    private Button smsBtn;
    private Button callBtn;
    private Button settingsBtn;

    private EditText numberText;
    private TextView contactText;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private static String CHANNEL_ID = "MyChannel";

    private double longitude;
    private double latitude;

    private String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };
    private ArrayList<String> permissionsNeeded = new ArrayList<>();

    /**
     * View call. Sets activity, variables and listeners.
     * Checks for permissions and asks for them if not already granted or starts location tracking.
     * Creates a locationListener to track the units GPS location.
     *
     * @param savedInstanceState
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            /**
             * Updates latitude & longitude on GPS updates.
             */
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            /**
             * If Position is disabled, opens settings to prompt the user to turn it on.
             */
            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        numberText = findViewById(R.id.numberText);
        contactText = findViewById(R.id.contactText);
        storedEmergencyNumber();

        createNotificationChannel();

        smsBtn = findViewById(R.id.smsBtn);
        smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmergencySMS();
            }
        });

        callBtn = findViewById(R.id.callBtn);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEmergencyCall();
            }
        });

        compassBtn = findViewById(R.id.compassBtn);
        compassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCompass();
            }
        });

        mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEmergencyContact();
            }
        });

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
    }

    /**
     * Helper method to check for permissions.
     */
    private void checkPermissions() {
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, perm) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(perm);
            }
        }
        if (permissionsNeeded.size() > 0) {
            ActivityCompat.requestPermissions(MainActivity.this, permissionsNeeded.toArray(new String[permissionsNeeded.size()]), 1);
        }
    }

    /**
     * Receives result of permission request and exits application if all are not granted or starts location tracking.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            System.exit(0);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a notification channel.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "channelname", importance);
            channel.setDescription("description");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Gets emergency number from shared preferences.
     */
    private String getEmergencyNumber() {
        String fileName = "emergencyContact";
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(fileName, "");
    }

    /**
     * Returns latitude & longitude
     */
    private String getCoordinates() {
        return "Lat: " + latitude + "Long: " + longitude;
    }

    /**
     * Opens google maps with the devices location zoomed in.
     * The app may have to be run for a few moments for it to receive location updates.
     */
    private void openMap() {
        Log.d(TAG, "openMap: " + latitude + " ::: " + longitude);
        String label = "currentPos";
        String uriBegin = "geo:" + latitude + "," + longitude;
        String query = latitude + "," + longitude + "(" + label + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * Opens compass activity.
     */
    private void openCompass() {
        Intent intent = new Intent(this, CompassActivity.class);
        startActivity(intent);
    }

    /** Creates a notification that opens up the app again. */
    private void notification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText("EMERGENCY NOTIFICATION")
                .setContentText("EMERGENCY SMS HAS BEEN SENT")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(MainActivity.this);
        manager.notify(1, builder.build());
    }

    /**
     * Checks the format of the emergency number.
     */
    private boolean checkViableNumber(String number) {
        return number.matches("^[0-9]{10}$");
    }

    /**
     * Gets the stored emergency number from shared preferences
     * and displays it in the UI or, if no number present
     * informs the user to store a number.
     */
    private void storedEmergencyNumber() {
        if (checkViableNumber(getEmergencyNumber())){
            String emergencyNumber = getEmergencyNumber();
            contactText.setText(emergencyNumber);
        } else {
            Toast.makeText(this, "Save an emergency contact!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets emergency number to be used in the application and stores it in
     * shared preferences.
     */
    private void setEmergencyContact() {
        if (checkViableNumber(numberText.getText().toString())) {
            String number =  numberText.getText().toString();
            String fileName = "emergencyContact";
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(fileName, number);
            editor.commit();
            numberText.setText("");
            contactText.setText(number);
            Toast.makeText(this, "Emergency contact saved!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Use format: '0701234567", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Invokes SendSMS class and sends an emergency SMS.
     */
    private void sendEmergencySMS() {
        if (!checkViableNumber(getEmergencyNumber())) {
            Toast.makeText(this, "NO VIABLE CONTACT SAVED", Toast.LENGTH_SHORT).show();
        } else {
            new SendSMS().sendSMS("+46" + getEmergencyNumber(), "HELP IM IN DANGER! MY COORDINATES ARE: " + getCoordinates());
            Toast.makeText(MainActivity.this, "EMERGENCY TEXT SENT, HANG IN THERE", Toast.LENGTH_SHORT).show();
            notification(MainActivity.this);
        }
    }

    /**
     * Opens the phone to call the number chosen.
     */
    private void makeEmergencyCall() {
        if (!checkViableNumber(getEmergencyNumber())) {
            Toast.makeText(this, "NO VIABLE CONTACT SAVED", Toast.LENGTH_SHORT).show();
        } else {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + "+46" + getEmergencyNumber()));
            if (callIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(callIntent);
            }
        }
    }


}
