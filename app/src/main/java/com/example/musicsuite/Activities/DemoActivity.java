package com.example.musicsuite.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.musicsuite.BuildConfig;
import com.example.musicsuite.Fragments.MainScreenFragment;
import com.example.musicsuite.R;
import com.example.musicsuite.Songs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class DemoActivity extends AppCompatActivity {
    private String endPoint = "https://emotionpackedapi.cognitiveservices.azure.com/face/v1.0/";
    private static final String API_KEY = BuildConfig.ApiKey;

    private final FaceServiceClient faceServiceClient = new FaceServiceRestClient(endPoint, API_KEY);

    JSONObject jsonObject, jsonObject1;
    ImageView imageView;
    Bitmap mBitmap;
    boolean takePicture = false;

    private ProgressDialog detectionProgressDialog;
    Face[] facesDetected;

    /**
     * Spotify things
     */

    private RequestQueue queue;
    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;
    // TODO: 09-04-2021 Complete the Demo Activity and create a new Client Id for this package and change the REDIRECT_URI

    private static final String CLIENT_ID = BuildConfig.SpotifyKey;
    private static final String REDIRECT_URI = "com.example.musicsuite://callback";
    private static final int REQUEST_CODE = 1338;
    private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private";

    ArrayList<Songs> songList;
    boolean isAuthenticated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        // Getting the songs
        songList = MainScreenFragment.Statified.INSTANCE.getGetSongsList();


        detectionProgressDialog = new ProgressDialog(this);

        jsonObject = new JSONObject();
        jsonObject1 = new JSONObject();
//        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.billgates);
        imageView = findViewById(R.id.imageView);
//        imageView.setImageBitmap(mBitmap);
        Button btnDetect = findViewById(R.id.btnDetectFace);
//        Button btnIdentify = findViewById(R.id.btnIdentify);

        btnDetect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isAuthenticated) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DemoActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
                    } else {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, 0);
                    }
                } else {
                    authenticateSpotify();
                }
            }
        });
        if (!isAuthenticated) {
            authenticateSpotify();
        }


        msharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this);

    }

    //     To authenticate the user using token
    private void authenticateSpotify() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{SCOPES});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
// Emotion Detection Dialog
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            detectAndFrame(bitmap);
        }

// Spotify Part
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    Log.e("DemoActivity", "Token:" + response.getAccessToken());
                    Log.d("STARTING", "GOT AUTH TOKEN");
                    editor.apply();
                    Toast.makeText(this, "oAuth Completed Now you can Continue!!!", Toast.LENGTH_SHORT).show();
                    isAuthenticated = true;
                    Toast.makeText(getApplicationContext(), "Press the Detect Button to take a picture. Press Identify to identify the person.", Toast.LENGTH_LONG).show();
                    break;

                // Auth flow returned an error
                case ERROR:
                    Toast.makeText(this, "TOKEN ERROR", Toast.LENGTH_SHORT).show();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }

    private void detectAndFrame(final Bitmap imageBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        @SuppressLint("StaticFieldLeak") AsyncTask<InputStream, String, Face[]> detectTask =

                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            final com.microsoft.projectoxford.face.contract.Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    // returnFaceAttributes:
                                    new FaceServiceClient.FaceAttributeType[]{
                                            FaceServiceClient.FaceAttributeType.Emotion,
                                            FaceServiceClient.FaceAttributeType.Gender}
                            );

                            for (int i = 0; i < result.length; i++) {
                                jsonObject.put("happiness", result[i].faceAttributes.emotion.happiness);
                                jsonObject.put("sadness", result[i].faceAttributes.emotion.sadness);
                                jsonObject.put("surprise", result[i].faceAttributes.emotion.surprise);
                                jsonObject.put("neutral", result[i].faceAttributes.emotion.neutral);
                                jsonObject.put("anger", result[i].faceAttributes.emotion.anger);
                                jsonObject.put("contempt", result[i].faceAttributes.emotion.contempt);
                                jsonObject.put("disgust", result[i].faceAttributes.emotion.disgust);
                                jsonObject.put("fear", result[i].faceAttributes.emotion.fear);
                                Log.e(TAG, "doInBackground: " + jsonObject.toString());

                                jsonObject1.put((String.valueOf(i)), jsonObject);
                            }

                            Map<String, Object> retMap = new Gson().fromJson(jsonObject.toString(), new TypeToken<HashMap<String, Object>>() {
                            }.getType());
                            double maxVal = 0;
                            String maxKeyValue = "";
                            for (Map.Entry<String, Object> entry : retMap.entrySet()) {
                                if ((double) entry.getValue() > maxVal) {
                                    maxVal = (double) entry.getValue();
                                    maxKeyValue = entry.getKey();
                                }
                            }
                            final String emotionValue = maxKeyValue;
                            final double maxKey = maxVal;
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
//                                    Toast.makeText(DemoActivity.this, "DATA" + jsonObject1.toString(), Toast.LENGTH_LONG).show();
                                    Toast.makeText(DemoActivity.this, "Emotion Key: " + emotionValue + " , Max Value: " + maxKey, Toast.LENGTH_LONG).show();
