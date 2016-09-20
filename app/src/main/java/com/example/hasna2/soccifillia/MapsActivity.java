package com.example.hasna2.soccifillia;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    final private String TAG =this.getClass().getSimpleName();
    SharedPreferences mPrefs ;
    ArrayList< MarkerOptions> markerOptionsAl;
    ArrayList<Integer> radiusAl;
    MarkerOptions currentMarker;
    LatLng lastLatLngGlobal;
    ImageView syncImageView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPrefs = getPreferences(MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FloatingActionButton refresh = (FloatingActionButton) findViewById(R.id.refresh);
        FloatingActionButton locate = (FloatingActionButton) findViewById(R.id.locate);
        FloatingActionButton bluetooth = (FloatingActionButton) findViewById(R.id.bluetooth);

        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), BluetoothActivity.class);
                startActivity(intent);
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();

            }
        });
        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firedialog();
            }
        });
        ImageButton fake = (ImageButton) findViewById(R.id.fake1);
        fake.setImageResource(R.drawable.route);
        ImageButton fake2 = (ImageButton) findViewById(R.id.fake2);
        fake2.setImageResource(R.drawable.placeholder);

        fake2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        markerOptionsAl = new ArrayList<>();
        radiusAl=new ArrayList<>();
        currentMarker = new MarkerOptions();
        syncImageView = (ImageView)findViewById(R.id.sync);



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        update();
    }

    public void update() {
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            public void updateMap(LatLng lastLatLang) {
                //lastLatLang = new LatLng(-34, 151);
//                mMap.clear();
                if(lastLatLang.equals(null))
                    Toast.makeText(getApplicationContext(),"Error fetching data",Toast.LENGTH_SHORT).show();
                currentMarker.position(lastLatLang).title("current");
                mMap.addMarker(currentMarker);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLang,15));
                lastLatLngGlobal=lastLatLang;
//                mMap.setBuildingsEnabled(true);
//                Gson gson = new Gson();
//                String json = mPrefs.getString("MyObject", "");
//                MarkerOptions obj = gson.fromJson(json, MarkerOptions.class);
//                if(!obj.equals(null)){
//                    Log.d(TAG,"object retrived");
//                mMap.addMarker(obj);}

            }

            @Override
            public void onProgressUpdate(boolean b) {
                if(b)
                    syncImageView.setVisibility(View.GONE);
                else
                    syncImageView.setVisibility(View.VISIBLE);
            }
        };
        asyncTask.execute();
        Intent intent = getIntent();
        if(!intent.equals(null) && intent.hasExtra("L")) {
            MarkerOptions markerOptions = intent.getParcelableExtra("L");
            markerOptionsAl.add(markerOptions);
            radiusAl.add(intent.getIntExtra("r", 1));
        }

        for (int i=0 ; i<markerOptionsAl.size();i++){
            mMap.addMarker(markerOptionsAl.get(i));
            mMap.addCircle(new CircleOptions()
                    .center(markerOptionsAl.get(i).getPosition())
                    // Fill color of the circle
                    // 0x represents, this is an hexadecimal code
                    // 55 represents percentage of transparency. For 100% transparency, specify 00.
                    // For 0% transparency ( ie, opaque ) , specify ff
                    // The remaining 6 characters(00ff00) specify the fill color
                    .fillColor(0x4000f000)
                    .strokeWidth(7)
                    .strokeColor(0x00f000)
                    .radius(radiusAl.get(i)));
            Log.d(TAG,markerOptionsAl.get(i).getTitle()+"i");
        }


    }


    public void firedialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.dialog_title);
        alertDialog.setMessage("Enter Info");

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText editTextR = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextR.setLayoutParams(lp);
        editTextR.setHint(R.string.GZone_r);
        editTextR.setInputType(InputType.TYPE_CLASS_NUMBER);

        final EditText editTextName = new EditText(this);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        editTextName.setLayoutParams(lp2);
        editTextName.setHint(R.string.GZone_name);


        layout.addView(editTextName);
        layout.addView(editTextR);
        alertDialog.setView(layout);
        LayoutInflater inflater =this.getLayoutInflater();
//        alertDialog.setView(inflater.inflate(R.layout.dialog_gzone, null));
        alertDialog.setIcon(R.drawable.icecream);

//        final EditText editTextName= (EditText)findViewById(R.id.gzone_name);
//        final EditText editTextR= (EditText)findViewById(R.id.gzone_r);

        alertDialog.setPositiveButton("DONE",
                new DialogInterface.OnClickListener() {
                    int radius;
                    String name;
                    public void onClick(DialogInterface dialog, int which) {

                        try
                        {
                            radius =Integer.parseInt(editTextR.getText().toString());
                            name=(editTextName).getText().toString();
                            if (radius <=0) throw new Exception ("wrong radius entered") ;
                            Intent intent = new Intent(getBaseContext(),GZoneActivity.class);
                            intent.putExtra("r",radius);
                            intent.putExtra("n",name);
                            intent.putExtra("lastLatLng",lastLatLngGlobal);
                            startActivity(intent);
                            dialog.cancel();
                        }catch (Exception e){
                            Log.d(TAG,"exception :"+e.getMessage());
                            Toast.makeText(getApplicationContext(),"Wrong radius value",Toast.LENGTH_SHORT).show();
                        }
                        finally {
                            Log.d(TAG,"finally"+editTextR.getText().toString());
                        }
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();

    }

}
