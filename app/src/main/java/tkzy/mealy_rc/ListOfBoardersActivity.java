package tkzy.mealy_rc;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tkzy.mealy_rc.R;
import tkzy.mealy_rc.models.BoarderAdapter;
import tkzy.mealy_rc.models.User;

@SuppressWarnings("FieldCanBeLocal")
public class ListOfBoardersActivity extends AppCompatActivity {

    // Widgets
    private RecyclerView mRecyclerView;
    private BoarderAdapter adapter;
    private ProgressBar mProgressBar;

    // Model Variable
    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> room = new ArrayList<>();
    private ArrayList<String> bed = new ArrayList<>();
    private ArrayList<String> building = new ArrayList<>();
    private ArrayList<Boolean> day = new ArrayList<>();
    private ArrayList<Boolean> night = new ArrayList<>();
    private ArrayList<String> guests = new ArrayList<>();
    private ArrayList<String> phone = new ArrayList<>();

    // Variables
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_boarders);

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        getDataFromFirebase();
    }

    private void getDataFromFirebase() {

        FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_user_ids))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                            phoneNumber = singleSnapshot.getValue(String.class);

                            Query queryGetUserDetails = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.dbnode_users))
                                    .orderByKey()
                                    .equalTo(phoneNumber);

                            queryGetUserDetails.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                                        User boarder = singleSnapshot.getValue(User.class);

                                        name.add(boarder.getName());
                                        room.add(boarder.getRoomNumber());
                                        bed.add(boarder.getBedNumber());
                                        building.add(boarder.getBuilding());
                                        day.add(boarder.getDayMealON());
                                        night.add(boarder.getNightMealON());
                                        guests.add(boarder.getNumberOfGuests());
                                        phone.add(boarder.getPhoneNumber());

                                        initialize();

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void initialize() {

        mRecyclerView = findViewById(R.id.rvList);
        adapter = new BoarderAdapter(this, name, room, bed, building, day, night, guests, phone);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressBar.setVisibility(View.GONE);

    }

}
