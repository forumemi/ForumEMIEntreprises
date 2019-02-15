package com.forum.emi.app;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private Context context = this;
    private Button welcomeButton = null;
    private FirebaseAuth firebaseAuth = null;
    private FirebaseUser firebaseUser = null;
    private String userName ;
    private FirebaseFirestore firebaseFirestore = null;
    private DatabaseReference databaseRef = null;
    private String token = null;
    private TextView welcomeText = null;


    private View.OnClickListener welcomeButtonListenerOld = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    };
    private View.OnClickListener welcomeButtonListenerNew = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            createSignInIntent();
        }
    };
    private View.OnClickListener textlistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FirebaseAuth.getInstance().signOut();
            recreate();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        welcomeButton = (Button)findViewById(R.id.welcome_Button);
        welcomeText = (TextView)findViewById(R.id.welcome_text);

        if (firebaseUser != null){
            welcomeText.setText("Bienvenue "+firebaseUser.getDisplayName()+" !\nCe n'est pas vous? Cliquez ici!");
            welcomeText.setOnClickListener(textlistener);
            welcomeButton.setText("Poursuivre");
            welcomeButton.setOnClickListener(welcomeButtonListenerOld);

        } else  {
            welcomeText.setText("Connectez-vous pour poursuivre");
            welcomeButton.setText("Se connecter");
            welcomeButton.setOnClickListener(welcomeButtonListenerNew);

        }
        databaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
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

    //[BEGIN] Firebase Authentication UI results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Intent intent = new Intent(context,MainActivity.class);
            if (response != null) {
                if (response.isNewUser()){
                    intent.setAction("newUser");
                }
            }
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                firebaseAuth = FirebaseAuth.getInstance();
                firebaseUser = firebaseAuth.getCurrentUser();
                getToken();
                startActivity(intent);
                finish();

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    //[END] Firebase Authentication UI results

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
}
