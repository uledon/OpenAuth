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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.OpenNAC.openauth.remote.EncryptionUtils;
import com.OpenNAC.openauth.remote.Post;
import com.OpenNAC.openauth.remote.UserService;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

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
            USERNAME_TEXT = "edtusername", PASSWORD_TEXT = "edtpassword", SWITCH1 = "switch1";

    Button btnLogin; // This is the button user clicks to log in
    private TextView result; // Used to view if various actions are successful or not
    private String url, urlText, usernameText, passwordText; // variables used to store data from fields
    private EditText urlBox, edtUsername, edtPassword; // These are the fields where user enters data
    private Switch localUserSwitch; // Switch used to set if user is local or not
    private CheckBox rememberMeBox; // check box to remember details
    private boolean switchOnOff; // variable to store boolean value of rememberMeBox CheckBox variable
    ///
    private FingerprintManager fingerprintManager; // This is used to initialise the fingerprint Manager

    /**
     * The following code is run when the page is created
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        result = findViewById(R.id.result);

        edtUsername = findViewById(R.id.usernameBox);
        edtPassword = findViewById(R.id.passwordBox);
        urlBox = findViewById(R.id.URLBox);
        localUserSwitch = findViewById(R.id.localUserSwitch);
        rememberMeBox = findViewById(R.id.rememberMeBox);
        btnLogin = findViewById(R.id.loginBtn);
        enableFingerprint();
        btnLogin.setOnClickListener(v -> {
            url = urlBox.getText().toString();
            if (!urlBox.getText().toString().endsWith("/")) {
                url = urlBox.getText().toString() + "/";
            }
            System.out.println("url is:" + url);
            String username = edtUsername.getText().toString();
            String password = edtPassword.getText().toString();
            try {
                createPost(url, username, password, localUserSwitch.isChecked(), v);
                //localUserSwitch.resetPivot();
            } catch (IllegalArgumentException ex) {
                System.out.println(ex.getMessage());
            }

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void enableFingerprint(){
        ///// Fingerprint enabling start /////
        fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
        Dexter.withActivity(this).withPermission(Manifest.permission.USE_FINGERPRINT).withListener(
                new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setStatus("Waiting for authentication...");
                        auth();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        setStatus("Need permission");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
        ///// Fingerprint enabling end /////
    }
    /**
     * This method will be called when the login button is clicked
     *
     * @param url              gets the url from the form
     * @param username         gets the username from the form
     * @param password         gets the password from the form
     * @param useOnlyLocalRepo gets the boolean useOnlyLocalRepo
     * @param view             gets the view to change.
     */
    public void createPost(String url, String username, String password, boolean useOnlyLocalRepo, View view) {

        //Creating a retrofit instance to work on as this library is very easy to use.
        if (validateLogin(url, username, password)) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            UserService api = retrofit.create(UserService.class);
            Post post = new Post(username, password, useOnlyLocalRepo);
            Call<Post> call = api.createPost(post);
            //call.enqueue is an asynchronous call without slowing main thread
            call.enqueue(new Callback<Post>() {
                @Override
                public void onResponse(Call<Post> call, Response<Post> response) {
                    if (!response.isSuccessful()) {
                        result.setText("Code:" + response.code());
                        return;
                    }
                    Post postResponse = response.body();
                    String content = "";
                    content += "result: " + postResponse.getResult() + "\n";
                    content += "token: " + postResponse.getToken();
                    //content += "mac: " + postResponse.getMac();
                    result.setText(content);
                    if (rememberMeBox.isChecked()) {
                        saveData();
                    }
                    Intent myIntent = new Intent(view.getContext(), SharedKeyScreen.class);
                    startActivityForResult(myIntent, 0);
                }

                @Override
                public void onFailure(Call<Post> call, Throwable t) {
                    result.setText(t.getMessage());
                }
            });
        }
    }

    /**
     * This following method will make sure the fields below are not empty
     *
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

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(URL_TEXT, EncryptionUtils.encrypt(LoginActivity.this,urlBox.getText().toString()));
        editor.putString(USERNAME_TEXT, EncryptionUtils.encrypt(LoginActivity.this,edtUsername.getText().toString()));
//        String encryptedValue = EncryptionUtils.encrypt(LoginActivity.this,
//                edtPassword.getText().toString());
        editor.putString(PASSWORD_TEXT, EncryptionUtils.encrypt(LoginActivity.this,
                edtPassword.getText().toString()));
        editor.putBoolean(SWITCH1, localUserSwitch.isChecked());
        editor.apply();
        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        urlText = EncryptionUtils.decrypt(LoginActivity.this, sharedPreferences.getString(URL_TEXT, ""));
        usernameText = EncryptionUtils.decrypt(LoginActivity.this, sharedPreferences.getString(USERNAME_TEXT, ""));
//        System.out.println("load data is: "+sharedPreferences.getString(USERNAME_TEXT,""));
        passwordText = EncryptionUtils.decrypt(LoginActivity.this, sharedPreferences.getString(PASSWORD_TEXT, ""));

//        passwordText = EncryptionUtils.decrypt(this,sharedPreferences.getString(PASSWORD_TEXT,""));
        switchOnOff = sharedPreferences.getBoolean(SWITCH1, false);
//        System.out.println("the passwordText is: " + passwordText);
    }

    public void updateViews() {
        urlBox.setText(urlText);
        edtUsername.setText(usernameText);
        edtPassword.setText(passwordText);
        localUserSwitch.setChecked(switchOnOff);
        System.out.println("update data is: " + usernameText);
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
                        runOnUiThread(() -> loadData());
                        runOnUiThread(() -> updateViews());
                        runOnUiThread(() -> btnLogin.performClick());
                        runOnUiThread(() -> logIn());
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
    private void logIn(){

    }
    private void setStatus(final String message) {
        runOnUiThread(() -> result.setText(message));
    }

}
