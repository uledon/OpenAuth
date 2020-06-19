package com.OpenNAC.openauth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.Menu;
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
import com.OpenNAC.openauth.Services.User;
import com.OpenNAC.openauth.Services.UserService;
import com.OpenNAC.openauth.remote.EncryptionUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String SHARED_PREFS= "sharedPrefs",URL_TEXT= "urlBox",TOKEN_TEXT = "token",
            ENGLISH_LANG = "en", SPANISH_LANG = "es", PORTOGUESE_LANG = "pt",useragent = "Mozilla/5.0",
            API_KEY = "AAAAVBsyhjw:APA91bETZ1-vkgonil0ZuQK9TyZNUMhRxATxQQEPYxGm4ZbYMF3GqprfZDFNyFXZCwG8AGu669cGpWDkn4y_YVJFR_G9miQK8BHDfl3_uFpLNSfozkOf4N9uCo0FzltgyuWW-gOa4Msn",
            ttl = "300";
    private static final String info = "https://www.opencloudfactory.com/en/";
    private Button infoBtn,beginBtn,helpBtn;
    private long backPressedTime;
    private Toast backToast;
    TextView welcomeText, account_name;
    ImageView splash_background,app_logo, appLogoLight;
    Animation topAnim,iconAnim,fadeAnim,logoAnim;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    View header;
    final static float metric = (float)1.58;
    final static float logo_metric=(float) 6.64;
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
        //toolbar and navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar =  findViewById(R.id.toolbar);
        header = navigationView.inflateHeaderView(R.layout.header); //getting header manually
        account_name = header.findViewById(R.id.authenticator_app); //setting value manually
        Runnable runnable1 = () -> {
            setSupportActionBar(toolbar);
            navigationView.bringToFront();
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setNavigationItemSelectedListener(MainActivity.this);
        };
        Thread thread1 = new Thread(runnable1);
        thread1.start();
        //setting animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        iconAnim = AnimationUtils.loadAnimation(this, R.anim.icon_anim);
        logoAnim = AnimationUtils.loadAnimation(this,R.anim.logo_anim);

        Runnable runnable = this::executeAnimations;
        Thread thread = new Thread(runnable);
        thread.start();
        //executeAnimations();
        //getting wifi data
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        //testing the data outputs
        System.out.println("dataclass ip is " + DataClass.getIp(wifiInfo) + "\n"
                + "hostname is: " + DataClass.getHostName(this) + "\n"
                + "model " + DataClass.getModel() + "\n"
                + " security Patch is " + DataClass.getSecurityPatch() + "\n"
                + " actual mac " + DataClass.getMacAddr());
        getLocation(); //method is used to get the current location of the device.
        //testing if time is correct
        System.out.println("time in main is " + DataClass.getTimeStamp());
        //setting button actions
        beginBtn.setOnClickListener((View view) -> {
            Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
            startActivityForResult(myIntent, 0);
            this.onPause();
        });

        infoBtn.setOnClickListener(v -> {
            Uri webaddress = Uri.parse(info);
            Intent goToSite = new Intent(Intent.ACTION_VIEW, webaddress);
            if (goToSite.resolveActivity(getPackageManager()) != null) {
                startActivity(goToSite);
            }
        });

        helpBtn.setOnClickListener(v->{
//            makeCall();
//            Toast.makeText(this,"Support is contacted",Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(v.getContext(), OnboardingActivity.class);
            startActivityForResult(myIntent,0);
        });
        System.out.println("Token is: " + DataClass.getFirebaseToken());
    }


    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            //finish();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), getString(R.string.press_back_again), Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    /**
     * Method changes the current App Language
     * @param localeCode //Language code. Only accepting en; es; pt
     */
    private void setLanguage(String localeCode) {
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(localeCode.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
        onConfigurationChanged(configuration);
    }

    /**
     * This method will refresh the current screens string files
     * This is done manually because method recreate() is a heavy task, takes time and looks bad
     * @param newConfig getting from setLanguage method
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // refreshing views here
        beginBtn.setText(getString(R.string.connect));
        helpBtn.setText(getString(R.string.help));
        infoBtn.setText(getString(R.string.visit_our_website));
        welcomeText.setText(getString(R.string.welcome));
        Menu menu =  navigationView.getMenu();
        menu.findItem(R.id.english).setTitle(R.string.english);
        menu.findItem(R.id.spanish).setTitle(R.string.spanish);
        menu.findItem(R.id.portuguese).setTitle(R.string.portuguese);
        menu.findItem(R.id.language_title).setTitle(R.string.language);
        account_name.setText(getString(R.string.authenticator_app));
        super.onConfigurationChanged(newConfig);

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public void executeAnimations(){
        float slash_movement = getScreenHeight()/metric;
        float logo_movement = getScreenHeight()/logo_metric;
//        System.out.println("app logo y is: "+app_logo.getY() + "height is:" + app_logo.getHeight()+ "width is:" +app_logo.getWidth() +"pivot is: " + app_logo.getPivotY());
//        System.out.println("The system height is:" + getScreenHeight());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                splash_background.animate().translationY(-slash_movement).setDuration(2300).setStartDelay(300);
                app_logo.animate().translationY(-logo_movement).setDuration(2500).setStartDelay(300);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

        //app_logo.setAnimation(logoAnim);
//        Handler handler = new Handler();
//        handler.postDelayed(() -> ImageViewCompat.setImageTintList(splash_background, ColorStateList.valueOf(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))), 3100);
        infoBtn.startAnimation(iconAnim);
        beginBtn.startAnimation(iconAnim);
        helpBtn.startAnimation(iconAnim);
//        hamburgerBtn.startAnimation(topAnim);
        //welcomeText.startAnimation(topAnim);
      //  toolbar.setAnimation(topAnim);
//        appLogoLight.startAnimation(topAnim);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String setLanguage = sharedPreferences.getString("getLanguage", "") ;
        switch (menuItem.getItemId()){
            case R.id.english:
                    setLanguage(ENGLISH_LANG);
                    setLanguage = ENGLISH_LANG;
                break;
            case R.id.spanish:
                    setLanguage(SPANISH_LANG);
                    setLanguage = SPANISH_LANG;
                break;
            case R.id.portuguese:
                    setLanguage(PORTOGUESE_LANG);
                    setLanguage = PORTOGUESE_LANG;
                break;
        }
        editor.putString("getLanguage", setLanguage);
        editor.apply();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void makeCall(){
        try{
            // HttpLoggingInterceptor used to log the connection and debug the connection
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            String urlText = EncryptionUtils.decrypt(MainActivity.this, sharedPreferences.getString(URL_TEXT, ""));
            String cookie = EncryptionUtils.decrypt(MainActivity.this, sharedPreferences.getString("cookie", ""));
            String username = EncryptionUtils.decrypt(MainActivity.this, sharedPreferences.getString("username", ""));
            String token = EncryptionUtils.decrypt(MainActivity.this, sharedPreferences.getString("token", ""));
            String deviceId = EncryptionUtils.decrypt(MainActivity.this, sharedPreferences.getString("deviceId", ""));
            String action = "send_notification";
            System.out.println(deviceId);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(urlText)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            UserService api = retrofit.create(UserService.class);
            RequestBody actionPart = RequestBody.create(MultipartBody.FORM, action);
            RequestBody apiKeyPart = RequestBody.create(MultipartBody.FORM, API_KEY);
            RequestBody deviceIdPart = RequestBody.create(MultipartBody.FORM, deviceId);
            RequestBody ttlPart = RequestBody.create(MultipartBody.FORM, ttl);
            RequestBody titlePart = RequestBody.create(MultipartBody.FORM, "Did you just log in?");
            RequestBody messagePart = RequestBody.create(MultipartBody.FORM, "Someone is trying to log in!");
            RequestBody fcmtPart = RequestBody.create(MultipartBody.FORM, DataClass.getFirebaseToken());
            System.out.println("before call token is: "+token);
            System.out.println("before call username is: "+username);
            System.out.println("before call cookie is: "+cookie);
            System.out.println("before call useragent is: "+useragent);
            System.out.println("before call action is: "+ action);
            System.out.println("before call deviceId is: "+ deviceId);
            System.out.println("before call apiKey is: "+ API_KEY);
            Call<User> call = api.send_notification(token,username, cookie,useragent,actionPart,deviceIdPart,apiKeyPart,fcmtPart,ttlPart,titlePart, messagePart);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {

                    if (!response.isSuccessful()) {
                        System.out.println("Operation not successful because " + response.code() +
                                " " + response.message() + " " + response.body());
                        Toast.makeText(MainActivity.super.getBaseContext(), "Here " + response.code() +
                                " " + response.message() + " " + response.body(), Toast.LENGTH_SHORT).show();
                        return;

                    }
                    System.out.println("the thing went great");
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    System.out.println("the problem is failure: " + t.getMessage());
                    //Toast.makeText(MainActivity.super.getBaseContext(), "Failure" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
        catch(IllegalArgumentException ex){
            System.out.println("the problem is illegal argument exception: " + ex.getMessage());
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
