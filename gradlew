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
        startService(intent);
    }

    private void initializeComponents() {

        // TextViews
        mName = findViewById(R.id.tvNameDashboardActivity);
        mCollegeName = findViewById(R.id.tvCollegeNameDashboardActivity);
        mRoom = findViewById(R.id.tvRoomDashboardActivity);
        mStatus = findViewById(R.id.tvStatusDashboardActivity);
        mSupportDevelopers = findViewById(R.id.tvSupportDevelopersDashboardActivity);

        // CardView
        mView = findViewById(R.id.cvRoomDetails);

        // Make those views invisible till the data is fetched from Firebase servers
        mName.setVisibility(View.INVISIBLE);
        mCollegeName.setVisibility(View.INVISIBLE);
        mRoom.setVisibility(View.INVISIBLE);
        mStatus.setVisibility(View.INVISIBLE);
        mSupportDevelopers.setVisibility(View.INVISIBLE);
        mView.setVisibility(View.INVISIBLE);

        // Progress Bars
        mProgressbar = findViewById(R.id.pbLoader);
        mProgressbar.setVisibility(View.VISIBLE);

        // Buttons
        mSaveMeal = findViewById(R.id.btSaveMealDashboardActivity);
        mViewMealStatusOfBoarders = findViewById(R.id.btViewMealStatusDashboardActivity);

        // Set those buttons invisible too
        mSaveMeal.setVisibility(View.INVISIBLE);
        mViewMealStatusOfBoarders.setVisibility(View.INVISIBLE);

        // Switches
        mDaySwitch = findViewById(R.id.switchDayMeal);
        mNightSwitch = findViewById(R.id.switchNightMeal);

        // Set those switches invisible too
        mDaySwitch.setVisibility(View.INVISIBLE);
        mNightSwitch.setVisibility(View.INVISIBLE);

        // Click Listeners
        mSaveMeal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                saveMeal();
            }

        });

        mSupportDevelopers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //supportDevelopers();
                Toast.makeText(DashboardActivity.this, "Clicking on this will take you to Support Developers Activity", Toast.LENGTH_SHORT).show();
            }
        });

        mViewMealStatusOfBoarders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //viewBoarders();
                Toast.makeText(DashboardActivity.this, "This button will take you to an Activity where the list of all the boarders are show", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveMeal() {
        takeDataBeforeSavingMeal();

        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                .setValue(users)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

