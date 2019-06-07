package com.sriniavsprojectpool.dataset;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import android.view.View;

import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnSuccessListener;


import java.io.File;

import java.io.FileWriter;
import java.io.IOException;


public class GrabDetails extends AppCompatActivity {


    //variable diclarations for permissions , inputs , locations , files , dialougs and progressbars
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 199;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 200;
    private AppCompatEditText building, room, floor;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private ProgressDialog progress;
    File root;
    File gpxfile;
    FileWriter writer;

    public int count =0;


    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grab_details);
        building = findViewById(R.id.Building);
        floor = findViewById(R.id.floor);
        room = findViewById(R.id.room);


        //getting provider from google client after perfect setup
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        progress=new ProgressDialog(this);
        progress.setMessage("Initiating Process");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.setCancelable(false);
        builder = new AlertDialog.Builder(this);



    }

    public void beginbAction(View view) {

        //onclick of buttion triegrs this method button{Begin Process}

        //requests location permissions
        if (!building.getText().toString().trim().equals("") && !room.getText().toString().trim().equals("") && !floor.getText().toString().trim().equals("")) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {

                requestStoragePermission();

            }
        } else {
            Toast.makeText(this, "Please enter valid data", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        //after gatting response from user regarding permission
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    requestStoragePermission();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Provide permissions to run the app", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    requestStoragePermission();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Provide permissions to run the app", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void requestStoragePermission() {

        //requesting storage permission here
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_STORAGE);
        } else {

            //when all permissions are talied process bigins
            beginActionNow();

        }

    }

    private void beginActionNow() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            Toast.makeText(this, "You must give permissions", Toast.LENGTH_LONG).show();
            return;
        }



        //returns if Location disabled or network disabled
        if(!isLocationEnabled())
        {
            Toast.makeText(this, "Location Not enabled", Toast.LENGTH_LONG).show();
            return;
        }



        //getting last known location from the client or provider
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        //initializing the file to write location coordinates
                        root = new File(Environment.getExternalStorageDirectory(), "DataCordinates");
                        if (!root.exists()) {
                            root.mkdirs();
                        }

                        // checking this not to be null and when we got last known location then we are initializing the location updates
                        if (location != null) {
                            //Toast.makeText(GrabDetails.this, "" + location.toString(), Toast.LENGTH_LONG).show();
                            progress.show();
                            //location updates initialization
                            locationCallback = new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    if (locationResult == null) {
                                        return;
                                    }
                                    for (Location location : locationResult.getLocations()) {


                                        //getting locations periodically
                                        if(location.getAltitude()!=0.0) {
                                            generateNoteOnSD(getApplicationContext(), "DataFile.csv", location);

                                            count++;
                                            //Toast.makeText(GrabDetails.this, "" + location.toString(), Toast.LENGTH_LONG).show();
                                            progress.setMessage("Location recorded count :" + count+"/500");
                                            if (count == 500) {
                                                progress.dismiss();
                                                count = 0;
                                                fusedLocationClient.removeLocationUpdates(locationCallback);
                                                showContinueDialouge();

                                            }
                                        }
                                        else if(!isLocationEnabled())
                                        {

                                            Toast.makeText(GrabDetails.this, "Please turn on location" , Toast.LENGTH_LONG).show();
                                        }
                                        else
                                        {
                                            Toast.makeText(GrabDetails.this, "Accuracy is too low , Please Move Around or Run on high end  device. Accuracy is :"+location.getAccuracy()+" Meters.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                ;
                            };
                            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(getApplicationContext(), "You must give permissions", Toast.LENGTH_LONG).show();
                                return;
                            }
                            //setting location settings to get high accuracy
                            LocationRequest locationRequest = LocationRequest.create();
                            locationRequest.setInterval(1000);
                            locationRequest.setFastestInterval(500);
                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            fusedLocationClient.requestLocationUpdates(locationRequest,
                                    locationCallback,
                                    null /* Looper */);
                        }
                        else
                        {
                            Toast.makeText(GrabDetails.this, "Not getting valid location , Trying to initiate  again.", Toast.LENGTH_LONG).show();
                            beginActionNow();
                        }
                    }
                });


    }

    private void showContinueDialouge() {

        //To perform action after succesful location storage
        builder.setMessage("Click OK when you are ready to capture locations.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Toast.makeText(getApplicationContext(),"you have choosen to continue.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                        finish();
                        Toast.makeText(getApplicationContext(),"you can exit the app. All resources are released Thank you.", Toast.LENGTH_LONG).show();
                    }
                });

        AlertDialog alert = builder.create();

        alert.setTitle("Do you Take More Coordinates or willing to quit?");
        alert.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if(fusedLocationClient!=null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public void generateNoteOnSD(Context context, String sFileName, Location location) {

        //storaing data on files
        try {

            String lat = String.valueOf(location.getLatitude());
            String lon = String.valueOf(location.getLongitude());
            String alt = String.valueOf(location.getAltitude());

            gpxfile = new File(root, "DataFile.csv");
            String position =  building.getText().toString().trim()+"-"+floor.getText().toString().trim()+"-"+room.getText().toString().trim();
            writer = new FileWriter(gpxfile,true);
            writer.append(lat+","+lon+","+alt+","+position+"\n");
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "something went wrong while storing data. ", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isLocationEnabled()
    {

        //checking wheather location and network is enabled
        LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        boolean enebled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            Toast.makeText(this, "Location or Network not available", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return false;
        }
        return true;
    }


}
