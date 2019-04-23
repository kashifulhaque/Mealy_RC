package tkzy.mealy_rc.models;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tkzy.mealy_rc.R;

@SuppressWarnings("FieldCanBeLocal")
public class BoarderAdapter extends RecyclerView.Adapter<BoarderAdapter.BoarderViewHolder> {

    // Model Variables
    private ArrayList<String> bed;
    private ArrayList<String> name;
    private ArrayList<String> room;
    private ArrayList<String> phone;
    private ArrayList<String> guests;
    private ArrayList<String> building;
    private ArrayList<String> day;
    private ArrayList<String> night;
    private Context mContext;

    // Variables
    private ArrayList<Boolean> isMealChanged;

    // Constructor
    public BoarderAdapter(Context mContext, ArrayList<String> name, ArrayList<String> room,
                          ArrayList<String> bed, ArrayList<String> building,
                          ArrayList<String> guests, ArrayList<String> phone,
                          ArrayList<String> day, ArrayList<String> night, ArrayList<Boolean> isMealChanged) {
        this.name = name;
        this.room = room;
        this.bed = bed;
        this.building = building;
        this.guests = guests;
        this.mContext = mContext;
        this.phone = phone;
        this.day = day;
        this.night = night;

        this.isMealChanged = isMealChanged;
    }

    // This method is used to create the view and inflate it with values
    @NonNull
    @Override
    public BoarderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_of_boarders_layout, parent, false);

        return new BoarderViewHolder(view);
    }

    // This method binds those controls inside the view to their respective controls
    @Override
    public void onBindViewHolder(@NonNull final BoarderViewHolder holder, int position) {

        holder.mRoom.setText(room.get(position));
        holder.mBed.setText(bed.get(position));
        holder.mName.setText(name.get(position));
        holder.mBuilding.setText(building.get(position));
        holder.mDay.setText(day.get(position));
        holder.mNight.setText(night.get(position));

        holder.mGuests.setText(guests.get(position));

        if (isMealChanged.get(position)) {
            holder.mParent.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
        } else {
            holder.mParent.setBackgroundColor(mContext.getResources().getColor(R.color.gay_white));
        }

        // onClickListener for every item in the RecyclerView
        holder.mParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchUserProfile(phone.get(holder.getAdapterPosition()));
            }
        });

    }

    // This method is used to launch a new Activity which displays the profile of the boarder to the manager
    private void launchUserProfile(String phoneNumber) {
        Intent intent = new Intent(mContext, tkzy.mealy_rc.UserProfileActivity.class);
        intent.putExtra("userPhoneNumber", phoneNumber);
        mContext.startActivity(intent);
    }

    // This method returns the number of items in the RecyclerView
    @Override
    public int getItemCount() {
        return name.size();
    }

    public class BoarderViewHolder extends RecyclerView.ViewHolder {

        // Widgets
        private TextView mRoom, mBed, mName, mBuilding, mGuests, mDay, mNight;
        private CardView mParent;

        public BoarderViewHolder(View itemView) {
            super(itemView);

            mRoom = itemView.findViewById(R.id.tvRoomOfBoarder);
            mBed = itemView.findViewById(R.id.tvBedNumberOfBoarder);
            mName = itemView.findViewById(R.id.tvNameOfBoarder);
            mBuilding = itemView.findViewById(R.id.tvBuildingOfBoarder);
            mGuests = itemView.findViewById(R.id.tvNumberOfGuests);
            mParent = itemView.findViewById(R.id.cvListOfBoarders);
            mDay = itemView.findViewById(R.id.tvDayMealStatus);
            mNight = itemView.findViewById(R.id.tvNightMealStatus);

        }

    }
}