//                                    Creating the Dialog Box to show the User's Emotion to User
                                    AlertDialog.Builder builder
                                            = new AlertDialog
                                            .Builder(DemoActivity.this)
                                            .setTitle(emotionValue + " Emotion detected")
                                            .setMessage("Do you want to Continue?")
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                     TODO: 23-03-2021 When the Yes button is clicked then play the correct emotion song First call the
//                                                      Search Api to get the Id of songs
//                                                      then call the audio_feature api using that Id to generate the song Emotion
//                                                    for (Songs song : songList) {
////                                                        Log.e("After dialog", "Song title: " + song.getSongTitle());
//                                                        String Spotify_songId = getSpotifySongId(song.getSongTitle(), song.getArtist());
////                                                        Log.e("After Getting ID ", "SongID: " + Spotify_songId);
//                                                    }

                                                    if (emotionValue.equals("neutral")) {
                                                        //Open neutral Fragment
//                                                        Intent mainIntent = new Intent(DemoActivity.this, MainActivity.class);
//                                                        mainIntent.putExtra("emotion", "neutral");
//                                                        startActivity(mainIntent);
//                                                        finish();
                                                        playRandomSong(emotionValue);

                                                    } else if (emotionValue.equals("happiness")) {
                                                        //Open Happy Fragment
//                                                        Intent mainIntent = new Intent(DemoActivity.this, MainActivity.class);
//                                                        mainIntent.putExtra("emotion", "happiness");
//                                                        startActivity(mainIntent);
//                                                        finish();
                                                        playRandomSong(emotionValue);

                                                    } else if (emotionValue.equals("sadness")) {
//                                                        Intent mainIntent = new Intent(DemoActivity.this, MainActivity.class);
//                                                        mainIntent.putExtra("emotion", "sadness");
//                                                        startActivity(mainIntent);
//                                                        finish();
                                                        playRandomSong(emotionValue);
                                                    }
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //cancel
                                                    dialogInterface.cancel();
                                                    Intent refresh = new Intent(DemoActivity.this, ButtonsActivity.class);
                                                    startActivity(refresh);//Start the same Activity
                                                    finish();
                                                }
                                            });

                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();


                                }
                            });

                            if (result == null) {
                                publishProgress(
                                        "Detection Finished. Nothing detected");
                                return null;
                            }
                            Log.e("TAG", "doInBackground: " + "   " + result.length);
                            publishProgress(String.format(
                                    "Detection Finished. %d face(s) detected",
                                    result.length));

                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {

                        detectionProgressDialog.show();
                    }

                    @Override
                    protected void onProgressUpdate(String... progress) {

                        detectionProgressDialog.setMessage(progress[0]);
                    }

                    @Override
                    protected void onPostExecute(Face[] result) {
                        detectionProgressDialog.dismiss();

                        facesDetected = result;

                        if (!exceptionMessage.equals("")) {
                            if (facesDetected == null) {
//                                showError(exceptionMessage + "\nNo faces detected.");
                                Toast.makeText(DemoActivity.this, "No faces detected", Toast.LENGTH_SHORT).show();
                            } else {
//                                showError(exceptionMessage);
                                Toast.makeText(DemoActivity.this, exceptionMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (result == null) {
                            if (facesDetected == null) {
//                                showError("No faces detected");
                                Toast.makeText(DemoActivity.this, "No faces detected", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Log.e("TAG", "onPostExecute: " + facesDetected);

                        ImageView imageView = findViewById(R.id.imageView);
                        imageView.setImageBitmap(
                                drawFaceRectanglesOnBitmap(imageBitmap, result));
                        imageBitmap.recycle();
                        takePicture = true;
                    }
                };

        detectTask.execute(inputStream);
    }

    private void playRandomSong(String emotion) {

        Intent MainIntent = new Intent(DemoActivity.this, MainActivity.class);
        MainIntent.putExtra("RandomSong", "playRandom");
        Toast.makeText(this, "Starting " + emotion + " Song", Toast.LENGTH_SHORT).show();
        startActivity(MainIntent);
        finish();
    }

    //** Spotify Search API
    private String getSpotifySongId(String songTitle, final String songartist) {
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Please Wait...");

//        final ArrayList<String> spotify_songsId = null;

        String ENDPOINT = "https://api.spotify.com/v1/search?q=" + songTitle + "&type=track";
        final String[] song_Spotify_Id = new String[1];
        // TODO: 24-03-2021 Implement a loader and filter the name of songs
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, ENDPOINT, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();
                        try {
//                            Log.e("Getting ", "Response: " + response.toString());
                            JSONObject tracks = response.getJSONObject("tracks");
                            JSONArray items = tracks.getJSONArray("items");
//                            Log.e("items: ", "items Object: " + items);
                            //loop through items
                            String Song_ID = "";
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject itemObj = items.getJSONObject(i);
                                JSONArray artists = itemObj.getJSONArray("artists");
//                                Log.e("artists: ", "artists Array: " + artists);
                                JSONObject artistInner = artists.getJSONObject(0);
                                String artist_name = artistInner.getString("name");

                                if (artist_name.equals(songartist)) {
                                    //if artist matched then fetch id of song
                                    Song_ID = itemObj.getString("id");
                                    break;
                                } else {
                                    // fetch id of first song by default
                                    Song_ID = items.getJSONObject(0).getString("id");
                                    break;
                                }

                            }
                            song_Spotify_Id[0] = Song_ID;
                            Log.e("Inside Response: ", "song_Spotify_Id[0] : " + song_Spotify_Id[0]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(DemoActivity.this, "That didn't work", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = msharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);

        Log.e("Inside getSpotifySongId", "getSpotifySongId SongID:  " + song_Spotify_Id[0]);
        return song_Spotify_Id[0];
    }


    //    For Drawing the Frame Around the face
    private static Bitmap drawFaceRectanglesOnBitmap(
            Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(9);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }
}