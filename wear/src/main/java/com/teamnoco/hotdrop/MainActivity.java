package com.teamnoco.hotdrop;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import java.util.List;


public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;
    private final String TAG = "TAGSTRING";
    private Location mLocation;
    private Location mSaved;
    private static final int SPEECH_REQUEST_CODE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);

                Button btnMakeDrop = (Button)findViewById(R.id.enter_location);
                btnMakeDrop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       Log.e("tag","Hi my button works!");
                        String btnText = ((Button)v).getText().toString();
                        mSaved = mLocation;
                        if(btnText.equals("Make Drop!"))
                        {
                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            // Start the activity, the intent will be populated with the speech text
                            startActivityForResult(intent, SPEECH_REQUEST_CODE);
                            ((Button)v).setText("Aquire Drop!");
                        }
                        else
                        {
                            // Build an intent for an action to view a map
                            Log.e("I'm here","asdfasdfsadf");
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW);

                            Uri geoUri = Uri.parse("http://maps.google.com/maps?saddr=42.35892," +
                                    "-71.05781&daddr=40.756054,-73.986951");
                            mapIntent.setData(geoUri);

                            PendingIntent mapPendingIntent =
                                    PendingIntent.getActivity(getApplicationContext(), 0, mapIntent, 0);

                            PendingIntent viewPendingIntent =
                                    PendingIntent.getActivity(getApplicationContext(), 0, getIntent(), 0);

                           /* NotificationCompat.Builder notificationBuilder =
                                    new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentTitle("Title")
                                            .setContentText("Location")
                                            //.setContentIntent(viewPendingIntent)
                                            .extend(new NotificationCompat.WearableExtender())
                                            //.setContentIntent(viewPendingIntent)
                                            .addAction(R.drawable.ic_launcher,
                                                    "Map", mapPendingIntent);*/


                            NotificationCompat.Builder notificationBuilder =
                                    new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setContentTitle("Location")
                                            .setContentText("Here's the location")
                                            .setContentIntent(viewPendingIntent)
                                            .setPriority(Notification.PRIORITY_MAX)
                                            .addAction(R.drawable.ic_map,
                                                   "Map", mapPendingIntent)
                                            .extend(new NotificationCompat.WearableExtender());
                            //
                        //notificationBuilder.build();

                            NotificationManagerCompat notificationManager =
                                    NotificationManagerCompat.from(getApplicationContext());

// Build the notification and issues it with notification manager.
                            notificationManager.notify(999, notificationBuilder.build());
                        }

                    }

                });
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);

            }


        });
    }


    @Override
    protected void onPause() {
        super.onPause();

        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("I'm resuming", "resuming");
        mGoogleApiClient.connect();
    }



    @Override
    public void onConnected(Bundle bundle) {
        Log.e("HI", "I'm here!!!#$!@#$");
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3)
                .setFastestInterval(3);

        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {

                        if (status.getStatus().isSuccess()) {
                            if (Log.isLoggable(TAG, Log.DEBUG)) {
                                Log.d(TAG, "Successfully requested location updates");
                            }
                        } else {
                            Log.e(TAG,
                                    "Failed in requesting location updates, "
                                            + "status code: "
                                            + status.getStatusCode() + ", message: " + status
                                            .getStatusMessage());
                        }
                    }
                });

       /* LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                .setResultCallback(new ResultCallback<Status>() {



                    @Override
                    public void onResult(Status status) {
                        Log.e("Im in on result","On result");
                        if (status.getStatus().isSuccess()) {
                            if (Log.isLoggable(TAG, Log.DEBUG)) {
                                Log.d(TAG, "Successfully requested location updates");
                            }
                        } else {
                            Log.e(TAG,
                                    "Failed in requesting location updates, "
                                            + "status code: "
                                            + status.getStatusCode()
                                            + ", message: "
                                            + status.getStatusMessage());
                        }
                    }


                });*/

    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        Log.e("Hi i have locations","locations are cool!");
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.e("Spoken Text","Text: " + spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("failed","connection");
    }
}
