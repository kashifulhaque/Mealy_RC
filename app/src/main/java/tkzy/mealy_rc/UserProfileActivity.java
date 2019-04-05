package tkzy.mealy_rc;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import tkzy.mealy_rc.R;
import tkzy.mealy_rc.models.User;

@SuppressWarnings("FieldCanBeLocal")
public class UserProfileActivity extends AppCompatActivity {

    // Widgets
    private TextView mName, mCollege, mBed, mBuilding, mRoom, mGuests;
    private ImageView mDay, mNight;
    private de.hdodenhof.circleimageview.CircleImageView mProfilePicture;

    // Data Model
    private User user;

    // Variables
    private String userPhoneNumber, profilePictureLink;

    // Google
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getUserProfilePicture();
        getDataFromIntent();
        initializeWidgets();
        getUserData();
        loadAd();
    }

    private void loadAd() {
        MobileAds.initialize(this, getString(R.string.admob_app_id));

        adView = findViewById(R.id.adViewUserProfile);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void getUserProfilePicture() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child("profile_pictures");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        profilePictureLink = singleSnapshot.getValue(String.class);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getUserData() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbnode_users))
                    .orderByKey()
                    .equalTo(userPhoneNumber);

            query.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        user = singleSnapshot.getValue(User.class);
                        setData();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }

            });

            StorageReference profilePicture = FirebaseStorage.getInstance().getReference()
                    .child("users/profile_pictures/" + userPhoneNumber + ".jpg");

            profilePicture.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(UserProfileActivity.this).load(uri).into(mProfilePicture);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserProfileActivity.this, "No profile picture uploaded yet!", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    private void setData() {
        mName.setText(user.getName());
        mCollege.setText(user.getCollegeName());
        mBed.setText(user.getBedNumber());
        mBuilding.setText(user.getBuilding());
        mRoom.setText(user.getRoomNumber());
        mGuests.setText("2");

        if (user.getDayMealON()) {
            Glide.with(this).load(R.drawable.ic_check).into(mDay);
        } else {
            Glide.with(this).load(R.drawable.ic_wrong).into(mDay);
        }

        if (user.getNightMealON()) {
            Glide.with(this).load(R.drawable.ic_check).into(mNight);
        } else {
            Glide.with(this).load(R.drawable.ic_wrong).into(mNight);
        }
    }

    private void initializeWidgets() {
        mName = findViewById(R.id.tvBoarderName);
        mCollege = findViewById(R.id.tvBoarderCollege);
        mBed = findViewById(R.id.tvBoarderBed);
        mBuilding = findViewById(R.id.tvBoarderBuilding);
        mRoom = findViewById(R.id.tvBoarderRoom);
        mGuests = findViewById(R.id.tvBoarderGuests);
        mDay = findViewById(R.id.ivBoarderDayMeal);
        mNight = findViewById(R.id.ivBoarderNightMeal);
        mProfilePicture = findViewById(R.id.civProfilePicture);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        userPhoneNumber = intent.getExtras().getString("userPhoneNumber");
    }

}
