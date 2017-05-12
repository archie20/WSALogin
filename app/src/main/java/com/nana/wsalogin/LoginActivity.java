package com.nana.wsalogin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nana.wsalogin.helpers.ApiUrls;
import com.nana.wsalogin.helpers.Constants;
import com.nana.wsalogin.helpers.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    EditText emailET,passwordET,nameET;
    Button loginBtn,signUpBtn;
    private static final int LOGIN =1;
    private static final int SIGNUP=2;
    private int loginType=0;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //To hide the status bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);


        nameET = (EditText) findViewById(R.id.name_text);
        emailET = (EditText) findViewById(R.id.email_text);
        passwordET = (EditText) findViewById(R.id.password_text);
         loginBtn = (Button) findViewById(R.id.login_button);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginType=LOGIN;
                loginSignUp();
            }
        });
        signUpBtn = (Button) findViewById(R.id.sig_up_button);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginType=SIGNUP;
                loginSignUp();
            }
        });

    }

    private void loginSignUp() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        String url = "";
        if(! checkParams())
            return;

        JSONObject jsonObject = new JSONObject();
        try {
            if(loginType == SIGNUP) {
                progressDialog.setTitle(getString(R.string.signup_prog));
                progressDialog.show();
                jsonObject.put("name", nameET.getText().toString().trim());
                url = ApiUrls.SIGNUP_URL;
            }else{
                progressDialog.setTitle(getString(R.string.login_prog));
                progressDialog.show();
                url = ApiUrls.LOGIN_URL;
            }
            jsonObject.put("email", emailET.getText().toString().trim());
            jsonObject.put("password",passwordET.getText().toString().trim());

        } catch (JSONException e) {
            progressDialog.dismiss();
            createErrorAlert(getString(R.string.error_txt),getString(R.string.json_ext)+"\n"+e.getMessage());
            e.printStackTrace();
        }

        if(!TextUtils.isEmpty(url) && jsonObject!=null)
            makeRequest(url,jsonObject);
    }


    private void makeRequest(String url,JSONObject jsonObject){
        JsonObjectRequest request =  new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                                                            new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    String message = response.getString("message");
                    getSharedPreferences(Constants.SHARED_PREFS,MODE_PRIVATE).edit()
                            .putString(Constants.TOKEN,response.getString("token"))
                            .apply();// Store the authentication api token for subsequent requests

                    createErrorAlert("Success",message); //You can replace this by going to the next activity
                } catch (JSONException e) {
                    createErrorAlert("Error","An error occurred. Please try again");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                String message="";
                try {
                    if (error.networkResponse.statusCode == 400)
                             message = getString(R.string.bad_request);
                    if(error.networkResponse.statusCode == 500)
                        message = getString(R.string.server_error);
                    if(error.networkResponse.statusCode == 401)
                        message = getString(R.string.bad_credentials);
                }catch (NullPointerException e){
                    message = getString(R.string.connection_error);
                }
                        createErrorAlert("Error",message);

            }
        });
        VolleySingleton.getInstance(LoginActivity.this).addToRequestQueue(request);
    }


    private boolean checkParams() {
        if(loginType==SIGNUP)
            if(TextUtils.isEmpty(nameET.getText().toString())) {
                nameET.setError("Provide name");
                return false;
            }

        if(TextUtils.isEmpty(emailET.getText().toString())) {
            emailET.setError("Provide email");
            return false;
        }
        if(!isPasswordValid(passwordET.getText().toString())){
            passwordET.setError("Provide password");
            return false;
        }

        return true;
    }


    private boolean isPasswordValid(String password) {
        return Pattern.matches("^[a-zA-Z0-9_!#$%-]{6,20}$",password);
    }



    private void createErrorAlert(String title,String message){
        final AlertDialog aDialog = new AlertDialog.Builder(LoginActivity.this).create();
        aDialog.setTitle(title);
        aDialog.setMessage(message);
        aDialog.setCancelable(false);
        aDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                aDialog.dismiss();
                //nameET.setText("");
               // passwordET.setText("");
               // emailET.setText("");

            }
        });
        aDialog.show();
    }
    
}
