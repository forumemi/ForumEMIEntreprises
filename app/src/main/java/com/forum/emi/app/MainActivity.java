package com.forum.emi.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

    private static final int RC_SIGN_IN = 123;
    private SharedPreferences sharedPreferences = null;
    MenuItem actionSignin = null;
    MenuItem actionSignout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem actionSignin = menu.findItem(R.id.action_signin);
        MenuItem actionSignout = menu.findItem(R.id.action_signout);
        String userid = sharedPreferences.getString("USER_ID","");
        if (userid == "") {
            actionSignin.setVisible(true);
            actionSignout.setVisible(false);
        } else {
            actionSignin.setVisible(false);
            actionSignout.setVisible(true);
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
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_signin)  {
            createSignInIntent();
        } else if (id == R.id.action_signout)  {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences.Editor editor =sharedPreferences.edit();
            editor.putString("USER_ID","");
            editor.commit();
            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

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

    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("USER_ID",user.getUid());
                editor.commit();
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    // [END auth_fui_result]


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

            String userId = sharedPreferences.getString("USER_ID","");
            Toast.makeText(MainActivity.this,userId,Toast.LENGTH_SHORT).show();

        } else if (id == R.id.nav_qrcode) {
            Intent intent = new Intent(MainActivity.this,ScannerActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    static final int BUFF_SIZE = 2048;
    static final String DEFAULT_ENCODING = "utf-8";

    public static String readFileToString(String filePath, String encoding) throws IOException {

        if (encoding == null || encoding.length() == 0)
            encoding = DEFAULT_ENCODING;

        StringBuffer content = new StringBuffer();

        FileInputStream fis = new FileInputStream(new File(filePath));
        byte[] buffer = new byte[BUFF_SIZE];

        int bytesRead = 0;
        while ((bytesRead = fis.read(buffer)) != -1)
            content.append(new String(buffer, 0, bytesRead, encoding));

        fis.close();
        return content.toString();
    }
}
