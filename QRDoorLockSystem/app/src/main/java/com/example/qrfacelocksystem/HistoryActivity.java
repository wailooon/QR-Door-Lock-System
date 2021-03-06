package com.example.qrfacelocksystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class HistoryActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private FirebaseAuth mAuth;
    private FirebaseUser users;
    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    private ActionBar actionBar;

    private RecyclerView historyRecyclerView;
//    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String device_name_db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = mAuth.getCurrentUser();

        setActionBar("Door Attempt History");

        historyRecyclerView = (RecyclerView) findViewById(R.id.historyView);
        historyRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        historyRecyclerView.setLayoutManager(mLayoutManager);

    }

    private void load_data() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        device_name_db = sharedPref.getString("device_name_db", "");

    }

    @Override
    protected void onStart() {
        super.onStart();

        load_data();

        mDataRef = database.getReference("/Users Details/"+ users.getUid() + "/Attempt History/" + device_name_db);

        if(mDataRef != null){

            mDataRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {
                        ArrayList<HistoryItem> historyList = new ArrayList<>();

                        for(DataSnapshot data : dataSnapshot.getChildren()){
                            historyList.add(data.getValue(HistoryItem.class));
                        }

                        HistoryAdapter mAdapter = new HistoryAdapter(historyList);
                        historyRecyclerView.setAdapter(mAdapter);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    notification(error.getMessage());
                }
            });

        }
    }

    private void setActionBar(String heading) {
        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));
        actionBar.setTitle(heading);
        actionBar.show();

    }

    public static class HistoryItem{

        public String mImageResource;
        public String deviceName;
        public String dateAndTime;
        public String lock_Status;
        public String description;

        public HistoryItem() {

        }

        public HistoryItem(String imageResource, String deviceName,String description, String dateAndTime, String lock_Status){
            this.description = description;
            this.mImageResource = imageResource;
            this.deviceName = deviceName;
            this.dateAndTime = this.dateAndTime;
            this.lock_Status = lock_Status;
        }

        public String getImageResource(){
            return  mImageResource;
        }

        public String getDeviceName(){
            return deviceName;
        }

        public String getDateTime(){
            return dateAndTime;
        }

        public String getLockStatus(){
            return lock_Status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setImageResource(String mImageResource) {
            this.mImageResource = mImageResource;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public void setDateAndTime(String dateAndTime) {
            this.dateAndTime = dateAndTime;
        }

        public void setLock_Status(String lock_Status) {
            this.lock_Status = lock_Status;
        }


    }

    private void notification(String message) {
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }



}

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>{

    public static Context context;

    public  ArrayList<HistoryActivity.HistoryItem> mHistoryList;

    public static class HistoryViewHolder extends RecyclerView.ViewHolder{

        public ImageView mImageView;
        public TextView deviceName;
        public TextView time;
        public TextView lock_Status;
        public TextView description;

        public HistoryViewHolder(View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.imageView);
            deviceName = itemView.findViewById(R.id.deviceName_history);
            time = itemView.findViewById(R.id.dateTime_history);
            lock_Status = itemView.findViewById(R.id.lockStatus_history);
            description = itemView.findViewById(R.id.description_history);

        }
    }

    public HistoryAdapter(ArrayList<HistoryActivity.HistoryItem> historyList){
        this.mHistoryList = historyList;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from((parent.getContext())).inflate(R.layout.history_item, parent, false);
        HistoryViewHolder hvh = new HistoryViewHolder(v);
        return hvh;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        HistoryActivity.HistoryItem currentHistory = mHistoryList.get(position);

        context = holder.itemView.getContext();

        //Loading image from Glide library.
        Glide.with(context).load(currentHistory.getImageResource()).into(holder.mImageView);

        holder.deviceName.setText(currentHistory.getDeviceName());
        holder.time.setText(currentHistory.getDateTime());
        holder.lock_Status.setText(currentHistory.getLockStatus());
        holder.description.setText(currentHistory.getDescription());
    }


    @Override
    public int getItemCount() {
        return mHistoryList.size();
    }
}