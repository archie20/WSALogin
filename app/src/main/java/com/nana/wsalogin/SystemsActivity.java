package com.nana.wsalogin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nana.wsalogin.adapters.SystemsListClickListener;
import com.nana.wsalogin.adapters.SystemsRecyclerViewAdapter;
import com.nana.wsalogin.helpers.ApiUrls;
import com.nana.wsalogin.helpers.Constants;
import com.nana.wsalogin.helpers.Systems;
import com.nana.wsalogin.helpers.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SystemsActivity extends AppCompatActivity implements SystemsListClickListener{
    RecyclerView sysList;
    List<Systems> systemsList;
    SystemsListClickListener listener;
    ProgressDialog progDiag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_systems);
        listener =this;
        systemsList = new ArrayList<>();
        sysList = (RecyclerView) findViewById(R.id.sys_list);
        sysList.setLayoutManager(new LinearLayoutManager(SystemsActivity.this));
        sysList.setAdapter(new SystemsRecyclerViewAdapter(SystemsActivity.this,systemsList,listener));
        progDiag= new ProgressDialog(SystemsActivity.this);
        progDiag.setIndeterminate(true);
        progDiag.setCancelable(false);
        progDiag.setMessage("Please wait.. Getting your Systems");
        progDiag.show();
        getSystems();

    }





    public void getSystems(){
        String token = getSharedPreferences(Constants.SHARED_PREFS,MODE_PRIVATE)
                            .getString(Constants.API_TOKEN,"");
        String url = ApiUrls.POST_USER_SYSTEMS_URL+"?"+Constants.API_TOKEN+"="+token;


        Log.d("SYSTEMSACTIVITY","in getSystems() function");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("user_systems");

                    for(int i=0;i<jsonArray.length();i++){
                        Systems system =  new Systems();
                        system.setLocation(jsonArray.getJSONObject(i).getString("device_location"))
                                .setPlant(jsonArray.getJSONObject(i).getString("plant_name"))
                                .setSystemId(jsonArray.getJSONObject(i).getInt("id"))
                                .setStatus(jsonArray.getJSONObject(i).getInt("isActivated")==1);
                        systemsList.add(system);
                        Log.d("SYSTEMSACTIVITY","in for loop");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progDiag.dismiss();
                        }
                    });
                    sysList.getAdapter().notifyDataSetChanged();
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progDiag.dismiss();
                            createErrorDialog("Sorry","Please close the app and try again");
                        }
                    });
                    Log.d("SYSTEMSACTIVITY",e.getMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progDiag.dismiss();
                    }
                });
                try{
                    if(error.networkResponse.statusCode == 400)
                        createErrorDialog("Missing key","Authentication failed!. Login again");
                    else if(error.networkResponse.statusCode == 401)
                        createErrorDialog("Unauthorized","Authentication failed!. Login again");
                    else if(error.networkResponse.statusCode == 500)
                        createErrorDialog("Server Error","Something wrong with the servers, Try again");
                    else
                        createErrorDialog("Connection Error","Connection Error, Try again");
                }catch (NullPointerException e){
                    createErrorDialog("Connection Error","Connection Error, Try again");
                }
            }
        });


        VolleySingleton.getInstance(SystemsActivity.this).addToRequestQueue(request);
    }

    private void displayEmptyPage() {

    }

    private void createErrorDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(SystemsActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
        displayEmptyPage();
    }

    @Override
    public void onSystemClick(Systems system) {
        Intent intent = new Intent(SystemsActivity.this,SystemOverviewActivity.class);
        intent.putExtra("systemId",system.getSystemId());
        startActivity(intent);
    }
}
