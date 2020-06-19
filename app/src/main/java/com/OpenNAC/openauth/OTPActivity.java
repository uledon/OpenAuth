package com.OpenNAC.openauth;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.OpenNAC.openauth.Services.RssReader;
import com.OpenNAC.openauth.Services.User;
import com.OpenNAC.openauth.Services.UserService;
import com.OpenNAC.openauth.remote.EncryptionUtils;
import com.OpenNAC.openauth.remote.TimeBasedOneTimePasswordUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OTPActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    TextView otpTxt,account_name, username_box, server_box, onBoardingTime_box, version_box;
    private Date date;
    static String code,base32Secret, nameBox;
    private static final String SHARED_PREFS = "sharedPrefs",URL_TEXT = "urlBox",ENGLISH_LANG = "en", SPANISH_LANG = "es", PORTOGUESE_LANG = "pt",useragent = "Mozilla/5.0";
    ProgressBar progressBar;
    Button showotpBtn, disconnect_button,copyBtn;
    TextView connected_title;
    boolean shown;
    int ymove= 420;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    RecyclerView recyclerView;
    String address;
    //ScrollView scrollView;
    /**
     * This method will be run when the page is created
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_otp);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        //Button generateBtn = findViewById(R.id.generateBtn);//
        //Button logoutBtn = findViewById(R.id.logoutBtn);
        //accNameTxt = findViewById(R.id.accNameTxt);
        connected_title = findViewById(R.id.connected_title);
       // scrollView = findViewById(R.id.scrollView);
        //toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView = findViewById(R.id.nav_view);
        toolbar =  findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.inflateHeaderView(R.layout.header);
        account_name = header.findViewById(R.id.authenticator_app);
        View header2 = navigationView.inflateHeaderView(R.layout.user_box);
        username_box = header2.findViewById(R.id.username_box);
        server_box = header2.findViewById(R.id.server_box);
        onBoardingTime_box = header2.findViewById(R.id.onBoardingTime_box);
        version_box = header2.findViewById(R.id.version_box);
        disconnect_button = header2.findViewById(R.id.disconnect);
        copyBtn = header2.findViewById(R.id.copyBtn);
        username_box.setText(EncryptionUtils.decrypt(OTPActivity.this, sharedPreferences.getString("account", "")));
        server_box.setText(getString(R.string.server)+ ": " + EncryptionUtils.decrypt(OTPActivity.this, sharedPreferences.getString(URL_TEXT, "")));
        onBoardingTime_box.setText(getString(R.string.time)+ ": " + sharedPreferences.getString("timestamp", ""));
        version_box.setText(getString(R.string.version)+ ": 1.0");
        copyBtn.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("ServerName", EncryptionUtils.decrypt(OTPActivity.this, sharedPreferences.getString(URL_TEXT, "")));
            assert clipboardManager != null;
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(OTPActivity.this,getString(R.string.server_name_copied), Toast.LENGTH_SHORT).show();
        });
        //getting rssfeed url
        //rss feed
        recyclerView = findViewById(R.id.recyclerview);
        if(isNetworkAvailable()){
            getConfiguration();
        }
        else{
            Toast.makeText(OTPActivity.this,"No Internet", Toast.LENGTH_SHORT).show();
        }

        ////
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        //toolbar
        disconnect_button.setOnClickListener(v -> deletePost());
        otpTxt = findViewById(R.id.otpTxt);
        progressBar = findViewById(R.id.progressBar);
        showotpBtn = findViewById(R.id.showotpBtn);
        base32Secret = EncryptionUtils.decrypt(OTPActivity.this, sharedPreferences.getString("secret", ""));
        nameBox = EncryptionUtils.decrypt(OTPActivity.this, sharedPreferences.getString("account", ""));
        code = null;

                try {
                    code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);
                } catch (GeneralSecurityException | IllegalArgumentException e) {
                    e.printStackTrace();
                }

/// setting the Timer and making sure updates every 30 seconds
        code = code.substring(0, 3) + "-" + code.substring(3, code.length());
        otpTxt.setText(code);
        //accNameTxt.setText(getString(R.string.your_otp_is)+nameBox);
        Thread t = new Thread(){
            @Override
            public void run(){
                while(!isInterrupted()){
                    try {
                        Thread.sleep(1000);
//                        System.out.println(System.currentTimeMillis());
                        date = new Date();

                        runOnUiThread(() -> {

                                try {
                                    code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);
                                    code = code.substring(0, 3) + "-" + code.substring(3, code.length());
                                    otpTxt.setText(code);
                                    if(date.getSeconds()>=30){
                                        date.setSeconds(date.getSeconds()-30);
                                    }
                                    setProgressBar(date.getSeconds());
                                }
                                catch(GeneralSecurityException e){
                                    e.printStackTrace();
                                }

                            });
                    }
                    catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };
         t.start();
        showotpBtn.setOnClickListener(v -> {
           if (!shown){
               animatein();
               shown=true;
               showotpBtn.setText(getString(R.string.hide_otp));
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       animateout();
                       showotpBtn.setText(getString(R.string.show_otp));
                   }
               },60000);
           }
           else{
               animateout();
               shown=false;
               showotpBtn.setText(getString(R.string.show_otp));
           }
        });
        /**
         * This method will generate a QR Code whit the account name and pre-shared key
         */
