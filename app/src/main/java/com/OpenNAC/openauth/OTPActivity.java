package com.OpenNAC.openauth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.drawerlayout.widget.DrawerLayout;

import com.OpenNAC.openauth.Services.UserService;
import com.OpenNAC.openauth.remote.EncryptionUtils;
import com.OpenNAC.openauth.remote.TimeBasedOneTimePasswordUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.GeneralSecurityException;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OTPActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    TextView accNameTxt,otpTxt,account_name;
    private Date date;
    static String code,base32Secret, nameBox;
    private static final String SHARED_PREFS = "sharedPrefs",URL_TEXT = "urlBox";
    ProgressBar progressBar;
    Button showotpBtn;
    boolean shown;
    int ymove= 500;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
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
        accNameTxt = findViewById(R.id.accNameTxt);
        //toolbar
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView = findViewById(R.id.nav_view);
        toolbar =  findViewById(R.id.toolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.inflateHeaderView(R.layout.header);
        account_name = (TextView) header.findViewById(R.id.account_name);
        account_name.setText("HELLO");
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        //toolbar
        otpTxt = findViewById(R.id.otpTxt);
        progressBar = findViewById(R.id.progressBar);
        showotpBtn = findViewById(R.id.showotpBtn);
        base32Secret = EncryptionUtils.decrypt(OTPActivity.this, sharedPreferences.getString("secret", ""));
        nameBox = EncryptionUtils.decrypt(OTPActivity.this, sharedPreferences.getString("account", ""));
        code = null;

                try {
                    code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

/// setting the Timer and making sure updates every 30 seconds
        code = code.substring(0, 3) + "-" + code.substring(3, code.length());
        otpTxt.setText(code);
        accNameTxt.setText("Your OTP is: "+nameBox);
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
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       animateout();
                   }
               },60000);
           }
           else{
               animateout();
               shown=false;
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
    public void animatein(){
        //showotpBtn.animate().translationY(-ymove).setDuration(1500);
        accNameTxt.animate().translationY(-ymove).setDuration(1500);
        otpTxt.animate().translationY(-ymove).setDuration(1500);
        progressBar.animate().translationY(-ymove).setDuration(1500);
    }
    public void animateout(){
        //showotpBtn.animate().translationY(ymove/2).setDuration(1500);
        accNameTxt.animate().translationY(ymove/2).setDuration(1500);
        otpTxt.animate().translationY(ymove/2).setDuration(1500);
        progressBar.animate().translationY(ymove/2).setDuration(1500);
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
                Toast.makeText(OTPActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(OTPActivity.this, "Code on failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }
}


