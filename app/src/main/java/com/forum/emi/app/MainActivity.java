package com.forum.emi.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,
                    AdapterView.OnItemSelectedListener {

    private static final int RC_SIGN_IN = 123;
    Context context = this;
    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser firebaseUser = null;
    private DatabaseReference databaseRef = null;
    private WebView homeWebView = null;
    private WebView planWebView = null;
    private WebView programWebView = null;
    private WebView companiesWebView = null;
    private WebView sponsorsWebView = null;
    private NavigationView navigationView = null;
    private Button submit = null;
    private Dialog dialog = null;
    private String token = null;
    private Spinner schoolSpinner = null;
    private Spinner specialitySpinner = null;
    private Spinner promoSpinner = null;
    private FirebaseFirestore firebaseFirestore = null ;

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
        if (firebaseUser != null){
            navHeaderTitle.setText(firebaseUser.getDisplayName());
            navHeaderSubtitle.setText(firebaseUser.getEmail());
        }else{
            navHeaderTitle.setText("");
            navHeaderSubtitle.setText("");
        }
        navHeaderSubtitle.setText("");



        // TODO : remove
        if (firebaseUser != null) {
            Toast.makeText(MainActivity.this,firebaseUser.getUid(),Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this,"No user connected",Toast.LENGTH_SHORT).show();
        }

        initiateWebPages();
        if (isInternetAvailable()){
            companiesWebView.reload();
            homeWebView.reload();
            planWebView.reload();
            programWebView.reload();
            sponsorsWebView.reload();
        }

        databaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        if (Objects.equals(intent.getAction(), "newUser")){
            completeSignUp();
        }

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

    @Override
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
            Intent intent = new Intent(context,WelcomeActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.action_refresh) {
            if (isInternetAvailable()){
                companiesWebView.reload();
                homeWebView.reload();
                planWebView.reload();
                programWebView.reload();
                sponsorsWebView.reload();
            }
        } else if (id == R.id.action_help){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            WebView helpWebView = new WebView(this);
            helpWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/help.html");
            helpWebView.setWebViewClient(webViewClient);
            builder.setView(helpWebView);
            builder.setTitle("Aide");
            builder.setIcon(R.drawable.ic_help_black_24dp);
            builder.show();
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
                getToken();

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
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.signup_profile);
        dialog.setCancelable(false);
        dialog.show();

        schoolSpinner =(Spinner) dialog.findViewById(R.id.school_spinner);
        ArrayAdapter<CharSequence> adapterSchoolSpinner = ArrayAdapter.createFromResource(context,
                R.array.schools_array, android.R.layout.simple_spinner_item);
        adapterSchoolSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        schoolSpinner.setAdapter(adapterSchoolSpinner);
        schoolSpinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) context);

        specialitySpinner = (Spinner) dialog.findViewById(R.id.specility_spinner);

        promoSpinner =(Spinner) dialog.findViewById(R.id.promo_spinner);
        ArrayAdapter<CharSequence> adapterPromoSpinner = ArrayAdapter.createFromResource(context,
                R.array.promo_array, android.R.layout.simple_spinner_item);
        adapterPromoSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        promoSpinner.setAdapter(adapterPromoSpinner);




        submit = (Button) dialog.findViewById(R.id.submit);
        getToken();
        Map<String,List<String>> map = new HashMap<>();
        List<String> stringList = Collections.emptyList();
        map.put("complete",stringList);
        map.put("pending",stringList);

        firebaseFirestore.collection("users_registrations").document(firebaseUser.getUid()).set(map);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String school = schoolSpinner.getSelectedItem().toString();
                final String speciality = specialitySpinner.getSelectedItem().toString();
                final String promo = promoSpinner.getSelectedItem().toString();
                final String email = firebaseUser.getEmail();
                final String name = firebaseUser.getDisplayName();

                databaseRef.child("Users").child(firebaseUser.getUid()).child("school").setValue(school);
                databaseRef.child("Users").child(firebaseUser.getUid()).child("speciality").setValue(speciality);
                databaseRef.child("Users").child(firebaseUser.getUid()).child("promo").setValue(promo);
                databaseRef.child("Users").child(firebaseUser.getUid()).child("email").setValue(email);
                databaseRef.child("Users").child(firebaseUser.getUid()).child("name").setValue(name);
                databaseRef.child("Users").child(firebaseUser.getUid()).child("token").setValue(token);
                dialog.cancel();
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
            intent.putExtra("task","default");
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            //TODO : remove this completeSignUp();
            //completeSignUp();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("A propos");
            builder.setIcon(R.drawable.ic_info_black_24dp);
            builder.setMessage("message");
            builder.show();
        } else if (id == R.id.nav_contact) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            WebView helpWebView = new WebView(this);
            helpWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/contact.html");
            helpWebView.setWebViewClient(webViewClient);
            builder.setView(helpWebView);
            builder.setTitle("Contactez-nous");
            builder.setIcon(R.drawable.ic_help_black_24dp);
            builder.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //[END] Handle clicks on Navigation Drawer


    //[BEGIN] Initiates the pages : home,plan,program,companies
    @SuppressLint("SetJavaScriptEnabled")
    private void initiateWebPages() {

        homeWebView = (WebView)findViewById(R.id.home_webview);
        planWebView = (WebView)findViewById(R.id.plan_webview);
        programWebView = (WebView)findViewById(R.id.program_webview);
        companiesWebView = (WebView)findViewById(R.id.companies_webview);
        sponsorsWebView = (WebView)findViewById(R.id.sponsors_webview);

        WebSettings homeWebSettings = homeWebView.getSettings();
        WebSettings planWebSettings = planWebView.getSettings();
        WebSettings companiesWebSettings = companiesWebView.getSettings();
        WebSettings programWebSettings = programWebView.getSettings();
        WebSettings sponsorsWebSettings = sponsorsWebView.getSettings();

        homeWebSettings.setJavaScriptEnabled(true);
        planWebSettings.setJavaScriptEnabled(true);
        companiesWebSettings.setJavaScriptEnabled(true);
        programWebSettings.setJavaScriptEnabled(true);
        sponsorsWebSettings.setJavaScriptEnabled(true);

        homeWebView.setWebViewClient(webViewClient);
        planWebView.setWebViewClient(webViewClient);
        programWebView.setWebViewClient(webViewClient);
        companiesWebView.setWebViewClient(webViewClient);
        sponsorsWebView.setWebViewClient(webViewClient);

        homeWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/home.html");
        planWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/plan.html");
        companiesWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/companies.html");
        programWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/program.html");
        sponsorsWebView.loadUrl("https://forum-emi-entreprises.firebaseapp.com/sponsors.html");

    }
    //[END] Initiates the pages : home,plan,program,companies


    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0){
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                    R.array.emi_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 1) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                    R.array.ehtp_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 2) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                    R.array.ensmr_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 3) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                    R.array.ecc_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 4) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.aiac_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 5) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.ensem_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 6) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.ensias_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 7) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.esgb_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 8) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.esi_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 9) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.esith_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 10) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.iav_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 11) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.inpt_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 12) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.insea_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 13) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.ensa_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 14) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.ensam_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 15) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.enset_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        } else if (position == 16) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.fst_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            specialitySpinner.setAdapter(adapter);
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
                        databaseRef.child("Users").child(firebaseUser.getUid()).child("token").setValue(token);
                    }
                });
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
