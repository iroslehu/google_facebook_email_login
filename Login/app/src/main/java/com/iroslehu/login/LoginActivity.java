package com.iroslehu.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class LoginActivity extends AppCompatActivity implements FacebookCallback<LoginResult> {
    private static final String TAG = "LoginActivity";
    private static final int EMAIL_SIGN_IN_REQUEST = 0;

    EditText _emailText;
    EditText _passwordText;
    Button _loginButton;
    Button _email_button;

    ProgressDialog progressDialog;
    SharedPreferences sharedpreferences;

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private GoogleSignInClient mGoogleSignInClient;

    private Integer GOOGLE_SIGN_IN = 12345;

    String type = "default";
    String name = "name";
    String email = "email";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedpreferences = getSharedPreferences("EMAIL", Context.MODE_PRIVATE);

        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);

        _loginButton =findViewById(R.id.btn_login);
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
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


    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

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

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

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

        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EMAIL_SIGN_IN_REQUEST && data != null) {
            if (resultCode == RESULT_OK) {

                type = "email";
                name = data.getStringExtra("name");
                email = data.getStringExtra("email");

                sharePreferenceData();
                onLoginSuccess();
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

    public void sharePreferenceData(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("type", type);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.apply();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {

                type = "google";
                name = account.getDisplayName();
                email = account.getEmail();

                String personGivenName = account.getGivenName();
                String personFamilyName = account.getFamilyName();
                String personId = account.getId();
                Uri personPhoto = account.getPhotoUrl();

                Log.e(TAG, "personName " + name);
                Log.e(TAG, "personEmail " + email);
                Log.e(TAG, "personGivenName " + personGivenName);
                Log.e(TAG, "personFamilyName " + personFamilyName);
                Log.e(TAG, "personId " + personId);
                Log.e(TAG, "personPhoto " + personPhoto);

                sharePreferenceData();
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

                            type = "facebook";
                            name = object.getString("name");
                            email = object.getString("email");

                            sharePreferenceData();
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

    @Override
    protected void onStart() {
        super.onStart();
        if(!TextUtils.isEmpty(sharedpreferences.getString("name", "")) || !TextUtils.isEmpty(sharedpreferences.getString("email", "")) ){
            onLoginSuccess();
            Log.e(TAG, "EMAIL LOGIN");
        }else{

            // Check for existing Google Sign In account, if the user is already signed in
            // the GoogleSignInAccount will be non-null.
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

            // Check for existing Facebook Sign In account, if the user is already signed in
            // the AccessToken will be non-null.
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

            if(account != null){
                onLoginSuccess();
                Log.e(TAG, "GOOGLE LOGIN");
            }else if (isLoggedIn){
                onLoginSuccess();
                Log.e(TAG, "FACEBOOK LOGIN");
            }

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}