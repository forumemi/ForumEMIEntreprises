package com.forum.emi.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class ScannerActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser firebaseUser = null;
    private Button scanButton = null ;
    private FirebaseFunctions firebaseFunctions = null;
    private TextView waitingResult = null;
    private String company =null;

    private View.OnClickListener scanButtonListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Handle click on Scan Button
            // Launch Barcode Scanner
            IntentIntegrator integrator = new IntentIntegrator(ScannerActivity.this);
            integrator.setOrientationLocked(true);
            integrator.setPrompt("Scannez votre QR code");
            integrator.initiateScan();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        firebaseFunctions = FirebaseFunctions.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        scanButton = (Button)findViewById(R.id.scan_button);
        scanButton.setOnClickListener(scanButtonListner);

        Intent intent = getIntent();
        company = intent.getStringExtra("company");
        waitingResult = (TextView)findViewById(R.id.waiting_result);
        waitingResult.setText(company);


    }

    //[BEGIN] Handle result from Barcode Scanner
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String result = scanResult.getContents();

            addMessage(result)
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Exception e = task.getException();
                                if (e instanceof FirebaseFunctionsException) {
                                    FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                    FirebaseFunctionsException.Code code = ffe.getCode();
                                    Object details = ffe.getDetails();
                                }

                                // ...
                            }

                            // ...
                        }
                    });

        }
        // else continue with any other code you need in the method

    }
    //[END] Handle result from Barcode Scanner

    private Task<String> addMessage(final String text) {
        Map<String ,Object> data = new HashMap<>();
        data.put("text",text);
        data.put("push",true);

        return firebaseFunctions
                .getHttpsCallable("register")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }
    @Override
    public void onBackPressed() {
        if (company != null){
            Intent i =new Intent(this,MainActivity.class);
            startActivity(i);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        MenuItem actionRefresh = menu.findItem(R.id.action_refresh);
        MenuItem actionSignIn = menu.findItem(R.id.action_signin);
        MenuItem actionSignOut = menu.findItem(R.id.action_signout);
        if (firebaseUser != null){
            actionSignIn.setVisible(false);
            actionSignOut.setVisible(true);
        } else {
            actionSignIn.setVisible(true);
            actionSignOut.setVisible(false);
        }
        actionRefresh.setVisible(false);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}


