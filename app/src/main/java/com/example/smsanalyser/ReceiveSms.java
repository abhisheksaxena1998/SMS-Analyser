package com.example.smsanalyser;


import static com.google.android.material.internal.ContextUtils.getActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ReceiveSms extends BroadcastReceiver {
    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive (Context context, Intent intent){
        RequestQueue ExampleRequestQueue = Volley.newRequestQueue(context);
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle=intent.getExtras();
            SmsMessage[] msgs=null;
            String msg_from="";
            String msg_body="";
            if (bundle!=null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs=new SmsMessage[pdus.length];
                    for (int i=0;i< msgs.length;i++){
                        msgs[i]=SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from=msgs[i].getOriginatingAddress();
                        msg_body=msgs[i].getMessageBody();
                    }
                    String finalMsg_from = msg_from;
                    String finalMsg_body = msg_body;

                    String url = "https://mudvfinalradar.eu-gb.cf.appdomain.cloud/api?query=%s";
                    int length = extractUrls(finalMsg_body).size();
                    if (length>0){
                        url = String.format(url, extractUrls(finalMsg_body).get(0));
                    }
                    StringRequest ExampleStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (String.valueOf(jsonObject.get("malware"))=="true"){
                                    Toast.makeText(context, "Digital attack detected ! \n Refresh to view details", Toast.LENGTH_LONG).show();
                                    SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("sent_by", finalMsg_from.toString());
                                    editor.putString("msg_body", finalMsg_body.toString());
                                    editor.putString("received_at",String.valueOf(jsonObject.get("datetime")));
                                    Toast.makeText(context, "From: "+finalMsg_from+"\n"+"Body: "+finalMsg_body+ "\n"+"Status: "+ "Digital attack suspected !", Toast.LENGTH_LONG).show();
                                    editor.commit();
                                }
                                else{
                                    Log.d("Its legitimate", "legitimate");
                                }
                            }catch (JSONException err){
                                Log.d("Error", err.toString());
                            }
                        }
                    }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                    ExampleRequestQueue.add(ExampleStringRequest);
                }catch (Exception e){
                    Toast.makeText(context, "error before req", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
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