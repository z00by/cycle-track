package uk.co.zoobyware.cycletrack;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import uk.co.zoobyware.cycletrack.output.GpxBuilder;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleApiClient googleApiClient;

    private static final String LOG_TAG = "cycletrack";

    private static final int LOCATION_PERMISSION = 0;
    private static final int WRITE_PERMISSION = 1;

    private boolean tracking = false;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private GpxBuilder gpxBuilder;

    private Location previous = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidThreeTen.init(this);

        setContentView(R.layout.activity_main);

        // Create an instance of GoogleAPIClient
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Get the manager from the system
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Create the locationListener that we will be adding and removing
        locationListener = new LocationListener() {
            public void onLocationChanged(final Location location) {
                recordLocation(location);
            }

            public void onStatusChanged(final String provider, final int status, final Bundle extras) {
            }

            public void onProviderEnabled(final String provider) {
            }

            public void onProviderDisabled(final String provider) {
            }
        };

    }

    public void recordLocation(final Location loc) {
        final float minMovement = 10;
        final long minTime = 2;
        float distance = 0;
        long time = 0;

        if (previous != null) {
            distance = loc.distanceTo(previous);
            time = loc.getTime() - previous.getTime();
        }

        if (previous == null || (distance >= minMovement && time >= minTime)) {
            gpxBuilder.location(loc);
            previous = loc;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        googleApiClient.connect();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        googleApiClient.disconnect();
    }

    private void saveGpx() {
        try {
            gpxBuilder.write(getStorageDirectory());
        } catch (TransformerException | ParserConfigurationException e) {
            Log.e(LOG_TAG, "Failed to create xml gpxBuilder", e);

            finish();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to write GPX", e);

            finish();
        }
    }

    @Override
    public void onConnected(@Nullable final Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(final int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {

    }

    public void toggleTracking(final View view) {
        if (tracking) {
            stopTracking();
        } else {
            startTracking();
        }
    }

    private void startTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
        }

        if (isFineLocationPending()) {
            final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION};

            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSION);
            return;
        }

        //noinspection MissingPermission - isFineLocationPending() checks the permission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        gpxBuilder = new GpxBuilder();
        gpxBuilder.track();

        tracking = true;
    }

    private boolean isFineLocationPending() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private void stopTracking() {
        if (isFineLocationPending()) {
            return;
        }
        locationManager.removeUpdates(locationListener);

        previous = null;

        tracking = false;

        saveGpx();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            tracking = true;
        } else if (requestCode == WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Cycle-track cannot function properly unless given permission to write to external storage. Please consider granting it this permission", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private File getStorageDirectory() {
        final File file;

        if (isExternalStorageWritable()) {
            file = new File(Environment.getExternalStorageDirectory(), "uk.co.zoobyware.cycletrack");
        } else {
            file = new File(Environment.getDataDirectory(), "uk.co.zoobyware.cycletrack");
        }

        if (!file.mkdirs() && !file.isDirectory()) {
            Log.e(LOG_TAG, "Unable to create output directory " + file.getAbsolutePath());
            finish();
        }

        return file;
    }

}
