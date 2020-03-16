package com.example.tibsolg.ethiotaxi;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {



    private String driverUID;
    private String customerUID;
    private TextView riderLocation;
    private String rideId;



    private ImageView userImage;

    private TextView rideDate;
    private TextView userName;
    private TextView userPhone;
    private String currentUserId;
    private String userDriverOrCustomer;
    private RatingBar mRatingBar;



    private DatabaseReference historyRideInformationDatabase;

    private LatLng destinationLatitudeLangtude;
    private LatLng pickupLatitudeLongtude;


    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        polylines = new ArrayList<>();

        rideId = getIntent().getExtras().getString("rideId");

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        riderLocation = (TextView) findViewById(R.id.rideLocation);
        rideDate = (TextView) findViewById(R.id.rideDate);
        userName = (TextView) findViewById(R.id.userName);
        userPhone = (TextView) findViewById(R.id.userPhone);

        userImage = (ImageView) findViewById(R.id.userImage);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyRideInformationDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        getRideInfo();

    }

    private void getRideInfo() {
        historyRideInformationDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot child:dataSnapshot.getChildren()){
                        if (child.getKey().equals("customer")){
                            customerUID = child.getValue().toString();
                            if(!customerUID.equals(currentUserId)){
                                userDriverOrCustomer = "Drivers";
                                getUserInfo("Customers", customerUID);
                            }
                        }
                        if (child.getKey().equals("driver")){
                            driverUID = child.getValue().toString();
                            if(!driverUID.equals(currentUserId)){
                                userDriverOrCustomer = "Customers";
                                getUserInfo("Drivers", driverUID);
                            }
                        }
                        if (child.getKey().equals("timestamp")){
                            rideDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if (child.getKey().equals("rating")){
                            mRatingBar.setRating(Integer.valueOf(child.getValue().toString()));

                        }
                        if (child.getKey().equals("destination")){
                            riderLocation.setText(child.getValue().toString());
                        }
                        if (child.getKey().equals("location")){
                            pickupLatitudeLongtude = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destinationLatitudeLangtude = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("lng").getValue().toString()));
                            if(destinationLatitudeLangtude != new LatLng(0,0)){
                                getRouteToMarker();
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getUserInfo(String otherUserDriverOrCustomer, String otherUserId) {
        DatabaseReference otherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserDriverOrCustomer).child(otherUserId);
        otherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        userName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        userPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(userImage);
                    }
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
        String date = DateFormat.format("MM-dd-yyyy hh:mm", calendar).toString();
        return date;
    }
    private void getRouteToMarker() {
        Routing rout = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLatitudeLongtude, destinationLatitudeLangtude)
                .build();
        rout.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap =googleMap;
    }


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRoutingStart() {
    }
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLatitudeLongtude);
        builder.include(destinationLatitudeLangtude);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        googleMap.animateCamera(cameraUpdate);

        googleMap.addMarker(new MarkerOptions().position(pickupLatitudeLongtude).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
        googleMap.addMarker(new MarkerOptions().position(destinationLatitudeLangtude).title("destination"));

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = googleMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onRoutingCancelled() {
    }
    private void deletePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

}
