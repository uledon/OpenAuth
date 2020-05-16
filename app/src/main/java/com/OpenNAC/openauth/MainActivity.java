package com.OpenNAC.openauth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.OpenNAC.openauth.Services.DataClass;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String ENGLISH_LANG = "en", SPANISH_LANG = "es", PORTOGUESE_LANG = "pt";
    private static final String info = "https://www.opencloudfactory.com/en/";
    private Button infoBtn;
    private long backPressedTime;
    private Toast backToast;

    ///maybe need to have a better way of handling languages
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //System.out.println("after main tkn = " + DataClass.getFirebaseToken());
        Button beginButton = findViewById(R.id.beginButton);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        System.out.println("dataclass ip is " + DataClass.getIp(wifiInfo) + "\n"
                + "hostname is: " + DataClass.getHostName(this) + "\n"
                + "model " + DataClass.getModel() + "\n"
                + " security Patch is " + DataClass.getSecurityPatch() + "\n"
                + " actual mac " + DataClass.getMacAddr());
        getLocation(); /// get location
        System.out.println("time in main is " + DataClass.getTimeStamp());
        infoBtn = findViewById(R.id.infoBtn);
        ImageButton spanishBtn = findViewById(R.id.spanishlang), englishBtn = findViewById(R.id.englang);
        beginButton.setOnClickListener((View view) -> {
            Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
        });
        infoBtn.setOnClickListener(v -> {
            Uri webaddress = Uri.parse(info);
            Intent goToSite = new Intent(Intent.ACTION_VIEW, webaddress);
            if (goToSite.resolveActivity(getPackageManager()) != null) {
                startActivity(goToSite);
            }
        });

        spanishBtn.setOnClickListener(v -> {
            setLanguage(SPANISH_LANG);
        });

        englishBtn.setOnClickListener(v -> {
            setLanguage(ENGLISH_LANG);
        });

    }

    @Override
    public void onBackPressed() {

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), getString(R.string.press_back_again), Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    private void setLanguage(String localeCode) {
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(new Locale(localeCode.toLowerCase()));
        } else {
            configuration.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(configuration, displayMetrics);
        recreate();
    }

    private void getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        } else {
            findLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findLocation();
                } else {
                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    public void findLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        try {
            String city = DataClass.getLocation(this, location.getLatitude(), location.getLongitude());
            System.out.println("data class city is " + city);
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "not found", Toast.LENGTH_SHORT).show();
        }
    }
}
