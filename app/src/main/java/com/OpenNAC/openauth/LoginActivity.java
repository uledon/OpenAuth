package com.OpenNAC.openauth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

    /**
     * The following code is run when the page is created
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        //result = findViewById(R.id.result);
        spanish = getIntent().getBooleanExtra("spanishset",false);
        edtUsername = findViewById(R.id.usernameBox);
        edtPassword = findViewById(R.id.passwordBox);
        urlBox = findViewById(R.id.URLBox);
        rememberMeBox = findViewById(R.id.rememberMeBox);
        btnLogin = findViewById(R.id.loginBtn);
//        passTxt = findViewById(R.id.passTxt);
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        System.out.println( "ssid in login screen is " + DataClass.getSsid(wifiInfo) + "\n");
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

//        if (spanish){
//            urlBox.setHint("Ingrese la URL aquí");
//            edtUsername.setHint("Ingrese su Nombre");
//            passTxt.setText("Contraseña");
//            edtPassword.setHint("Ingerese su contraseña");
//            btnLogin.setText("Entrar");
//            rememberMeBox.setText("Recuérdame");
//        }

        enableFingerprint();
        btnLogin.setOnClickListener(v -> {
            url = urlBox.getText().toString();
            if (!urlBox.getText().toString().endsWith("/")) {
                url = urlBox.getText().toString() + "/";
            }
            editor.putString(URL_TEXT, EncryptionUtils.encrypt(LoginActivity.this,urlBox.getText().toString()));
            editor.apply();
            String username = edtUsername.getText().toString();
            String password = edtPassword.getText().toString();
            try {
                createPost(url, username, password, v);
                //localUserSwitch.resetPivot();
            } catch (IllegalArgumentException ex) {
                System.out.println("catch is wrong" + ex.getMessage());
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
        if (validateLogin(url, username, password)) {
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
     *1
     * @param url,
     * @param password
     * @param, username,
     */
    private boolean validateLogin(String url, String username, String password) {
        if (!URLUtil.isValidUrl(url)) {
            Toast.makeText(this, "URL is not valid", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (username == null || username.trim().length() == 0) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password == null || password.trim().length() == 0) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        urlText = EncryptionUtils.decrypt(LoginActivity.this, sharedPreferences.getString(URL_TEXT, ""));
        usernameText = EncryptionUtils.decrypt(LoginActivity.this, sharedPreferences.getString(USERNAME_TEXT, ""));
        passwordText = EncryptionUtils.decrypt(LoginActivity.this, sharedPreferences.getString(PASSWORD_TEXT, ""));
        switchOnOff = sharedPreferences.getBoolean(SWITCH1, false);
    }

    public void updateViews() {
        urlBox.setText(urlText);
        edtUsername.setText(usernameText);
        edtPassword.setText(passwordText);
        System.out.println("update data is: " + usernameText);
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

                            setStatus("Necesita permiso");

                            setStatus("Need permission");

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
                        setStatus("Success !");
                        runOnUiThread(() -> setScreen());
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        setStatus("not recognized");
                    }
                }, null);
            } else {
                setStatus("No fingerprint saved");
            }
        } else {
            setStatus("No fingerprint reader detected");
        }
    }

    private void setScreen(){
        Intent intent = new Intent(this, OTPActivity.class);
        startActivity(intent);
        finish();
    }
    private void setStatus(final String message) {
        runOnUiThread(() -> System.out.println(message));
    }

}
