package com.example.currentlocation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.currentlocation.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    String path;
    String nama;
    String lat;
    String lng;
    private String selectedImage;

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }catch (ActivityNotFoundException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//
//            Uri uri = data.getData();
//            Toast.makeText(this, "" + uri, Toast.LENGTH_SHORT).show();
//            Context context = MainActivity.this;
//            path = RealPathUtil.getRealPath(context, uri);
//            Bitmap bitmap = BitmapFactory.decodeFile(path);
//            binding.imagePreview.setImageBitmap(imageBitmap);
//        }
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clickListener();


    }

    private void clickListener() {

        nama = binding.inputNama.getText().toString();
        lat = binding.inputLatitude.getText().toString();
        lng = binding.inputLongitude.getText().toString();


        binding.buttonUploadGambar.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 10);
            }else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        });

        binding.buttonTambahLaporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFileToServer(nama, lat, lng);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            path = RealPathUtil.getRealPath(this, uri);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            selectedImage = FileUtils.getPath(MainActivity.this, data.getData());
            binding.imagePreview.setImageBitmap(bitmap);
            Log.d("MainActivity", "onActivityResult: " + Uri.parse(selectedImage).getPath());
        }
    }

    public void uploadFileToServer(String paramName, String paramLat, String paramLng){

//        File file = new File(Uri.parse(selectedImage).getPath());
        File file = new File(Uri.parse(selectedImage).getPath());

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"),file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("sendimage","pelaporan_"+ System.currentTimeMillis()+".jpg",requestBody);

//        RequestBody nama = RequestBody.create(MediaType.parse("text/plain"),paramName);
//        RequestBody latitude = RequestBody.create(MediaType.parse("text/plain"),paramLat);
//        RequestBody longitude = RequestBody.create(MediaType.parse("text/plain"),paramLng);

        nama = binding.inputNama.getText().toString().trim();
        lat = binding.inputLatitude.getText().toString().trim();
        lng = binding.inputLongitude.getText().toString().trim();

        Log.d("coba", "uploadFileToServer: " + nama + lat + lng);

        DataJsonApi service = RetrofitBuilder.getRetrofit().create(DataJsonApi.class);

        Call<DataModel> call = service.getDataModel(filePart, nama, Double.valueOf(lat), Double.valueOf(lng));
        call.enqueue(new Callback<DataModel>() {
            @Override
            public void onResponse(Call<DataModel> call, Response<DataModel> response) {
                Toast.makeText(MainActivity.this, response.body().getMessage() + nama, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<DataModel> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

    private Uri getImageUri(Context context, Bitmap bitmap) {

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),bitmap, "myImage", "");
        return Uri.parse(path);

    }

    public void addData(String name, String lat, String lng){

//        final OkHttpClient httpClient = new OkHttpClient.Builder()
//                .readTimeout(60, TimeUnit.SECONDS)
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://10.0.2.2/upload_gambar/")
//                .client(httpClient)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();

        File file = new File(path);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"),file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file_gambar", file.getName(), requestFile);

        RequestBody nama = RequestBody.create(MediaType.parse("multipart/form-data"),name);
        RequestBody latitude = RequestBody.create(MediaType.parse("multipart/form-data"),lat);
        RequestBody longitude = RequestBody.create(MediaType.parse("multipart/form-data"),lng);

//        DataJsonApi dataJsonApi = retrofit.create(DataJsonApi.class);

    }
}