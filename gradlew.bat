package tkzy.mealy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import tkzy.mealy.models.User;

@SuppressWarnings("FieldCanBeLocal")
public class DashboardActivity extends AppCompatActivity {

    // Constants
    public static final String timeFormat = "HH:mm";

    // Data model
    private User users;

    // Widgets
    private TextView mName, mCollegeName, mRoom, mStatus, mSupportDevelopers;
    private CardView mView;
    private ProgressBar mProgressbar;
    private Button mSaveMeal, mViewMealStatusOfBoarders;
    private Switch mDaySwitch, mNightSwitch;
    private Menu menu;

    // Variables
    private boolean dayMeal, nightMeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Keep the App running in the background to recieve FCM notifications
        startTheService();

        // Initialize the Components of this Activity
        initializeComponents();

        // Check if the Account is set for deletion
        accountDeletionCheck();
    }

    private void startTheService() {
        Intent intent = new Intent(this, KeepInBackground.class);
      