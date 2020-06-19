package com.OpenNAC.openauth;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.OpenNAC.openauth.Services.Post;
import com.OpenNAC.openauth.Services.UserService;
import com.OpenNAC.openauth.remote.EncryptionUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    /**
     * This section is used to declared global variables
     */
    // The following Strings are used by SharedPreferences
    // They are used so differentiate the different stored values
    private static final String SHARED_PREFS = "sharedPrefs", URL_TEXT = "urlBox",
            USERNAME_TEXT = "edtusername", PASSWORD_TEXT = "edtpassword", SWITCH1 = "switch1", useragent = "Mozilla/5.0";

    Button btnLogin; // This is the button user clicks to log in
    //private TextView passTxt; // Used to view if various actions are successful or not
    private String url, urlText, usernameText, passwordText; // variables used to store data from fields
    private EditText urlBox, edtUsername, edtPassword; // These are the fields where user enters data
    private CheckBox rememberMeBox; // check box to remember details
    private boolean switchOnOff; // variable to store boolean value of rememberMeBox CheckBox variable
    protected static boolean loggedin,spanish;
    ///
    private FingerprintManager fingerprintManager; // This is used to initialise the fingerprint Manager
    TextView biometric_text;
    /**
     * The following code is run when the page is created
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        spanish = getIntent().getBooleanExtra("spanishset",false);
        edtUsername = findViewById(R.id.usernameBox);
        edtPassword = findViewById(R.id.passwordBox);
        urlBox = findViewById(R.id.URLBox);
        rememberMeBox = findViewById(R.id.rememberMeBox);
        btnLogin = findViewById(R.id.loginBtn);
        biometric_text = findViewById(R.id.biometric_text);
        //pre-text setting
        urlBox.setText("https://");
        //fingerprint managing
        Executor executor = Executors.newSingleThreadExecutor();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        loggedin = sharedPreferences.getBoolean("logged",false);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(this)
                    .setTitle(getString(R.string.waiting_for_auth))
                    .setSubtitle("Biometric Authentication")
                    .setDescription("")
                    .setNegativeButton("Cancel", executor, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).build();
            if(loggedin) {
                biometricPrompt.authenticate(new CancellationSignal(), executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        runOnUiThread(() -> setScreen());

                    }
                });
            }
        }
        else{
            enableFingerprint();
        }

        btnLogin.setOnClickListener(v -> {
            url = urlBox.getText().toString();
            if (!urlBox.getText().toString().endsWith("/")) {
                url = urlBox.getText().toString() + "/";
            }
            String username = edtUsername.getText().toString();
            String password = edtPassword.getText().toString();
            try {
                createPost(url, username, password, v);
            } catch (IllegalArgumentException ex) {
                System.out.println("catch is wrong" + ex.getMessage());
                Toast.makeText(LoginActivity.this,ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This method will be called when the login button is clicked
     *
     * @param url              gets the url from the form
     * @param username         gets the username from the form
     * @param password         gets the password from the form
     * @param view             gets the view to change.
     */
    public void createPost(String url, String username, String password, View view) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        if (validateLogin()) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            UserService api = retrofit.create(UserService.class);
        RequestBody usernamePart = RequestBody.create(MultipartBody.FORM, username);
        RequestBody passwordPart = RequestBody.create(MultipartBody.FORM, password);

            Call<Post> call = api.createPost(useragent,usernamePart,passwordPart);
            //call.enqueue is an asynchronous call without slowing main thread
            call.enqueue(new Callback<Post>() {
                @Override
                public void onResponse(Call<Post> call, Response<Post> response) {
                    if (!response.isSuccessful()) {
                        //result.setText("Code:" + response.code());
                        System.out.println("Code: " + response.code());
                        Toast.makeText(LoginActivity.this,response.code(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Post postResponse = response.body();
                    Headers headers = response.headers();
                    String cookie = response.headers().get("Set-Cookie");
                    String content = "";
                    content += "result: " + postResponse.getResult() + "\n";
                    content += "token: " + postResponse.getToken() + "\n";
                    content += "mail: " + postResponse.getMail();
                    //result.setText(content);
                    System.out.println(content);

                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("cookie", EncryptionUtils.encrypt(LoginActivity.this,cookie));
                    editor.putString("username", EncryptionUtils.encrypt(LoginActivity.this,username));
                    editor.putString("token", EncryptionUtils.encrypt(LoginActivity.this,postResponse.getToken()));
                    editor.putString(URL_TEXT, EncryptionUtils.encrypt(LoginActivity.this,url));
                    editor.apply();
                    Intent myIntent = new Intent(view.getContext(), VerificationActivity.class);
                    myIntent.putExtra("baseURL",url);
                    myIntent.putExtra("username", username );
                    myIntent.putExtra("token", postResponse.getToken());
                    myIntent.putExtra("spanishset",spanish);
                    if (rememberMeBox.isChecked()) {
                    myIntent.putExtra("logged",true);
                    }
                    startActivityForResult(myIntent, 0);
                    finish();
                }

                @Override
                public void onFailure(Call<Post> call, Throwable t) {
                    //result.setText("on failure " + t.getMessage());
                    System.out.println("on failure " + t.getMessage());
                }
            });
        }
    }

    /**
     * This following method will make sure the fields below are not empty
     */
    private boolean validateLogin() {

        if (urlBox.getText() == null || urlBox.getText().length() == 0) {
            Toast.makeText(this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (edtUsername.getText() == null || edtUsername.getText().length() == 0) {
            Toast.makeText(this, getString(R.string.username_required), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (edtPassword.getText() == null || edtPassword.getText().length() == 0) {
            Toast.makeText(this, getString(R.string.password_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void enableFingerprint(){
        ///// Fingerprint enabling start /////
        fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
        Dexter.withActivity(this).withPermission(Manifest.permission.USE_FINGERPRINT).withListener(
                new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
                        loggedin = sharedPreferences.getBoolean("logged",false);
                        if(loggedin) {
                                setStatus(getString(R.string.waiting_for_auth));
                            auth();
                        }
                        else{
                            setStatus(getString(R.string.no_registered_details));
                        }

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                            setStatus(getString(R.string.need_permission));


                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
        ///// Fingerprint enabling end /////
    }
    //this method is used to authenticate the fingerprint. It will run some checks before hand and
    //make sure that there is a method to authenticate with fingerprint.
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void auth() {
        if (fingerprintManager.isHardwareDetected()) {
            if (fingerprintManager.hasEnrolledFingerprints()) {
                fingerprintManager.authenticate(null, null, 0, new FingerprintManager.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        setStatus(errString.toString());
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                        super.onAuthenticationHelp(helpCode, helpString);
                        setStatus(helpString.toString());
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        setStatus(getString(R.string.success));
                        runOnUiThread(() -> setScreen());
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        setStatus(getString(R.string.not_recognized));
                    }
                }, null);
            } else {
                setStatus(getString(R.string.no_fingerprint));
            }
        } else {
            setStatus(getString(R.string.no_finger_reader));
        }
    }

    private void setScreen(){
        Intent intent = new Intent(this, OTPActivity.class);
        startActivity(intent);
        finish();
    }

    private void setStatus(final String message) {
        runOnUiThread(() -> biometric_text.setText(message));
    }

}