//        generateBtn.setOnClickListener(v -> {
//            String info = TimeBasedOneTimePasswordUtil.qrImageUrl(nameBox,base32Secret);
//            Uri webAddress = Uri.parse(info);
//            Intent goToSite = new Intent(Intent.ACTION_VIEW, webAddress);
//            if (goToSite.resolveActivity(getPackageManager()) != null){
//                startActivity(goToSite);
//            }
//        });
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        logoutBtn.setOnClickListener(v-> {
//            deletePost();
//            editor.putBoolean("logged", false);
//            editor.apply();
//            Intent intent = new Intent (this, LoginActivity.class);
//            startActivity(intent);
//            finish();
//        });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void animatein(){
        //showotpBtn.animate().translationY(-ymove).setDuration(1500);
        //accNameTxt.animate().translationY(-ymove).setDuration(1500);
        otpTxt.animate().translationY(-ymove).setDuration(1500);
        progressBar.animate().translationY(-ymove).setDuration(1500);
        //scrollView.animate().translationY(-ymove).setDuration(1500);
    }

    public void animateout(){
        //showotpBtn.animate().translationY(ymove/2).setDuration(1500);
        //accNameTxt.animate().translationY(ymove/2).setDuration(1500);
        otpTxt.animate().translationY(ymove/2).setDuration(1500);
        progressBar.animate().translationY(ymove/2).setDuration(1500);
        //scrollView.animate().translationY(ymove/2).setDuration(1500);
    }

    public void setProgressBar(int progress){

        progressBar.setMax(30);
        //progressBar.setInterpolator(new AccelerateDecelerateInterpolator());
        progressBar.setProgress(progress);
    }

    private void deletePost(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String urlText = EncryptionUtils.decrypt(OTPActivity.this, sharedPreferences.getString(URL_TEXT, ""));
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        Gson gson = new GsonBuilder().serializeNulls().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlText)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
        UserService api = retrofit.create(UserService.class);
        Call<Void> call = api.logout("Mozilla/5.0");
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("logged", false);
                editor.apply();
                OTPActivity.super.onBackPressed();
                Toast.makeText(OTPActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(OTPActivity.this, "Code on failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLanguage(String localeCode) {
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(localeCode.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
        onConfigurationChanged(configuration);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // refresh your views here
        //accNameTxt.setText(getString(R.string.your_otp_is)+nameBox);
        connected_title.setText(getString(R.string.connected));
        if(!shown){
            showotpBtn.setText(getString(R.string.show_otp));
        }
        else{
            showotpBtn.setText(getString(R.string.hide_otp));
        }
        disconnect_button.setText(getString(R.string.disconnect));
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        server_box.setText(getString(R.string.server)+ ": " + EncryptionUtils.decrypt(OTPActivity.this, sharedPreferences.getString(URL_TEXT, "")));
        onBoardingTime_box.setText(getString(R.string.time)+ ": " + sharedPreferences.getString("timestamp", ""));
        version_box.setText(getString(R.string.version)+ ": 1.0");
        Menu menu =  navigationView.getMenu();
        menu.findItem(R.id.english).setTitle(R.string.english);
        menu.findItem(R.id.spanish).setTitle(R.string.spanish);
        menu.findItem(R.id.portuguese).setTitle(R.string.portuguese);
        menu.findItem(R.id.language_title).setTitle(R.string.language);
        account_name.setText(getString(R.string.authenticator_app));
        super.onConfigurationChanged(newConfig);
        // Checks the active language
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

    public void getConfiguration(){
        try {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            String urlText = EncryptionUtils.decrypt(OTPActivity.this, sharedPreferences.getString(URL_TEXT, ""));
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(urlText)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            UserService api = retrofit.create(UserService.class);
            Call<User> call = api.getConfiguration(useragent);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {

                    if (!response.isSuccessful()) {
                        System.out.println("Operation not successful because " + response.code() +
                                " " + response.message() + " " + response.body());
                        Toast.makeText(OTPActivity.super.getBaseContext(), "Here " + response.code() +
                                " " + response.message() + " " + response.body(), Toast.LENGTH_SHORT).show();
                        return;

                    }
                    //initialising the user class to get response body and get the various values needed
                    User postResponse = response.body();
                    String rssFeed = postResponse.getValue();
                    address = rssFeed.replaceAll("rssUrl=feed","http");
                    System.out.println("response value is: " + address);
                    if(address!=null) {
                        RssReader rssReader = new RssReader(OTPActivity.this, recyclerView, address);
                        rssReader.execute();
                    }
                    else{
                        Toast.makeText(OTPActivity.this,"Server Bad Response",Toast.LENGTH_SHORT).show();
                    }

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
            Toast.makeText(OTPActivity.this,"No Internet", Toast.LENGTH_SHORT).show();
        }
    }

}


