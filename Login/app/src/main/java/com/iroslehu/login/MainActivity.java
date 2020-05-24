package com.iroslehu.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    static public String TAG = "MainActivity";
    private String _pictureUrl = "https://lh3.googleusercontent.com/proxy/iHp-OCR0B_hUX18xFr5fkbpkddHLiDngJQg3GSmiEm2jWeo21Pm_mcUJ1XVikf6TPzVQBeWrLAlF_t1WeDkMiWVVeddhaAzEXhESqa5GTXj13LaoLyQ";

    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(getApplicationContext().getPackageName() + ".email", MODE_PRIVATE);

        ImageView img_profile = findViewById(R.id.img_profile);
        TextView txt_name = findViewById(R.id.txt_name);
        TextView txt_email = findViewById(R.id.txt_email);
        TextView txt_type = findViewById(R.id.txt_type);

        if (sharedpreferences.getBoolean("login", false)){
            txt_name.setText(sharedpreferences.getString("name", "name"));
            txt_email.setText(sharedpreferences.getString("email", "email"));
        }else {
            Picasso.get().load(sharedpreferences.getString("social_picture", _pictureUrl)).into(img_profile);
            txt_name.setText(sharedpreferences.getString("social_name", "name"));
            txt_email.setText(sharedpreferences.getString("social_email", "email"));
        }

        txt_type.setText(sharedpreferences.getString("type", "type"));



        Button btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void signOut() {
        sharePreferenceLogOut();
        LoginManager.getInstance().logOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    public void sharePreferenceLogOut(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("login", false);
        editor.apply();
    }

}
