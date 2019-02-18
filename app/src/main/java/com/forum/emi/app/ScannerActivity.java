package com.forum.emi.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScannerActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser firebaseUser = null;
    private Button scanButton = null ;
    private FirebaseFunctions firebaseFunctions = null;
    private TextView registredTo ;
    private TextView processComplete ;
    private Set<String> registredToSet = null;
    private Set<String> processCompleteSet = null;
    private String textString;
    private String textString1;
    private FirebaseFirestore firebaseFirestore;

    private RecyclerView recyclerViewComplete;
    private RecyclerView.Adapter adapterComplete;
    private RecyclerView.LayoutManager layoutManagerComplete;

    private RecyclerView recyclerViewPending;
    private RecyclerView.Adapter adapterPending;
    private RecyclerView.LayoutManager layoutManagerPending;
    private Vibrator myVib;


    private View.OnClickListener scanButtonListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (firebaseUser == null) {
                Toast.makeText(ScannerActivity.this,"Veuillez-vous connecter !",Toast.LENGTH_SHORT).show();
            }else {
                // Handle click on Scan Button
                // Launch Barcode Scanner
                IntentIntegrator integrator = new IntentIntegrator(ScannerActivity.this);
                integrator.setOrientationLocked(true);
                integrator.setPrompt("Scannez votre QR code");
                integrator.setBeepEnabled(true);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.initiateScan();
            }
        }
    };
    public WebViewClient webViewClient = new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        recyclerViewComplete = (RecyclerView)findViewById(R.id.recycle_view_Complete);
        layoutManagerComplete = new LinearLayoutManager(this);
        recyclerViewComplete.setLayoutManager(layoutManagerComplete);

        recyclerViewPending = (RecyclerView)findViewById(R.id.recycle_view_pending);
        layoutManagerPending = new LinearLayoutManager(this);
        recyclerViewPending.setLayoutManager(layoutManagerPending);

        firebaseFirestore = FirebaseFirestore.getInstance();

        // Initiate Firebase Components
        firebaseFunctions = FirebaseFunctions.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        // Scanner Button
        scanButton = (Button)findViewById(R.id.scan_button);
        scanButton.setOnClickListener(scanButtonListner);

        final DocumentReference completeDocRef = firebaseFirestore.collection("users_registrations").document(firebaseAuth.getUid());
        completeDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("TAG", "Current data: " + snapshot.getData().get("complete"));

                    Object dataCompelte = snapshot.getData().get("complete");
                    Object dataPenging = snapshot.getData().get("pending");

                    adapterComplete = new MyAdapter((ArrayList<String>)dataCompelte);
                    recyclerViewComplete.setAdapter(adapterComplete);

                    adapterPending = new MyAdapter((ArrayList<String>)dataPenging);
                    recyclerViewPending.setAdapter(adapterPending);
                } else {
                    Log.d("TAG", "Current data: null");
                }
            }
        });
    }

    //[BEGIN] Handle result from Barcode Scanner
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String result = scanResult.getContents();
            if (result == null){
                result = "";
            }
            if (result.length()>Crypto.key.length()){
                if (result.substring(0,(Crypto.key).length()).equals(Crypto.key)){
                    result = Crypto.decrypt(result);
                    registerFunction(result)
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
            }

        }
        // else continue with any other code you need in the method

    }
    //[END] Handle result from Barcode Scanner

    private Task<String> registerFunction(final String text)  {
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
                        myVib.vibrate(50);
                        String result = (String) task.getResult().getData();
                        return result;
                    }

                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scanner_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            WebView helpWebView = new WebView(this);
            helpWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/help.html");
            helpWebView.setWebViewClient(webViewClient);
            builder.setView(helpWebView);
            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }
}


