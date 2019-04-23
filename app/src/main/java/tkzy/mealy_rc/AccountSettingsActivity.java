package tkzy.mealy_rc;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import tkzy.mealy_rc.models.User;

@SuppressWarnings("FieldCanBeLocal")
public class AccountSettingsActivity extends AppCompatActivity {

    // Constants
    private static final int PICK_IMAGE_REQUEST = 1;

    // Widgets
    private de.hdodenhof.circleimageview.CircleImageView mProfilePicture;
    private ProgressBar mProgressBar;
    private Button mChoose, mUpload, mSave;
    private EditText mName, mRoom, mCollegeName, mPhoneNumber;
    private Spinner mBed, mBuilding;

    // Get Image Uri
    private Uri mImageUri;

    /* --- Firebase --- */
    // Cloud Storage path is "/users/profile_pictures/"
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        initialize();
        getUserProfilePicture();
    }

    private void getUserProfilePicture() {
        StorageReference profilePicture = FirebaseStorage.getInstance().getReference()
                .child("users/profile_pictures/" + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() + ".jpg");

        profilePicture.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(AccountSettingsActivity.this).load(uri).into(mProfilePicture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AccountSettingsActivity.this,
                        "No profile picture uploaded yet!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initialize() {

        // Assign all the respective widgets
        mProfilePicture = findViewById(R.id.civDP);
        mProgressBar = findViewById(R.id.progressBar);
        mChoose = findViewById(R.id.btChoosePicture);
        mUpload = findViewById(R.id.btUploadPicture);
        mSave = findViewById(R.id.btSaveAccountDetails);
        mName = findViewById(R.id.etName);
        mRoom = findViewById(R.id.etRoom);
        mPhoneNumber = findViewById(R.id.etPhoneNumber);
        mBed = findViewById(R.id.spBeds);
        mBuilding = findViewById(R.id.spBuildings);
        mCollegeName = findViewById(R.id.etCollegeName);

        Glide.with(this).load(R.mipmap.ic_person_round).into(mProfilePicture);

        // Initialize the Firebase Storage Reference
        storageReference = FirebaseStorage.getInstance().getReference().child("users").child("profile_pictures");

        // Disable the "Upload" Button first, because we want the user to select an image first
        mUpload.setEnabled(false);

        // Click listener for the "Save" button
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog dialog = new AlertDialog.Builder(AccountSettingsActivity.this).create();
                dialog.setTitle(getString(R.string.warning));
                dialog.setMessage(getString(R.string.warning_message));
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "I Accept!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                insertData();
                            }
                        }
                );
                dialog.show();

            }
        });

        // Click listener for the "Choose" button
        mChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileSelector();
            }
        });

        // Click listener for the "Upload" button
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadProfilePicture();
            }
        });

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadProfilePicture() {

        if (mImageUri != null) {
            mUpload.setText(getString(R.string.done));
            mUpload.setEnabled(false);
            final StorageReference fileReference = storageReference
                    .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()
                            + "." + getFileExtension(mImageUri));

            UploadTask uploadTask = fileReference.putFile(mImageUri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgressBar.setProgress(100);

                    FirebaseDatabase.getInstance().getReference().child("profile_pictures")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                            .setValue(fileReference.getDownloadUrl().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(AccountSettingsActivity.this, "Profile Picture changed successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AccountSettingsActivity.this, "Failed to save, contact TKZY Support", Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AccountSettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mProgressBar.setProgress((int) progress);
                }
            });
        } else {
            Toast.makeText(this, "Please select an Image.", Toast.LENGTH_SHORT).show();
        }

    }

    private void openFileSelector() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            mChoose.setText(getString(R.string.done));
            mChoose.setEnabled(false);
            mUpload.setEnabled(true);
            Glide.with(this).load(mImageUri).into(mProfilePicture);
        } else {
            Toast.makeText(this, "Failed to pick an image.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // A simple check to know if the user has logged in for the first time or not
        FirebaseUserMetadata metadata = FirebaseAuth.getInstance().getCurrentUser().getMetadata();

        if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
            // This method is called which will display a dialog box stating that his profile is incomplete
            showProfileIncompleteDialog();
        } else {
            getUserAccountDetails();
        }

    }

    // This method will display a dialog which will serve a small heads up for the user just to let him know to fill the details for his profile
    private void showProfileIncompleteDialog() {

        AlertDialog dialog = new AlertDialog.Builder(AccountSettingsActivity.this)
                .create();
        dialog.setTitle("Profile Incomplete");
        dialog.setMessage(getString(R.string.incomplete));
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPhoneNumber.setText(FirebaseAuth.getInstance().getCurrentUser()
                                .getPhoneNumber());
                    }
                }
        );
        dialog.show();

    }

    // This method will fetch the user profile data from Firebase Realtime Database
    private void getUserAccountDetails() {

        // This is a reference to the root directory of the DB
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        // This query will the data from the "users" node based upon the users Phone Number
        Query query = reference.child(getString(R.string.dbnode_users))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        // Query returns the value by attaching a listener
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    User user = singleSnapshot.getValue(User.class);

                    mName.setText(user.getName());
                    switch (user.getBedNumber()) {

                        case "A":
                            mBed.setSelection(0);
                            break;

                        case "B":
                            mBed.setSelection(1);
                            break;

                        case "C":
                            mBed.setSelection(2);
                            break;

                        case "D":
                            mBed.setSelection(3);
                            break;

                    }
                    switch (user.getBuilding()) {

                        case "New":
                            mBuilding.setSelection(0);
                            break;

                        case "Old":
                            mBuilding.setSelection(1);
                            break;

                    }
                    mRoom.setText(user.getRoomNumber());
                    mCollegeName.setText(user.getCollegeName());
                    mPhoneNumber.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });

    }

    // This method will insert(save) the user profile data to the Firebase Realtime Database
    private void insertData() {

        // Check if the Name of the or the Room number is empty or not
        if (!mName.getText().toString().equals("")
                && !mRoom.getText().toString().equals("")) {

            User user = new User(
                    mBed.getSelectedItem().toString(),
                    mBuilding.getSelectedItem().toString(),
                    mCollegeName.getText().toString(),
                    false,
                    false,
                    mName.getText().toString(),
                    FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),
                    mRoom.getText().toString(),
                    "0"
            );
            FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbnode_users))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                    .setValue(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            redirectDashboardScreen();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AccountSettingsActivity.this,
                                    "Cannot save your data to the Database. Contact us via the Help & Support section", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
        }

    }

    // This method will redirect the user to the Dashboard Screen
    private void redirectDashboardScreen() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);

        finish();
    }

}
