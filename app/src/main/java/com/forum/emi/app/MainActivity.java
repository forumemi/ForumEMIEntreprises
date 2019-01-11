package com.forum.emi.app;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser firebaseUser = null;
    private DatabaseReference databaseRef;
    private WebView homeWebView = null;
    private WebView planWebView = null;
    private WebView programWebView = null;
    private WebView companiesWebView = null;
    private NavigationView navigationView = null;
    private EditText schoolEditText = null;
    private EditText specialityEditText = null;
    private EditText cityEditText = null;
    private EditText promoEditText = null;
    private Button submit = null;
    private Dialog signUpDialog = null ;
    private String token = null;

    public WebViewClient webViewClient = new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        TextView navHeaderTitle = (TextView)navigationView.getHeaderView(0).findViewById(R.id.nav_header_title);
        TextView navHeaderSubtitle = (TextView)navigationView.getHeaderView(0).findViewById(R.id.nav_header_subtitle);
        //if (firebaseUser != null){
          //  navHeaderTitle.setText(firebaseUser.getDisplayName());
          // navHeaderSubtitle.setText(firebaseUser.getEmail());
        //}else{
            navHeaderTitle.setText("");
            navHeaderSubtitle.setText("");
        //}




        if (firebaseUser != null) {
            Toast.makeText(MainActivity.this,firebaseUser.getUid(),Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this,"No user connected",Toast.LENGTH_SHORT).show();
        }

        iniateWebpages();


        databaseRef = FirebaseDatabase.getInstance().getReference();

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (navigationView.getMenu().getItem(3).isChecked() && companiesWebView.canGoBack()){
            companiesWebView.goBack();
        }
        else  {
            super.onBackPressed();
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        MenuItem actionSignIn = menu.findItem(R.id.action_signin);
        MenuItem actionSignOut = menu.findItem(R.id.action_signout);
        if (firebaseUser != null){
            actionSignIn.setVisible(false);
            actionSignOut.setVisible(true);
        } else {
            actionSignIn.setVisible(true);
            actionSignOut.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signin)  {
            createSignInIntent();
        } else if (id == R.id.action_signout)  {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_refresh) {
            companiesWebView.reload();
            homeWebView.reload();
            planWebView.reload();
            programWebView.reload();
        }

        return super.onOptionsItemSelected(item);
    }

    //[BEGIN] Launch Firebase authUI interface
    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }
    //[END] Launch Firebase authUI interface

    //[BEGIN] Firebase Authentication UI results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (response != null) {
                if (response.isNewUser()){
                    completeSignUp();
                }
            }
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                firebaseAuth = FirebaseAuth.getInstance();
                firebaseUser = firebaseAuth.getCurrentUser();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    //[END] Firebase Authentication UI results


    //[BEGIN] Show the form for User to complete signing up
    private void completeSignUp() {
        signUpDialog = new Dialog(this);
        signUpDialog.setContentView(R.layout.signup_profile);
        signUpDialog.setCancelable(true);
        signUpDialog.show();

        schoolEditText = (EditText)signUpDialog.findViewById(R.id.school);
        specialityEditText = (EditText)signUpDialog.findViewById(R.id.speciality);
        cityEditText = (EditText)signUpDialog.findViewById(R.id.city);
        promoEditText = (EditText)signUpDialog.findViewById(R.id.promo);

        submit = (Button)signUpDialog.findViewById(R.id.submit);
        

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getToken();
                databaseRef.child("Users").child(firebaseUser.getUid()).child("school").setValue(schoolEditText.getText().toString());
                databaseRef.child("Users").child(firebaseUser.getUid()).child("speciality").setValue(specialityEditText.getText().toString());
                databaseRef.child("Users").child(firebaseUser.getUid()).child("city").setValue(cityEditText.getText().toString());
                databaseRef.child("Users").child(firebaseUser.getUid()).child("promo").setValue(promoEditText.getText().toString());
                databaseRef.child("Users").child(firebaseUser.getUid()).child("token").setValue(token);
            }

            private void getToken() {
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w("token", "getInstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID token
                                token = task.getResult().getToken();

                                // Log and toast
                                String msg = getString(R.string.msg_token_fmt, token);
                                Log.d("token", msg);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
    //[END] Show the form for User to complete signing up

    //[BEGIN] Handle clicks on Navigation Drawer
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            //set HOME LAYOUT as visible
            ConstraintLayout homeLayout = (ConstraintLayout)findViewById(R.id.home_layout);
            homeLayout.setVisibility(View.VISIBLE);
            ConstraintLayout programLayout = (ConstraintLayout)findViewById(R.id.program_layout);
            programLayout.setVisibility(View.INVISIBLE);
            ConstraintLayout planLayout = (ConstraintLayout)findViewById(R.id.plan_layout);
            planLayout.setVisibility(View.INVISIBLE);
            ConstraintLayout companiesLayout = (ConstraintLayout)findViewById(R.id.companies_layout);
            companiesLayout.setVisibility(View.INVISIBLE);
            //end

        } else if (id == R.id.nav_program) {
            //set PROGRAM LAYOUT as visible
            ConstraintLayout homeLayout = (ConstraintLayout)findViewById(R.id.home_layout);
            homeLayout.setVisibility(View.INVISIBLE);
            ConstraintLayout programLayout = (ConstraintLayout)findViewById(R.id.program_layout);
            programLayout.setVisibility(View.VISIBLE);
            ConstraintLayout planLayout = (ConstraintLayout)findViewById(R.id.plan_layout);
            planLayout.setVisibility(View.INVISIBLE);
            ConstraintLayout companiesLayout = (ConstraintLayout)findViewById(R.id.companies_layout);
            companiesLayout.setVisibility(View.INVISIBLE);
            //end


        } else if (id == R.id.nav_plan) {
            //set PLAN LAYOUT as visible
            ConstraintLayout homeLayout = (ConstraintLayout)findViewById(R.id.home_layout);
            homeLayout.setVisibility(View.INVISIBLE);
            ConstraintLayout programLayout = (ConstraintLayout)findViewById(R.id.program_layout);
            programLayout.setVisibility(View.INVISIBLE);
            ConstraintLayout planLayout = (ConstraintLayout)findViewById(R.id.plan_layout);
            planLayout.setVisibility(View.VISIBLE);
            ConstraintLayout companiesLayout = (ConstraintLayout)findViewById(R.id.companies_layout);
            companiesLayout.setVisibility(View.INVISIBLE);
            //end

        } else if (id == R.id.nav_companies) {
            //set COMPANIES LAYOUT as visible
            ConstraintLayout homeLayout = (ConstraintLayout)findViewById(R.id.home_layout);
            homeLayout.setVisibility(View.INVISIBLE);
            ConstraintLayout programLayout = (ConstraintLayout)findViewById(R.id.program_layout);
            programLayout.setVisibility(View.INVISIBLE);
            ConstraintLayout planLayout = (ConstraintLayout)findViewById(R.id.plan_layout);
            planLayout.setVisibility(View.INVISIBLE);
            ConstraintLayout companiesLayout = (ConstraintLayout)findViewById(R.id.companies_layout);
            companiesLayout.setVisibility(View.VISIBLE);
            //end

        } else if (id == R.id.nav_qrcode) {
            Intent intent = new Intent(MainActivity.this,ScannerActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            completeSignUp();
        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //[END] Handle clicks on Navigation Drawer


    //[BEGIN] Initiates the pages : home,plan,program,companies
    private void iniateWebpages() {
        homeWebView = (WebView)findViewById(R.id.home_webview);
        planWebView = (WebView)findViewById(R.id.plan_webview);
        programWebView = (WebView)findViewById(R.id.program_webview);
        companiesWebView = (WebView)findViewById(R.id.companies_webview);

        WebSettings homeWebSettings = homeWebView.getSettings();
        WebSettings planWebSettings = planWebView.getSettings();
        WebSettings companiesWebSettings = companiesWebView.getSettings();
        WebSettings programWebSettings = programWebView.getSettings();

        homeWebSettings.setJavaScriptEnabled(true);
        planWebSettings.setJavaScriptEnabled(true);
        companiesWebSettings.setJavaScriptEnabled(true);
        programWebSettings.setJavaScriptEnabled(true);

        homeWebView.setWebViewClient(webViewClient);
        planWebView.setWebViewClient(webViewClient);
        programWebView.setWebViewClient(webViewClient);
        companiesWebView.setWebViewClient(webViewClient);

        planWebSettings.setSupportZoom(true);

        homeWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/home.html");
        planWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/plan.html");
        companiesWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/companies.html");
        programWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/program.html");

    }
    //[END] Initiates the pages : home,plan,program,companies
}
