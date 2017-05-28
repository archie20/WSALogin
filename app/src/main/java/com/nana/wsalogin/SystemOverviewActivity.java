package com.nana.wsalogin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nana.wsalogin.helpers.ApiUrls;
import com.nana.wsalogin.helpers.Constants;
import com.nana.wsalogin.helpers.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class SystemOverviewActivity extends AppCompatActivity {
    TextView tempText,moistureText,pumpStatusText;
    RelativeLayout pumpLayoutBg;
    LineChartView moistureChart;
    LinearLayout mainLayout;
    ProgressDialog progDiag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_overview);
        inflateViews();
        placeIcons();
        progDiag = new ProgressDialog(SystemOverviewActivity.this);
        progDiag.setIndeterminate(true);
        progDiag.setCancelable(false);
        progDiag.setTitle("Wait..");
        progDiag.show();

        getRecordedInfo();
        getSystemInfo();


    }

    private void getSystemInfo() {
        String url = ApiUrls.POST_USER_SINGLE_SYSTEM_URL+getIntent().getIntExtra("systemId",0)+"?api_token="+
                getSharedPreferences(Constants.SHARED_PREFS,MODE_PRIVATE)
                        .getString(Constants.API_TOKEN,"");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject obj = response.getJSONObject("system");
                    final int pumpStatus = obj.getInt("pump_status");
                    progDiag.dismiss();
                    mainLayout.setVisibility(View.VISIBLE);
                    setPumpValue(pumpStatus);
                    
                    pumpLayoutBg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pumpControl(pumpStatus==1);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       progDiag.dismiss();
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


            }
        });
        VolleySingleton.getInstance(SystemOverviewActivity.this).addToRequestQueue(request);
    }

    private void getRecordedInfo() {
        String url = ApiUrls.POST_USER_RECORDED_SYSTEMS_URL+getIntent().getIntExtra("systemId",0)+"?api_token="+
                                    getSharedPreferences(Constants.SHARED_PREFS,MODE_PRIVATE)
                                    .getString(Constants.API_TOKEN,"");

        JsonObjectRequest jsonObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject response) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                progDiag.dismiss();
                                mainLayout.setVisibility(View.VISIBLE);


                               JSONArray jsonArray = response.getJSONArray("moist_vals");
                                JSONObject jObj = jsonArray.getJSONObject(jsonArray.length()-1); //get most recent reading which is at position 0
                                setTemperaturePanel(jObj.getString("temp_reading"));
                                setMoisturePanel(jObj.getString("moisture_value"));
                             //   populateChart(jsonArray);

                            } catch (JSONException e) {
                                progDiag.dismiss();
                                createErrorDialog("Error","Please try again");
                                e.printStackTrace();
                            }
                        }
                    });


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            progDiag.dismiss();
                            if(error.networkResponse.statusCode == 400)
                                createErrorDialog("Missing key","Authentication failed!. Login again");
                            else if(error.networkResponse.statusCode == 401)
                                createErrorDialog("Unauthorized","Authentication failed!. Login again");
                            else if(error.networkResponse.statusCode == 500)
                                createErrorDialog("Server Error","Something wrong with the servers, Try again");
                            else if(error.networkResponse.statusCode == 404)
                                createErrorDialog("Not found","This system has been deleted or not found");
                        }catch (NullPointerException e){
                            createErrorDialog("Connection Error","Connection Error, Try again");
                        }
                    }
                });

            }
        });
        VolleySingleton.getInstance(SystemOverviewActivity.this).addToRequestQueue(jsonObjRequest);
    }


    private void populateChart(JSONArray jsonArray) throws JSONException {


        SimpleDateFormat inFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        SimpleDateFormat outFormat = new SimpleDateFormat("d");

        List<PointValue> values = new ArrayList<>();
        for(int i = 0;i<jsonArray.length();i++){

            JSONObject obj = jsonArray.getJSONObject(i);
            try {
                Date date = inFormat.parse(obj.getString("time_recorded"));

                //values.add(new PointValue(Integer.parseInt(outFormat.format(date)),Float.parseFloat(obj.getString("moisture_value"))));
                Log.d("Values",obj.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        values.add(new PointValue(1,2));
        values.add(new PointValue(2,3));
        values.add(new PointValue(3,4));
        values.add(new PointValue(4,5));
        values.add(new PointValue(5,6));

        Line line = new Line(values) ;
//        line.setColor(Color.BLUE);
//        line.setCubic(true);
//        line.setHasLabels(true);
//        line.setHasLines(true);
//        line.setHasPoints(true);
//        line.setFilled(true);

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);
//        data.setAxisXBottom(new Axis().setName("Dates"));
//        data.setAxisYLeft(new Axis().setName("Moisture"));
//        data.setBaseValue(Float.NEGATIVE_INFINITY);

    //    moistureChart.setOnValueTouchListener(new ValueTouchListener());
        moistureChart.setLineChartData(data);

   }

    private void setTemperaturePanel(String temp){
            tempText.setText(temp);
    }
    private void setMoisturePanel(String moisture){
        moistureText.setText(moisture);
    }

    private void setPumpValue(int value){

            if(value==1) {
                pumpLayoutBg.setBackgroundColor(getResources().getColor(R.color.on_pump_color));
                pumpStatusText.setText("ON");
            }
            else {
                pumpLayoutBg.setBackgroundColor(getResources().getColor(R.color.off_pump_color));
                pumpStatusText.setText("OFF");
            }
    }

    private void pumpControl(final boolean statusOn){
        final AlertDialog alert = new AlertDialog.Builder(SystemOverviewActivity.this).create();
        alert.setCancelable(false);
        alert.setTitle("Are you sure?");
       String message = statusOn? getString(R.string.off_pump_txt):getString(R.string.on_pump_txt);
        alert.setMessage(message);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert.dismiss();
               performPumpNetworkCall(statusOn);
            }
        });
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alert.dismiss();
            }
        });
        alert.show();
    }


    private void performPumpNetworkCall(boolean statusOn){
        int value = statusOn ? 0:1;
        String url = ApiUrls.POST_PUMP_SWITCH_URL+getIntent().getIntExtra("systemId",0);
        JSONObject obj = new JSONObject();
        try {
            obj.put("pump_status",value);
            obj.put("api_token", getSharedPreferences(Constants.SHARED_PREFS,MODE_PRIVATE)
                                    .getString(Constants.API_TOKEN,""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean pumpStatus = TextUtils.equals(pumpStatusText.getText(),"ON");
                        String message = TextUtils.equals(pumpStatusText.getText(),"ON")? "OFF":"ON";
                        Toast.makeText(SystemOverviewActivity.this, "Pump will switch "+message+" soon", Toast.LENGTH_SHORT).show();
                        int color = pumpStatus? getResources().getColor(R.color.off_pump_color)
                                                :getResources().getColor(R.color.on_pump_color);
                        pumpLayoutBg.setBackgroundColor(color);
                    }
                });

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            if(error.networkResponse.statusCode == 400)
                                createErrorDialog("Missing key","Authentication failed!. Login again");
                            else if(error.networkResponse.statusCode == 401)
                                createErrorDialog("Unauthorized","Authentication failed!. Login again");
                            else if(error.networkResponse.statusCode == 500)
                                createErrorDialog("Server Error","Something wrong with the servers, Try again");

                        }catch (NullPointerException e){
                            createErrorDialog("Connection Error","Connection Error, Try again");
                        }
                    }
                });

            }
        });

        VolleySingleton.getInstance(SystemOverviewActivity.this).addToRequestQueue(request);
    }


    private void placeIcons(){
        ((ImageView) findViewById(R.id.tmp_ic)).setImageDrawable(
                ContextCompat.getDrawable(SystemOverviewActivity.this,R.drawable.ic_thermometer));

        ((ImageView) findViewById(R.id.moisture_ic)).setImageDrawable(
                ContextCompat.getDrawable(SystemOverviewActivity.this,R.drawable.ic_water_drop));

        ((ImageView) findViewById(R.id.pump_ic)).setImageDrawable(
                ContextCompat.getDrawable(SystemOverviewActivity.this,R.drawable.ic_ecological_generator_tool_of_rotatory_fan));
    }
    private void inflateViews(){
        tempText = (TextView) findViewById(R.id.temp_text);
        moistureText = (TextView)findViewById(R.id.moisture_text);
        moistureChart = (LineChartView) findViewById(R.id.chart_moisture);
        pumpStatusText = (TextView) findViewById(R.id.pump_text);
        pumpLayoutBg = (RelativeLayout) findViewById(R.id.pump_bg);
        mainLayout =(LinearLayout) findViewById(R.id.main_layout);

    }

    private void createErrorDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(SystemOverviewActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "GO BACk", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
            }
        });
        alertDialog.show();

    }

    public void drawSinAbsChart() {
        String decimalPattern = "#.##";
        DecimalFormat decimalFormat = new DecimalFormat(decimalPattern);


        List<PointValue> values = new ArrayList<PointValue>();

        PointValue tempPointValue;
        for (float i = 0; i <= 360.0; i+= 15.0f) {
            tempPointValue = new PointValue(i, Math.abs((float)Math.sin(Math.toRadians(i))));
            tempPointValue.setLabel(decimalFormat
                    .format(Math.abs((float)Math.sin(Math.toRadians(i)))));
            values.add(tempPointValue);
        }

        Line line = new Line(values)
                .setColor(Color.BLUE)
                .setCubic(false)
                .setHasPoints(true).setHasLabels(true);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        List<AxisValue> axisValuesForX = new ArrayList<>();
        List<AxisValue> axisValuesForY = new ArrayList<>();
        AxisValue tempAxisValue;
        for (float i = 0; i <= 360.0f; i += 30.0f){
            tempAxisValue = new AxisValue(i);
            tempAxisValue.setLabel(i+"\u00b0");
            axisValuesForX.add(tempAxisValue);
        }

        for (float i = 0.0f; i <= 1.00f; i += 0.25f){
            tempAxisValue = new AxisValue(i);
            tempAxisValue.setLabel(""+i);
            axisValuesForY.add(tempAxisValue);
        }

        Axis xAxis = new Axis(axisValuesForX);
        Axis yAxis = new Axis(axisValuesForY);
        data.setAxisXBottom(xAxis);
        data.setAxisYLeft(yAxis);


        moistureChart.setLineChartData(data);


    }

    private class ValueTouchListener implements LineChartOnValueSelectListener{

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Toast.makeText(SystemOverviewActivity.this, ""+value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {

        }
    }
}
