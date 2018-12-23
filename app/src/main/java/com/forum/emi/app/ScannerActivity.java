package com.forum.emi.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
public class ScannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        IntentIntegrator integrator = new IntentIntegrator(ScannerActivity.this);
                integrator.setOrientationLocked(true);
                integrator.setPrompt("kljdfskdf");
        integrator.initiateScan();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String re = scanResult.getContents();
            Toast.makeText(ScannerActivity.this,re,Toast.LENGTH_LONG).show();
        }
        // else continue with any other code you need in the method

    }
}


