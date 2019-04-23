package tkzy.mealy_rc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import tkzy.mealy_rc.R;
import tkzy.mealy_rc.models.GuestMeal;
import tkzy.mealy_rc.models.PreviousMeal;
import tkzy.mealy_rc.models.User;

@SuppressWarnings("FieldCanBeLocal")
public class DashboardActivity extends AppCompatActivity implements ManageGuestsDialog.ManageGuestListener, SetManagerDialog.SetManagerListener {

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
    private boolean previousDayMeal, previousNightMeal;

    // Google
    private InterstitialAd mInterstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Keep the App running in the background to recieve FCM notifications
        startTheService();

        // Check if the Account is set for deletion
        accountDeletionCheck();

        // Initialize the Components of this Activity
        initializeComponents();

        // Load the ad
        mInterstitial = new InterstitialAd(this);
        mInterstitial.setAdUnitId("ca-app-pub-8400601956246937/3135680796");

        mInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitial.loadAd(new AdRequest.Builder().build());
            }

        });

        // Check the meal window and disable those buttons accordingly
        checkMealWindow();

    }

    private void checkMealWindow() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);

        if (!(hour >= 18 && hour <= 22)) {
            mSaveMeal.setEnabled(false);
        }

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
                Intent intent2 = new Intent(DashboardActivity.this, SupportActivity.class);
                startActivity(intent2);
            }
        });

        mViewMealStatusOfBoarders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ListOfBoardersActivity.class);
                startActivity(intent);
            }
        });

    }

    private void saveMeal() {
        takeDataBeforeSavingMeal();

        FirebaseUserMetadata metadata = FirebaseAuth.getInstance().getCurrentUser().getMetadata();

        // Check if current meal is not the same as previous meal.
        if (users.getDayMealON() != previousDayMeal || users.getNightMealON() != previousNightMeal ||
                metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
            savePreviousMeal();
        }

        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                .setValue(users)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        showMealSavedSuccessfullyDialog();

                    }

                })
                .addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {

                        showMealSavingFailedDialog();

                    }

                });
    }

    private void savePreviousMeal() {
        PreviousMeal previousMeal = new PreviousMeal(previousDayMeal, previousNightMeal);

        FirebaseDatabase.getInstance().getReference().child("previous_meal")
                .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                .setValue(previousMeal)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(DashboardActivity.this, "Previous meal saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DashboardActivity.this, "Failed to save previous meal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showMealSavedSuccessfullyDialog() {

        AlertDialog dialog = new AlertDialog.Builder(DashboardActivity.this).create();

        dialog.setTitle(getString(R.string.meal_status));
        dialog.setMessage(getString(R.string.meal_status_change_success));

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mInterstitial.isLoaded()) {
                            mInterstitial.show();
                        }
                    }

                }
        );

        dialog.show();

    }

    private void showMealSavingFailedDialog() {

        AlertDialog dialog = new AlertDialog.Builder(DashboardActivity.this).create();

        dialog.setTitle(getString(R.string.meal_status));
        dialog.setMessage(getString(R.string.meal_status_change_failure));

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(DashboardActivity.this,
                                "Meal Status UNCHANGED!", Toast.LENGTH_LONG).show();

                    }

                }
        );

        dialog.show();

    }

    private void takeDataBeforeSavingMeal() {
        dayMeal = mDaySwitch.isChecked();
        nightMeal = mNightSwitch.isChecked();

        users.setDayMealON(dayMeal);
        users.setNightMealON(nightMeal);
    }

    private void accountDeletionCheck() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("account_deletion_tickets")) {
                        FirebaseDatabase.getInstance().getReference().child("account_deletion_tickets")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                                            if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().equals(singleSnapshot.getValue(String.class))) {
                                                takeAccountDeletionSteps();
                                            } else {
                                                checkUserCredentials();
                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    } else {
                        checkUserCredentials();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {

            finish();

        }
    }

    private void takeAccountDeletionSteps() {
        mProgressbar.setVisibility(View.GONE);
        mDaySwitch.setVisibility(View.GONE);
        mNightSwitch.setVisibility(View.GONE);
        mSaveMeal.setVisibility(View.GONE);
        mViewMealStatusOfBoarders.setVisibility(View.GONE);
        mRoom.setVisibility(View.VISIBLE);
        mRoom.setText(getString(R.string.account_deletion_soon));
        mView.setVisibility(View.VISIBLE);

        /*MenuItem accountSettings, manageGuests;
        accountSettings = menu.findItem(R.id.menuAccountSettings);
        manageGuests = menu.findItem(R.id.menuManageGuests);*/

        /*accountSettings.setVisible(false);
        manageGuests.setVisible(false);*/
    }

    private void checkUserCredentials() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbnode_users))
                    .orderByKey()
                    .equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

            query.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        users = singleSnapshot.getValue(User.class);
                        setData();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }

            });

        }
    }

    private void setData() {
        mName.setText(users.getName());
        mCollegeName.setText(users.getCollegeName());
        mRoom.setText(getString(R.string.room) + " " + users.getRoomNumber()
                + "\t • \t" + getString(R.string.building) + " " + users.getBuilding()
                + "\t • \t" + getString(R.string.bed) + " " + users.getBedNumber());

        Query checkManager = FirebaseDatabase.getInstance().getReference().child("check_manager")
                .orderByKey()
                .equalTo("managerPhoneNumber");

        checkManager.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    String managerPhoneNumber = singleSnapshot.getValue(String.class);

                    if (managerPhoneNumber.equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {

                        mStatus.setText(getString(R.string.manager));
                        mViewMealStatusOfBoarders.setVisibility(View.VISIBLE);

                    } else {

                        mStatus.setText(getString(R.string.boarder));

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query checkGS = FirebaseDatabase.getInstance().getReference().child("check_gs")
                .orderByKey()
                .equalTo("gsPhoneNumber");

        checkGS.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    if (singleSnapshot.getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {

                        mStatus.setText(getString(R.string.gs));

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (users.getDayMealON() != null)
            mDaySwitch.setChecked(users.getDayMealON());

        if (users.getNightMealON() != null)
            mNightSwitch.setChecked(users.getNightMealON());

        mProgressbar.setVisibility(View.GONE);
        mSupportDevelopers.setVisibility(View.VISIBLE);
        mSaveMeal.setVisibility(View.VISIBLE);
        mName.setVisibility(View.VISIBLE);
        mCollegeName.setVisibility(View.VISIBLE);
        mRoom.setVisibility(View.VISIBLE);
        mStatus.setVisibility(View.VISIBLE);
        mSupportDevelopers.setVisibility(View.VISIBLE);
        mView.setVisibility(View.VISIBLE);
        mDaySwitch.setVisibility(View.VISIBLE);
        mNightSwitch.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        this.menu = menu;

        Query query = FirebaseDatabase.getInstance().getReference().child("check_gs")
                .orderByKey()
                .equalTo("gsPhoneNumber");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    if (singleSnapshot.getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {

                        MenuInflater inflater = getMenuInflater();
                        inflater.inflate(R.menu.gs_menu, menu);

                    } else {

                        MenuInflater menuInflater = getMenuInflater();
                        menuInflater.inflate(R.menu.user_menu, menu);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menuAccountSettings:
                Intent intent = new Intent(this, AccountSettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.menuManageGuests:
                manageGuests();
                return true;

            case R.id.menuSetManager:
                setManagerDialog();
                return true;

            case R.id.menuSignOut:
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;

            case R.id.menuSupportDevelopers:
                Intent intent2 = new Intent(this, SupportActivity.class);
                startActivity(intent2);
                return true;

            case R.id.menuDeleteAccount:
                Intent intent1 = new Intent(this, DeleteAccountActivity.class);
                startActivity(intent1);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void manageGuests() {
        ManageGuestsDialog manageGuests = new ManageGuestsDialog();
        manageGuests.show(getSupportFragmentManager(), "Manage Guests");
    }

    private void setManagerDialog() {
        SetManagerDialog setManager = new SetManagerDialog();
        setManager.show(getSupportFragmentManager(), "Set Manager");
    }

    @Override
    public void addGuests(String numberOfGuests, boolean dayMealGuest, boolean nightMealGuest) {

        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                .child("numberOfGuests")
                .setValue(numberOfGuests)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(DashboardActivity.this, "Guests added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DashboardActivity.this, "Try adding Guests again", Toast.LENGTH_SHORT).show();
                    }
                });

        GuestMeal guest = new GuestMeal(dayMealGuest, nightMealGuest);

        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbnode_guest_meal))
                .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                .setValue(guest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(DashboardActivity.this, "Guest meal status modified", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DashboardActivity.this, "Guest meal status change failed", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void setManager(String phoneNumber) {

        FirebaseDatabase.getInstance().getReference()
                .child("check_manager")
                .child("managerPhoneNumber")
                .setValue(phoneNumber)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(DashboardActivity.this, "New Manager", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DashboardActivity.this, "Still the old manager", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
