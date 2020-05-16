package com.OpenNAC.openauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.OpenNAC.openauth.Services.UserService;
import com.OpenNAC.openauth.remote.EncryptionUtils;
import com.OpenNAC.openauth.remote.TimeBasedOneTimePasswordUtil;
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

public class OTPScreen extends AppCompatActivity {
    TextView accNameTxt,otpTxt,countdownTxt;
    private Date date;
    static String code,base32Secret, nameBox;
    private static boolean spanish;
    private static final String SHARED_PREFS = "sharedPrefs",URL_TEXT = "urlBox";
    /**
     * This method will be run when the page is created
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_otpscreen);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Button generateBtn = findViewById(R.id.generateBtn);//
        Button logoutBtn = findViewById(R.id.logoutBtn);
        spanish = getIntent().getBooleanExtra("spanishset",false);
        if (spanish){
            generateBtn.setText("generar código qr");
        }
        accNameTxt = findViewById(R.id.accNameTxt);
        otpTxt = findViewById(R.id.otpTxt);
        countdownTxt = findViewById(R.id.countdownTxt);
        base32Secret = EncryptionUtils.decrypt(OTPScreen.this, sharedPreferences.getString("secret", ""));
        nameBox = EncryptionUtils.decrypt(OTPScreen.this, sharedPreferences.getString("account", ""));
        code = null;

                try {
                    code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

/// setting the Timer and making sure updates every 30 seconds
        otpTxt.setText(code);
        accNameTxt.setText(nameBox);
        Thread t = new Thread(){
            @Override
            public void run(){
                while(!isInterrupted()){
                    try {
                        Thread.sleep(1000);
//                        System.out.println(System.currentTimeMillis());
                        date = new Date();

                        runOnUiThread(() -> {

                            String datetxt;
                                try {
                                    code = TimeBasedOneTimePasswordUtil.generateCurrentNumberString(base32Secret);
                                    otpTxt.setText(code);
                                    if(date.getSeconds()>=30){
                                        date.setSeconds(date.getSeconds()-30);
                                    }
                                    datetxt = "" + date.getSeconds()+ " /30";
                                    countdownTxt.setText(datetxt);
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
        /**
         * This method will generate a QR Code whit the account name and pre-shared key
         */
        generateBtn.setOnClickListener(v -> {
            String info = TimeBasedOneTimePasswordUtil.qrImageUrl(nameBox,base32Secret);
            Uri webAddress = Uri.parse(info);
            Intent goToSite = new Intent(Intent.ACTION_VIEW, webAddress);
            if (goToSite.resolveActivity(getPackageManager()) != null){
                startActivity(goToSite);
            }
        });
        SharedPreferences.Editor editor = sharedPreferences.edit();
        logoutBtn.setOnClickListener(v-> {
            deletePost();
            editor.putBoolean("logged", false);
            editor.apply();
            Intent intent = new Intent (this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }
    private void deletePost(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String urlText = EncryptionUtils.decrypt(OTPScreen.this, sharedPreferences.getString(URL_TEXT, ""));
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
                Toast.makeText(OTPScreen.this, "Logged out", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(OTPScreen.this, "Code on failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}


