package com.example.tibsolg.ethiotaxi;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.tibsolg.ethiotaxi.recyclerView.HistAdapter;
import com.example.tibsolg.ethiotaxi.recyclerView.HistObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;


public class HistoryActivity extends AppCompatActivity {
    private String customerOrDriver, uid;

    private RecyclerView historyRecyclerview;
    private RecyclerView.Adapter historyAdapter;
    private RecyclerView.LayoutManager historyLayoutManager;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);




        historyRecyclerview = (RecyclerView) findViewById(R.id.historyRecyclerView);
        historyRecyclerview.setNestedScrollingEnabled(false);
        historyRecyclerview.setHasFixedSize(true);
        historyLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        historyRecyclerview.setLayoutManager(historyLayoutManager);
        historyAdapter = new HistAdapter(getDataSetHistory(), HistoryActivity.this);
        historyRecyclerview.setAdapter(historyAdapter);


        customerOrDriver = getIntent().getExtras().getString("customerOrDriver");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();



    }

    private void getUserHistoryIds() {
        DatabaseReference userHistoryDB = FirebaseDatabase.getInstance().getReference().child("Users").child(customerOrDriver).child(uid).child("history");
        userHistoryDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history : dataSnapshot.getChildren()){
                        fetchRiderInfo(history.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void fetchRiderInfo(String rideKey) {
        DatabaseReference historyDB = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historyDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String rideId = dataSnapshot.getKey();
                    Long timestamp = 0L;

                    if(dataSnapshot.child("timestamp").getValue() != null){
                        timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                    }

                    HistObject obj = new HistObject(rideId, getDate(timestamp));
                    resltHistory.add(obj);
                    historyAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String getDate(Long time) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(time*1000);
        String hDate = DateFormat.format("MM-dd-yyyy hh:mm", calendar).toString();
        return hDate;
    }

    private ArrayList resltHistory = new ArrayList<HistObject>();
    private ArrayList<HistObject> getDataSetHistory() {
        return resltHistory;
    }




    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");
    ProgressDialog progress;

}
