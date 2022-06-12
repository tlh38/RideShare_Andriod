package net.rideshare_ptc;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestARide extends AppCompatActivity {
    Button riderReq;
    Button retToMenu;
    Ride riderRidePost;
    EditText rpickupLocI;
    EditText rdestLocI;
    EditText rrideDateTimeI;
    CheckBox rsmokingI;
    CheckBox reatingI;
    CheckBox rtalkingI;
    CheckBox rcarseatI;
    String rrideJSON;
    LoginManager mgr = LoginManager.getInstance();
    User loggedInUser = mgr.getLoggedInUser();
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_aride);
        //get the objects (input fields) from the activity - intialize views
        rpickupLocI = (EditText) findViewById(R.id.inptReqPickUpLoc);
        rdestLocI = (EditText) findViewById(R.id.inptReqDestLoc);
        rrideDateTimeI = (EditText) findViewById(R.id.inptReqDateTime); //this needs to be changed to a date picker
        rsmokingI = (CheckBox) findViewById(R.id.ReqcheckBoxSmoking);
        reatingI = (CheckBox) findViewById(R.id.ReqcheckBoxEating);
        rtalkingI = (CheckBox) findViewById(R.id.ReqcheckBoxTalking);
        rcarseatI = (CheckBox) findViewById(R.id.ReqcheckBoxHasCarseat);
        riderReq = (Button) findViewById(R.id.btnReqARide);
        retToMenu = (Button) findViewById(R.id.btnReqRideRetMenu);


                riderReq.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int SDK_INT = Build.VERSION.SDK_INT;
                        if (SDK_INT > 8) {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);

                            if (rpickupLocI.getText().toString().isEmpty() || rdestLocI.getText().toString().isEmpty() || rrideDateTimeI.getText().toString().isEmpty()) {
                                Toast.makeText(RequestARide.this, "Please complete all fields", Toast.LENGTH_SHORT).show();

                            } else {
                                getRiderRideData();
                                try {
                                    postRiderRideDataCreateRideInDB();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                    }
                });

                retToMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), MainMenu.class);
                        startActivity(intent);
                    }

                });
            }

        private void getRiderRideData() {
            //get the data from the form and add to Ride object
            //cannot get an object mapper to work, trying construction JSON Object instead
            //TODO: Enhance input validation- calendar selector for date/time, implement Google API for locations
            //TODO: Add calculations for duration, distance, cost similarly to how handled in webapp
            riderRidePost = new Ride();
            riderRidePost.setRiderID(loggedInUser.getUserID());
            riderRidePost.setRiderScore(loggedInUser.getuRiderScore());
            riderRidePost.setCarseat((byte)0);
            riderRidePost.setTalking((byte)0);
            riderRidePost.setEating((byte)0);
            riderRidePost.setSmoking((byte)0);
            riderRidePost.setPickUpLoc(rpickupLocI.getText().toString());
            riderRidePost.setDest(rdestLocI.getText().toString());
            riderRidePost.setRideDate(rrideDateTimeI.getText().toString());
            if (rsmokingI.isChecked()) {
                riderRidePost.setSmoking((byte) 1);
            }
            if (rtalkingI.isChecked()) {
                riderRidePost.setTalking((byte) 1);
            }
            if (rcarseatI.isChecked()) {
                riderRidePost.setCarseat((byte) 1);
            }
            if (reatingI.isChecked()) {
                riderRidePost.setEating((byte) 1);
            }

            riderRidePost.setIsCompleted((byte) 0);
            riderRidePost.setIsTaken((byte) 0);
            //map to JSON
            ObjectMapper mapper = new ObjectMapper();
            try {
                rrideJSON = mapper.writeValueAsString(riderRidePost);
            } catch (JsonProcessingException e) {
                Toast.makeText(RequestARide.this, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }


            //startActivity(new Intent(DriverPostARide.this,RidePostedSuccess.class).putExtra("Success Ride Posted","Ride Posted: \n"+ rideJSON));
    }

        private void postRiderRideDataCreateRideInDB() throws IOException {


            URL url = new URL("http://10.0.2.2:8080/driverpostaride"); //set URL
            HttpURLConnection con = (HttpURLConnection) url.openConnection(); //open connection
            con.setRequestMethod("POST");//set request method
            con.setRequestProperty("Content-Type", "application/json"); //set the request content-type header parameter
            con.setDoOutput(true); //enable this to write content to the connection OUTPUT STREAM

            //Create the request body
            OutputStream os = con.getOutputStream();
            byte[] input = rrideJSON.getBytes("utf-8");   // send the JSON as bye array input
            os.write(input, 0, input.length);

            //read the response from input stream
            //TODO: Add error handling for any response code other than 200

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                String strResponse = response.toString();

                startActivity(new Intent(RequestARide.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Ride Successfully Posted: \n" + riderRidePost.toString()));
                //get response status code

            }

            con.disconnect();
    }
}