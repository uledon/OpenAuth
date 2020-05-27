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
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.OpenNAC.openauth.Services.DataClass;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String ENGLISH_LANG = "en", SPANISH_LANG = "es", PORTOGUESE_LANG = "pt";
    private static final String info = "https://www.opencloudfactory.com/en/";
    private Button infoBtn,beginBtn,helpBtn;
    private long backPressedTime;
    private Toast backToast;
    TextView welcomeText;
    ImageView splash_background,app_logo, appLogoLight;
    Animation topAnim,iconAnim,fadeAnim,logoAnim;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        splash_background = findViewById(R.id.bigBack2);
        app_logo= findViewById(R.id.appLogo);
        infoBtn = findViewById(R.id.infoBtn);
        beginBtn = findViewById(R.id.beginButton);
        helpBtn = findViewById(R.id.helpBtn);
        welcomeText = findViewById(R.id.welcomeText);
        //toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        //toolbar
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        iconAnim = AnimationUtils.loadAnimation(this, R.anim.icon_anim);
        logoAnim = AnimationUtils.loadAnimation(this,R.anim.logo_anim);
        executeAnimations();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        System.out.println("dataclass ip is " + DataClass.getIp(wifiInfo) + "\n"
                + "hostname is: " + DataClass.getHostName(this) + "\n"
                + "model " + DataClass.getModel() + "\n"
                + " security Patch is " + DataClass.getSecurityPatch() + "\n"
                + " actual mac " + DataClass.getMacAddr());
        getLocation(); /// get location
        System.out.println("time in main is " + DataClass.getTimeStamp());
        beginBtn.setOnClickListener((View view) -> {
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

    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
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

    public void executeAnimations(){
        splash_background.animate().translationY(-1670).setDuration(2300).setStartDelay(300);
        app_logo.animate().translationYBy(-270).setDuration(2500).setStartDelay(300);
        //app_logo.setAnimation(logoAnim);
//        Handler handler = new Handler();
//        handler.postDelayed(() -> ImageViewCompat.setImageTintList(splash_background, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))), 3100);
        infoBtn.startAnimation(iconAnim);
        beginBtn.startAnimation(iconAnim);
        helpBtn.startAnimation(iconAnim);
//        hamburgerBtn.startAnimation(topAnim);
        welcomeText.startAnimation(topAnim);
        toolbar.setAnimation(topAnim);
//        appLogoLight.startAnimation(topAnim);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return true;
    }
}
