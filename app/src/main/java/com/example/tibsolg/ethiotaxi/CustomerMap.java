package com.example.tibsolg.ethiotaxi;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMap extends FragmentActivity implements OnMapReadyCallback {


    private static final String TAG = "Activity_Login";

    private FusedLocationProviderClient fusedLocationProviderClient;

    Location mPreviousLocation;

    private Button mLogout, mRequestRide, mSettings, mHistory, mCallDriverButton;

    private LatLng pickLocation, destinationLatLng;

    private Boolean customerRequested = false;

    private GoogleMap mMap;

    private Marker marker;

    private SupportMapFragment supportMapFragment;

    private String destination;

    private String serviceRequest;

    private LinearLayout mDriverDetails;

    LocationRequest mCustomerLocationRequest;

    private ImageView mDriverProfilePicture;

    private TextView mDriverName, mDriverPhone, mDriverCar;

    private RadioGroup mRadioGroup;

    private RatingBar mRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        destinationLatLng = new LatLng(0.0,0.0);

        mDriverDetails = (LinearLayout) findViewById(R.id.driverInfo);

        mDriverProfilePicture = (ImageView) findViewById(R.id.driverProfileImage);

        mCallDriverButton = (Button) findViewById(R.id.call_driver_button);

        mDriverName = (TextView) findViewById(R.id.driverName);
        mDriverPhone = (TextView) findViewById(R.id.driverPhone);
        mDriverCar = (TextView) findViewById(R.id.driverCar);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.Black);

        mLogout = (Button) findViewById(R.id.logout);
        mRequestRide = (Button) findViewById(R.id.request);
        mSettings = (Button) findViewById(R.id.settings);
        mHistory = (Button) findViewById(R.id.history);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMap.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CustomerMap.this, CustomerSettings.class);
                startActivity(i);

            }
        });

        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CustomerMap.this, HistoryActivity.class);
                i.putExtra("customerOrDriver", "Customers");
                startActivity(i);

            }
        });

        mRequestRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (customerRequested){
                    endRide();


                }else{
                    int checkedID = mRadioGroup.getCheckedRadioButtonId();

                    final RadioButton rb = (RadioButton) findViewById(checkedID);

                    if (rb.getText() == null){
                        return;
                    }

                    serviceRequest = rb.getText().toString();

                    customerRequested = true;

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire gf = new GeoFire(databaseReference);
                    gf.setLocation(uid, new GeoLocation(mPreviousLocation.getLatitude(), mPreviousLocation.getLongitude()));

                    pickLocation = new LatLng(mPreviousLocation.getLatitude(), mPreviousLocation.getLongitude());
                    marker = mMap.addMarker(new MarkerOptions().position(pickLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

                    mRequestRide.setText("Getting your Driver....");

                    getClosestDriver();
                }
            }
        });




        PlaceAutocompleteFragment placeAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);


        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination= place.getName().toString();
                destinationLatLng = place.getLatLng();

            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


    }
    private int rad = 1;
    private Boolean isDriverLocated = false;
    private String driverLocatedID;

    GeoQuery query;
    private void getClosestDriver(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire gf = new GeoFire(databaseReference);
        query = gf.queryAtLocation(new GeoLocation(pickLocation.latitude, pickLocation.longitude), rad);
        query.removeAllListeners();

        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!isDriverLocated && customerRequested){
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (isDriverLocated){
                                    return;
                                }

                                if(driverMap.get("service").equals(serviceRequest)){
                                    isDriverLocated = true;
                                    driverLocatedID = dataSnapshot.getKey();

                                    DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverLocatedID).child("customerRequest");
                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap hashMap = new HashMap();
                                    hashMap.put("customerRideId", uid);
                                    hashMap.put("destination", destination);
                                    hashMap.put("destinationLat", destinationLatLng.latitude);
                                    hashMap.put("destinationLng", destinationLatLng.longitude);
                                    databaseReference2.updateChildren(hashMap);

                                    getDriverLocation();
                                    getDriverInfo();
                                    getHasRideEnded();
                                    mRequestRide.setText("Looking for Driver Location....");
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!isDriverLocated)
                {
                    rad++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker marker1;
    private DatabaseReference reference;
    private ValueEventListener valueEventListener;
    private void getDriverLocation(){
        reference = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverLocatedID).child("l");
        valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && customerRequested){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLatitude = 0;
                    double locationLongitude = 0;
                    if (map != null && map.get(0) != null) {
                        locationLatitude = Double.parseDouble(map.get(0).toString());
                    }
                    if (map != null && map.get(1) != null) {
                        locationLongitude = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatitudeLongitude = new LatLng(locationLatitude,locationLongitude);
                    if(marker1 != null){
                        marker1.remove();
                    }
                    Location location1 = new Location("");
                    location1.setLatitude(pickLocation.latitude);
                    location1.setLongitude(pickLocation.longitude);

                    Location location2 = new Location("");
                    location2.setLatitude(driverLatitudeLongitude.latitude);
                    location2.setLongitude(driverLatitudeLongitude.longitude);

                    float d = location1.distanceTo(location2);

                    if (d<100){
                        mRequestRide.setText("The Driver Has Arrived");
                    }else{
                        mRequestRide.setText("Driver Found: " + String.valueOf(d));
                    }



                    marker1 = mMap.addMarker(new MarkerOptions().position(driverLatitudeLongitude).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void getDriverInfo(){
        mDriverDetails.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverLocatedID);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("name")!=null){
                        mDriverName.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if(dataSnapshot.child("phone")!=null){
                        final String driverPhone = dataSnapshot.child("phone").getValue().toString();
                        mDriverPhone.setText(driverPhone);
                        mCallDriverButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(Intent.ACTION_CALL);
                                i.setData(Uri.parse("tel:" + driverPhone.toString()));
                                if (ContextCompat.checkSelfPermission(CustomerMap.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                    startActivity(i);
                                } else {
                                    Toast.makeText(CustomerMap.this, "Permission Not Given", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    if(dataSnapshot.child("car")!=null){
                        mDriverCar.setText(dataSnapshot.child("car").getValue().toString());
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(mDriverProfilePicture);
                    }

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private DatabaseReference databaseReference;
    private ValueEventListener eventListener;
    private void getHasRideEnded(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverLocatedID).child("customerRequest").child("customerRideId");
        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        customerRequested = false;
        query.removeAllListeners();
        reference.removeEventListener(valueEventListener);
        databaseReference.removeEventListener(eventListener);

        if (driverLocatedID != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverLocatedID).child("customerRequest");
            driverRef.removeValue();
            driverLocatedID = null;

        }
        isDriverLocated = false;
        rad = 1;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire gf = new GeoFire(databaseReference);
        gf.removeLocation(uid);

        if(marker != null){
            marker.remove();
        }
        if (marker1 != null){
            marker1.remove();
        }
        mRequestRide.setText("Call Weraj Ale");

        mDriverDetails.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverCar.setText("Destination: --");
        mDriverProfilePicture.setImageResource(R.mipmap.ic_default_user);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mCustomerLocationRequest = new LocationRequest();
        mCustomerLocationRequest.setInterval(1000);
        mCustomerLocationRequest.setFastestInterval(1000);
        mCustomerLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                checkLocationPermission();
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(mCustomerLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mPreviousLocation = location;

                    LatLng latitudeLongitude = new LatLng(location.getLatitude(),location.getLongitude());

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latitudeLongitude));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    if(!getDriversNearbyStarted)
                        getDriversNearby();
                }
            }
        }
    };


    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("Let Weraj Ale use location services.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMap.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMap.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        fusedLocationProviderClient.requestLocationUpdates(mCustomerLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Let Weraj Ale use location services", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }




    List<Marker> markers = new ArrayList<Marker>();
    boolean getDriversNearbyStarted = false;
    private void getDriversNearby(){
        getDriversNearbyStarted = true;
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire gf = new GeoFire(driverLocation);
        GeoQuery query = gf.queryAtLocation(new GeoLocation(mPreviousLocation.getLongitude(), mPreviousLocation.getLatitude()), 999999999);

        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                try {
                    for (Marker markerIt : markers) {
                        if (markerIt.getTag() != null) {
                            if (markerIt.getTag().equals(key))
                                return;
                        }
                    }

                    LatLng LatitudeLongitude = new LatLng(location.latitude, location.longitude);

                    Marker marker = mMap.addMarker(new MarkerOptions().position(LatitudeLongitude).title(key).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                    marker.setTag(key);

                    markers.add(marker);

                }catch (Exception e)
                {
                    Toast.makeText(CustomerMap.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markers){
                    if (markerIt.getTag() != null) {
                        if (markerIt.getTag().equals(key)) {
                            markerIt.remove();
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    //try {
                    if(markerIt.getTag()!=null) {
                        if (markerIt.getTag().equals(key)) {
                            markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                    //}
                    /*catch (NullPointerException e) {
                        Toast.makeText(CustomerMap.this, "Error onKeyMoved", Toast.LENGTH_SHORT).show();
                    }*/
                }
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}
