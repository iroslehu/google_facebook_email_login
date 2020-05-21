package com.iroslehu.login;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity implements FacebookCallback<LoginResult> {
    private static final String TAG = "LoginActivity";
    private static final int EMAIL_SIGN_IN_REQUEST = 0;

    private EditText _emailText;
    private EditText _passwordText;
    private Button _loginButton;
    private Button _email_button;
    private CheckBox cbx_fingerprint;
    private ImageButton biometricLoginButton;

    private ProgressDialog progressDialog;
    private SharedPreferences sharedpreferences;

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private GoogleSignInClient mGoogleSignInClient;

    private Integer GOOGLE_SIGN_IN = 12345;

    private Boolean _login = false;
    private String _type = "";
    private String _name = "";
    private String _social_name = "";
    private String _email = "";
    private String _social_email = "";
    private String _password = "";
    private Boolean _fingerprint = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedpreferences = getSharedPreferences(getApplicationContext().getPackageName() + ".email", Context.MODE_PRIVATE);

        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        cbx_fingerprint = findViewById(R.id.cbx_fingerprint);

        _loginButton = findViewById(R.id.btn_login);
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!validateFilds()) {
                    onLoginFailed();
                }else {
                    if (cbx_fingerprint.isChecked()){
                        showBiometricPrompt();
                    }else {
                        login();
                    }
                }
            }
        });

        _email_button = findViewById(R.id.email_button);
        _email_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, EMAIL_SIGN_IN_REQUEST);
            }
        });

        //------------------------START FACEBOOK LOGIN
        callbackManager = CallbackManager.Factory.create();

        loginButton = findViewById(R.id.login_button);
        //loginButton.setReadPermissions("email");
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));
        // Callback registration
        loginButton.registerCallback(callbackManager, this);
        //----------------------END FACEBOOK LOGIN

        //----------------------START GOOGLE LOGIN
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
            }
        });
        //-------------------END GOOGLE LOGIN

        // Prompt appears when user clicks "Log in"
        biometricLoginButton = findViewById(R.id.img_login);
        biometricLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (canAuthenticateWithBiometrics()) {  // Check whether this device can authenticate with biometrics
                    // Create biometricPrompt
                    if (_fingerprint) {
                        showBiometricPrompt();
                    } else {
                        Toast.makeText(getApplicationContext(), "Login with an account and anable fingerprint ", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        _loginButton.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        _login = true;
                        _type = "email";
                        sharePreferenceEmail();
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    public void loginFingerPrint() {
        Log.d(TAG, "LoginFingerPrint");

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        // TODO: Implement your own authentication logic here.
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        _login = true;
                        _type = "email";
                        sharePreferenceEmail();
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    public void onLoginSuccess() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validateFilds() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.equals(_email) && password.equals(_password)) {

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _emailText.setError("enter a valid email address");
                valid = false;
            } else {
                _emailText.setError(null);
            }

            if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
                _passwordText.setError("between 4 and 10 alphanumeric characters");
                valid = false;
            } else {
                _passwordText.setError(null);
            }



        } else {
            valid = false;
        }

        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EMAIL_SIGN_IN_REQUEST && data != null) {
            if (resultCode == RESULT_OK) {

                _login = true;
                _type = "email";
                _name = data.getStringExtra("name");
                _email = data.getStringExtra("email");
                _password = data.getStringExtra("password");

                sharePreferenceEmail();
            }
        }

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    public void sharePreferenceSocialEmail() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("type", _type);
        editor.putString("social_name", _social_name);
        editor.putString("social_email", _social_email);
        editor.apply();
    }

    public void sharePreferenceEmail() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("type", _type);
        editor.putString("name", _name);
        editor.putString("email", _email);
        editor.putString("password", _password);
        editor.putBoolean("login", _login);
        if (cbx_fingerprint.isChecked()) {
            editor.putBoolean("fingerprint", true);
        }
        editor.apply();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {

                _type = "google";
                _social_name = account.getDisplayName();
                _social_email = account.getEmail();

                String personGivenName = account.getGivenName();
                String personFamilyName = account.getFamilyName();
                String personId = account.getId();
                Uri personPhoto = account.getPhotoUrl();

                Log.e(TAG, "personName " + _social_name);
                Log.e(TAG, "personEmail " + _social_email);
                Log.e(TAG, "personGivenName " + personGivenName);
                Log.e(TAG, "personFamilyName " + personFamilyName);
                Log.e(TAG, "personId " + personId);
                Log.e(TAG, "personPhoto " + personPhoto);

                sharePreferenceSocialEmail();
                onLoginSuccess();
            }

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }


    @Override
    public void onSuccess(LoginResult loginResult) {
        // App code
        Log.e(TAG, "onSuccess " + loginResult.getAccessToken().getUserId());

        AccessToken accessToken = loginResult.getAccessToken();
        Profile profile = Profile.getCurrentProfile();

        // Facebook Email address
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        try {

                            _type = "facebook";
                            _social_name = object.getString("name");
                            _social_email = object.getString("email");

                            sharePreferenceSocialEmail();
                            onLoginSuccess();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, email, birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onCancel() {
        // App code
        Log.e(TAG, "onCancel " + "CANCELADO");
    }

    @Override
    public void onError(FacebookException exception) {
        // App code
        Log.e(TAG, "onError " + exception);
    }


    private Boolean checkBiometricSupport() {

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        PackageManager packageManager = this.getPackageManager();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG,"This Android version does not support fingerprint authentication.");
            return false;
        }

        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT))
        {
            Log.e(TAG,"Fingerprint Sensor not supported");
            return false;
        }

        if (!keyguardManager.isKeyguardSecure()) {
            Log.e(TAG,"Lock screen security not enabled in Settings");

            return false;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG,"Fingerprint authentication permission not enabled");

            return false;
        }

        return true;
    }

    /**
     * Indicate whether this device can authenticate the user with biometrics
     *
     * @return true if there are any available biometric sensors and biometrics are enrolled on the device, if not, return false
     */
    private boolean canAuthenticateWithBiometrics() {
        // Check whether the fingerprint can be used for authentication (Android M to P)
        if (Build.VERSION.SDK_INT < 29) {
            FingerprintManagerCompat fingerprintManagerCompat = FingerprintManagerCompat.from(this);
            return fingerprintManagerCompat.hasEnrolledFingerprints() && fingerprintManagerCompat.isHardwareDetected();
        } else {    // Check biometric manager (from Android Q)
            BiometricManager biometricManager = this.getSystemService(BiometricManager.class);
            if (biometricManager != null) {
                return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
            }
            return false;
        }
    }

    private Handler handler = new Handler();

    private Executor executor = new Executor() {
        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    };

    private void showBiometricPrompt() {

        String emailFild = _emailText.getText().toString().trim();
        String email = TextUtils.isEmpty(emailFild) ? _email : emailFild;

        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("LOGIN")
                        .setSubtitle(email)
                        .setDescription("Do you want to login with this email?")
                        .setNegativeButtonText("Cancel")
                        .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                BiometricPrompt.CryptoObject authenticatedCryptoObject = result.getCryptoObject();
                    loginFingerPrint();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Displays the "log in" prompt.
        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart()");

        _login = sharedpreferences.getBoolean("login", false);
        _type = sharedpreferences.getString("type", "");
        _name = sharedpreferences.getString("name", "");
        _social_name = sharedpreferences.getString("social_name", "");
        _email = sharedpreferences.getString("email", "");
        _social_email = sharedpreferences.getString("social_email", "");
        _password = sharedpreferences.getString("password", "");
        _fingerprint = sharedpreferences.getBoolean("fingerprint", false);

        if (_fingerprint) {
            biometricLoginButton.setColorFilter(getApplicationContext().getResources().getColor(R.color.colorPrimary));
        }

        if (_login && _type.equals("email")) {
            onLoginSuccess();
            Log.e(TAG, "EMAIL LOGIN");
        } else {

            // Check for existing Google Sign In account, if the user is already signed in
            // the GoogleSignInAccount will be non-null.
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

            // Check for existing Facebook Sign In account, if the user is already signed in
            // the AccessToken will be non-null.
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

            if (account != null) {
                onLoginSuccess();
                Log.e(TAG, "GOOGLE LOGIN");
            } else if (isLoggedIn) {
                onLoginSuccess();
                Log.e(TAG, "FACEBOOK LOGIN");
            }

        }
        if (!checkBiometricSupport())cbx_fingerprint.setVisibility(View.GONE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, " onStop()");
    }


}