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
import tkzy.mealy_rc.models.PreviousMeal;
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
    private ArrayList<String> guests = new ArrayList<>();
    private ArrayList<String> phone = new ArrayList<>();
    private ArrayList<String> day = new ArrayList<>();
    private ArrayList<String> night = new ArrayList<>();
    private ArrayList<Boolean> previousDay = new ArrayList<>();
    private ArrayList<Boolean> previousNight = new ArrayList<>();
    private ArrayList<Boolean> isMealChanged = new ArrayList<>();

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
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                            phoneNumber = singleSnapshot.getValue(String.class);

                            Query queryGetPreviousMeal = FirebaseDatabase.getInstance().getReference()
                                    .child("previous_meal")
                                    .orderByKey()
                                    .equalTo(phoneNumber);

                            queryGetPreviousMeal.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot singleDS : dataSnapshot.getChildren()) {
                                        if (singleDS != null) {
                                            PreviousMeal previousMeal = singleDS.getValue(PreviousMeal.class);

                                            previousDay.add(previousMeal.isPreviousDayMeal());
                                            previousNight.add(previousMeal.isPreviousNightMeal());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            Query queryGetUserDetails = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.dbnode_users))
                                    .orderByKey()
                                    .equalTo(phoneNumber);

                            queryGetUserDetails.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot singleDS : dataSnapshot.getChildren()) {
                                        fetchData(singleDS);
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

    private void fetchData(DataSnapshot singleSnapshot) {
        User boarder = singleSnapshot.getValue(User.class);

        name.add(boarder.getName());
        room.add(boarder.getRoomNumber());
        bed.add(boarder.getBedNumber());
        building.add(boarder.getBuilding());
        guests.add(boarder.getNumberOfGuests());
        phone.add(boarder.getPhoneNumber());

        if (boarder.getDayMealON()) {
            day.add("ON");
        } else {
            day.add("OFF");
        }

        if (boarder.getNightMealON()) {
            night.add("ON");
        } else {
            night.add("OFF");
        }

        if (boarder.getDayMealON() != previousDay.get(previousDay.size() - 1)
                || boarder.getNightMealON() != previousNight.get(previousNight.size() - 1)) {
            isMealChanged.add(true);
        } else {
            isMealChanged.add(false);
        }

        initialize(isMealChanged);
    }

    private void initialize(ArrayList<Boolean> isMealChanged) {

        mRecyclerView = findViewById(R.id.rvList);
        adapter = new BoarderAdapter(this, name, room, bed, building, guests, phone, day, night, isMealChanged);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressBar.setVisibility(View.GONE);

    }

}
