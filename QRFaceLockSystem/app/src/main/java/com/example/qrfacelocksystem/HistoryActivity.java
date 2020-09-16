package com.example.qrfacelocksystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser users;
    private FirebaseDatabase database;
    private DatabaseReference mDataRef;

    private ActionBar actionBar;

    private RecyclerView historyRecyclerView;
//    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = mAuth.getCurrentUser();

        setActionBar("Door History");

        mDataRef = database.getReference("/Users Details/"+ users.getUid() + "/Attempt History");


//        createHistoryList();

        historyRecyclerView = (RecyclerView) findViewById(R.id.historyView);
        historyRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        historyRecyclerView.setLayoutManager(mLayoutManager);

    }


//    private void createHistoryList(){
//
//        mDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                for(DataSnapshot data: dataSnapshot.getChildren()){
////                     historyList.add(new HistoryItem((R.drawable.lock_icon), data.child(), "Time", "LOCK"));
////                     historyList.add(new HistoryItem((R.drawable.unlock_icon), "Device 2", "Time", "UNLOCK"));
//
//                }
//
//
//
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//
//    }

    private void getFirebaseHistoryDB(){

    }

    @Override
    protected void onStart() {
        super.onStart();
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

        public int mImageResource;
        public String deviceName;
        public String time;
        public String lock_Status;

        public HistoryItem() {

        }

        public HistoryItem(String deviceName, String time, String lock_Status){
//            this.mImageResource = imageResource;
            this.deviceName = deviceName;
            this.time = time;
            this.lock_Status = lock_Status;
        }

        public int getImageResource(){
            return  mImageResource;
        }

        public String getDeviceName(){
            return deviceName;
        }

        public String getDateTime(){
            return time;
        }

        public String getLockStatus(){
            return lock_Status;
        }

        public void setImageResource(int mImageResource) {
            this.mImageResource = mImageResource;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public void setTime(String time) {
            this.time = time;
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
    public  ArrayList<HistoryActivity.HistoryItem> mHistoryList;

    public static class HistoryViewHolder extends RecyclerView.ViewHolder{

//        public ImageView mImageView;
        public TextView deviceName;
        public TextView time;
        public TextView lock_Status;

        public HistoryViewHolder(View itemView) {
            super(itemView);
//            mImageView = itemView.findViewById(R.id.imageView);
            deviceName = itemView.findViewById(R.id.deviceName_history);
            time = itemView.findViewById(R.id.dateTime_history);
            lock_Status = itemView.findViewById(R.id.lockStatus_history);

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

//        holder.mImageView.setImageResource(currentHistory.getImageResource());
        holder.deviceName.setText(currentHistory.getDeviceName());
        holder.time.setText(currentHistory.getDateTime());

        if(currentHistory.equals("Lock")){
            holder.lock_Status.setTextColor(Color.RED);
            holder.lock_Status.setText(currentHistory.getLockStatus());
        }else if (currentHistory.equals("Unlock")){
            holder.lock_Status.setTextColor(Color.GREEN);
            holder.lock_Status.setText(currentHistory.getLockStatus());
        }
    }


    @Override
    public int getItemCount() {
        return mHistoryList.size();
    }
}