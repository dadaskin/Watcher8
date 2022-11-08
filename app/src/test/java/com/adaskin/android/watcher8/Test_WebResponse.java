package com.adaskin.android.watcher8;

import org.junit.Test;

import static org.junit.Assert.*;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


public class Test_WebResponse {
    @Test
    public void Test_Send_Receive_Web() {
        String url = "https://schema.org";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                handleWebResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleWebError(error);
            }
        });
    }

    private void handleWebResponse(String response){
        assertNotNull(response);
    }

    private void handleWebError(VolleyError error){
        assertNotNull(error);
    }
}
