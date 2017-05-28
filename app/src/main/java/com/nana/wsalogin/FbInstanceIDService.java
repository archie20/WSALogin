package com.nana.wsalogin;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.nana.wsalogin.helpers.ApiUrls;
import com.nana.wsalogin.helpers.Constants;
import com.nana.wsalogin.helpers.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by NanaYaw on 5/27/2017.
 */

public class FbInstanceIDService extends FirebaseInstanceIdService {
    String TAG = "FbInstanceIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        if(refreshedToken != null)
             sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        String url = ApiUrls.POST_DEVICE_TOKEN_URL+"?api_token="+getSharedPreferences(Constants.SHARED_PREFS,MODE_PRIVATE)
                                                    .getString(Constants.API_TOKEN,"");
        JSONObject obj = new JSONObject();
        try {
            obj.put("device_token",refreshedToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Succesfully posted");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Failed to post");
            }
        });
        VolleySingleton.getInstance(FbInstanceIDService.this).addToRequestQueue(request);
    }
}
