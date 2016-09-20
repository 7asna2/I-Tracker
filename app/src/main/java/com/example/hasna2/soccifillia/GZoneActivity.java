package com.example.hasna2.soccifillia;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GZoneActivity extends AppCompatActivity implements OnMapReadyCallback {

    private int radius ;
    private String name;
    private GoogleMap mMap;
    final private String TAG = this.getClass().getSimpleName();
    private TextView textView;
    MarkerOptions markerOptions;
    CircleOptions circleOptions;
    LatLng lastLatLng;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==1000) {

//            Log.d(TAG, "done clicked");
//            SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
//            SharedPreferences.Editor prefsEditor = mPrefs.edit();
//            Gson gson = new Gson();
//            String json = gson.toJson(markerOptions);
//            prefsEditor.putString("k",json);
//            prefsEditor.apply();

            Log.d(TAG,"pref stored ");
            if (!markerOptions.getPosition().equals(null)) {
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra("L", markerOptions);
                upIntent.putExtra("r",radius);

                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
            }
        }else
            Toast.makeText(getApplicationContext(),"not saved",Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuItem menuItem = menu.add(Menu.NONE, 1000, Menu.NONE,"DONE");
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gzone_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        if ( intent !=null && intent.hasExtra("r")&&intent.hasExtra("n")){
            radius=intent.getIntExtra("r",100);
            name=intent.getStringExtra("n");
            lastLatLng=intent.getParcelableExtra("lastLatLng");
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        textView = (TextView)findViewById(R.id.latLang);
        markerOptions = new MarkerOptions();
        circleOptions = new CircleOptions();
        if (!lastLatLng.equals(null)){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng,15));
        }
        // Add a marker in Sydney and move the camera
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String txt="lat:"+latLng.latitude+",lng:"+latLng.longitude;
                Log.d(TAG,txt);
                textView.setText(txt);
                mMap.clear();
                markerOptions
                        .position(latLng)
                        .title(name)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mMap.addMarker( markerOptions);


                mMap.addCircle(circleOptions
                        .center(latLng)
                        // Fill color of the circle
                        // 0x represents, this is an hexadecimal code
                        // 55 represents percentage of transparency. For 100% transparency, specify 00.
                        // For 0% transparency ( ie, opaque ) , specify ff
                        // The remaining 6 characters(00ff00) specify the fill color
                        .fillColor(0x4000f000)
                        .strokeWidth(7)
                        .strokeColor(0x00f000)
                        .radius(radius));

            }
        });
    }
}
