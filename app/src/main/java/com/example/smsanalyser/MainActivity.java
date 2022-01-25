package com.example.smsanalyser;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.tapadoo.alerter.Alerter;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    CoordinatorLayout layout;
    public static final String MyPREFERENCES = "myPrefs" ;
    SharedPreferences sharedpreferences;
    private TextView sentBy, messageBody, parsedUrl, receivedAt;
    private Button syncData, redirectToSandbox, redirectToDetails;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestQueue ExampleRequestQueue = Volley.newRequestQueue(this);
        SharedPreferences sharedPreferences = this.getSharedPreferences("myPrefs",Context.MODE_PRIVATE);

        sentBy = findViewById(R.id.sentBy);
        messageBody = findViewById(R.id.messageBody);
        parsedUrl = findViewById(R.id.parsedUrl);
        receivedAt = findViewById(R.id.receivedAt);
        sentBy.setText(sharedPreferences.getString("sent_by", ""));
        messageBody.setText(sharedPreferences.getString("msg_body", ""));
        parsedUrl.setText(String.join(", ",extractUrls(sharedPreferences.getString("msg_body", ""))));
        receivedAt.setText(sharedPreferences.getString("received_at",""));
        syncData = findViewById(R.id.syncData);
        layout = findViewById(R.id.layout);
        redirectToSandbox = findViewById(R.id.redirectToSandbox);
        redirectToDetails = findViewById(R.id.redirectToDetails);
        Alerter.create(MainActivity.this).setTitle("Alert !")
                .setText("You have been subjected to digital attacks")
                .setDuration(3000)
                .setBackgroundColorRes(R.color.warning)
                .show();
        Snackbar snackbar
                = Snackbar
                .make(
                        layout,
                        "Report this attack",
                        Snackbar.LENGTH_INDEFINITE)
                .setAction(
                        "Report",
                        new View.OnClickListener() {
                            @SuppressLint("LongLogTag")
                            @Override
                            public void onClick(View view)
                            {
                                String url = "https://mudvfinalradar.eu-gb.cf.appdomain.cloud/result?url=%s";
                                int length = extractUrls(sharedPreferences.getString("msg_body", "")).size();
                                if (length>0){
                                    url = String.format(url, extractUrls(sharedPreferences.getString("msg_body", "")).get(0));
                                    StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject jsonObject = new JSONObject(response);
                                            }catch (JSONException err){
                                                Log.d("Error", err.toString());
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        //Create an error listener to handle errors appropriately.
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                        }
                                    });
                                    ExampleRequestQueue.add(ExampleStringRequest);
                                }
                                Toast.makeText(MainActivity.this,"Attack has been reported",Toast.LENGTH_SHORT).show();
                            }
                        });
        snackbar.show();
        syncData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sentBy.setText(sharedPreferences.getString("sent_by", ""));
                messageBody.setText(sharedPreferences.getString("msg_body", ""));
                parsedUrl.setText(String.join(", ",extractUrls(sharedPreferences.getString("msg_body", ""))));
                receivedAt.setText(sharedPreferences.getString("received_at",""));
                Toast.makeText(getApplicationContext(),"latest SMS details updated", Toast.LENGTH_LONG);
                snackbar.show();
            }
        });

        redirectToDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String resultUrl = "https://mudvfinalradar.eu-gb.cf.appdomain.cloud/result?url=%s";
                List<String> parsedTargetUrl = extractUrls(sharedPreferences.getString("msg_body", ""));
                if (!parsedTargetUrl.isEmpty()){
                    if (parsedTargetUrl.get(0) != "") {
                        resultUrl = String.format(resultUrl, parsedTargetUrl.get(0));
                        Intent viewIntent =
                                new Intent("android.intent.action.VIEW",
                                        Uri.parse(resultUrl));
                        startActivity(viewIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Please click on refresh button to view details of most recent threat", Toast.LENGTH_LONG);
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Please click on refresh button to view details of most recent threat", Toast.LENGTH_LONG);
                }
            }
        });

        redirectToSandbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String resultUrl = "https://mudvfinalradar.eu-gb.cf.appdomain.cloud/sandboxresult?url=%s";
                List<String> parsedTargetUrl = extractUrls(sharedPreferences.getString("msg_body", ""));
                if (!parsedTargetUrl.isEmpty()){
                    if (parsedTargetUrl.get(0) != "") {
                        resultUrl = String.format(resultUrl, parsedTargetUrl.get(0));
                        Intent viewIntent =
                                new Intent("android.intent.action.VIEW",
                                        Uri.parse(resultUrl));
                        startActivity(viewIntent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Please click on refresh button to view details of most recent threat", Toast.LENGTH_LONG);
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Please click on refresh button to view details of most recent threat", Toast.LENGTH_LONG);
                }
            }
        });
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},1000);
        }
    }
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode==1000){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    public void openDialog() {
        ExampleDialog exampleDialog = new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(),"example dialog");
    }

    public static List<String> extractUrls(String input)
    {
        List<String> result = new ArrayList<String>();

        String[] words = input.split("\\s+");

        Pattern pattern = Patterns.WEB_URL;
        for(String word : words)
        {
            if(pattern.matcher(word).find())
            {
                if(!word.toLowerCase().contains("http://") && !word.toLowerCase().contains("https://"))
                {
                    word = "http://" + word;
                }
                result.add(word);
            }
        }
        return result;
    }
}