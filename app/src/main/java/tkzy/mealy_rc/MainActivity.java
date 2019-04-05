package tkzy.mealy_rc;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends AppCompatActivity {

    // Flag
    private static final int RC_SIGN_IN = 1;

    // Widgets
    private Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Components
        initializeComponents();

        //Button Click Listeners
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAuthFlow();
            }
        });
    }

    private void initializeComponents() {
        mLogin = findViewById(R.id.btLoginMainActivity);
    }

    private void startAuthFlow() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {

            redirectDashboardActivity();

        }
        else {

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(
                                    Collections.singletonList(
                                            new AuthUI.IdpConfig.PhoneBuilder().build()
                                    )
                            )
                            .setTheme(R.style.AppTheme)
                            .build(),
                    RC_SIGN_IN
            );

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {

            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {

                FirebaseUserMetadata metadata = FirebaseAuth.getInstance().getCurrentUser().getMetadata();

                // First time Log-In
                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {

                    String key = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.dbnode_user_ids))
                            .push().getKey();

                    FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.dbnode_user_ids))
                            .child(key)
                            .setValue(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    redirectAccountSettingsActivity();
                                }

                            })
                            .addOnFailureListener(new OnFailureListener() {

                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(MainActivity.this, "Failed to Login. Contact tkzydevelopers@gmail.com", Toast.LENGTH_LONG).show();

                                    finish();

                                }

                            });
                }

                // Subsequent Log-In
                else {
                    redirectDashboardActivity();
                }

            }

            else {

                // Sign in failed
                if (response == null) {

                    // User pressed back button
                    finish();

                }

            }

        }
    }

    private void redirectDashboardActivity() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }

    private void redirectAccountSettingsActivity() {
        Intent intent = new Intent(this, AccountSettingsActivity.class);
        startActivity(intent);
    }

}
