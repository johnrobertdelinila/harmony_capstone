package com.capstone.harmony.ui.activities.account;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.capstone.harmony.R;
import com.capstone.harmony.utils.AnimationUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 200;
    public Uri imageUri;
    public StorageReference storageReference;
    public ProgressDialog mDialog;
    public String name_, pass_, email_, username_, location_;
    private EditText name, email, password, location, username;
    private CircleImageView profile_image;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private int count_reference = 0;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, RegisterActivity.class));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private void askPermission() {

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            Toasty.info(RegisterActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toasty.LENGTH_LONG, true).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        setContentView(R.layout.activity_register);

        askPermission();

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child("images");
        firebaseFirestore = FirebaseFirestore.getInstance();
        imageUri = null;

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        location = findViewById(R.id.location);
        username = findViewById(R.id.username);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        Location location1 = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
        Geocoder geocoder=new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;

        try {
            if (location1 != null && geocoder != null) {
                addresses=geocoder.getFromLocation(location1.getLatitude(),location1.getLongitude(),1);
                if(addresses.size()>0){
                    location.setText(addresses.get(0).getLocality());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        Button register = findViewById(R.id.button);

        profile_image=findViewById(R.id.profile_image);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Fade fade = new Fade();
            fade.excludeTarget(findViewById(R.id.layout), true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fade.excludeTarget(android.R.id.statusBarBackground, true);
                fade.excludeTarget(android.R.id.navigationBarBackground, true);
                getWindow().setEnterTransition(fade);
                getWindow().setExitTransition(fade);
            }
        }

        register.setOnClickListener(view -> {

            if (imageUri!=null){

                username_=username.getText().toString();
                name_=name.getText().toString();
                email_=email.getText().toString();
                pass_=password.getText().toString();
                location_=location.getText().toString();

                if (TextUtils.isEmpty(username_) ) {
                    AnimationUtil.shakeView(username, RegisterActivity.this);
                    Toasty.warning(RegisterActivity.this, "Enter your username.", Toasty.LENGTH_SHORT).show();
                }else if(username_.length() < 5){
                    Toasty.warning(getApplicationContext(),"Username should be more than 5 characters",Toasty.LENGTH_SHORT,true).show();
                    AnimationUtil.shakeView(username, RegisterActivity.this);
                }else if(!username_.matches("[a-zA-Z._]*")){
                    Toasty.warning(getApplicationContext(),"No numbers or special character than period and underscore allowed",Toasty.LENGTH_SHORT,true).show();
                    AnimationUtil.shakeView(username, RegisterActivity.this);
                }

                if (TextUtils.isEmpty(name_) && !name_.matches("[a-zA-Z ]*")) {
                    Toasty.warning(getApplicationContext(),"Invalid name",Toasty.LENGTH_SHORT,true).show();
                    AnimationUtil.shakeView(name, RegisterActivity.this);
                }
                if (TextUtils.isEmpty(email_)) {
                    Toasty.warning(RegisterActivity.this, "Enter your email.", Toasty.LENGTH_SHORT).show();
                    AnimationUtil.shakeView(email, RegisterActivity.this);
                }
                if (TextUtils.isEmpty(pass_)) {
                    Toasty.warning(RegisterActivity.this, "Enter your password", Toasty.LENGTH_SHORT).show();
                    AnimationUtil.shakeView(password, RegisterActivity.this);
                }

                if (TextUtils.isEmpty(location_)) {
                    AnimationUtil.shakeView(location, RegisterActivity.this);
                }


                if (!TextUtils.isEmpty(name_) && !TextUtils.isEmpty(email_) &&
                        !TextUtils.isEmpty(pass_) && !TextUtils.isEmpty(username_) && !TextUtils.isEmpty(location_)) {
                    mDialog.show();

                    // Checking if the username already existed
                    firebaseFirestore.collection("Usernames")
                            .document(username_)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if(!documentSnapshot.exists()){
                                    registerUser();
                                }else{
                                    Toasty.error(RegisterActivity.this, "Username already exists", Toasty.LENGTH_SHORT,true).show();
                                    AnimationUtil.shakeView(username, RegisterActivity.this);
                                    mDialog.dismiss();
                                }
                            })
                            .addOnFailureListener(e -> {
                                mDialog.dismiss();
                                Log.e("Error",e.getMessage());
                            });
                }else{
                    AnimationUtil.shakeView(username, RegisterActivity.this);
                    AnimationUtil.shakeView(name, RegisterActivity.this);
                    AnimationUtil.shakeView(email, RegisterActivity.this);
                    AnimationUtil.shakeView(password, RegisterActivity.this);
                    AnimationUtil.shakeView(location, RegisterActivity.this);
                }
            }else{
                AnimationUtil.shakeView(profile_image, RegisterActivity.this);
                Toasty.warning(RegisterActivity.this, "We recommend you to set a profile picture", Toasty.LENGTH_SHORT,true).show();
            }

        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void registerUser() {

        mAuth.createUserWithEmailAndPassword(email_, pass_).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Map<String,Object> usernameMap= new HashMap<>();
                usernameMap.put("username",username_);

                // Adding the new username to the database
                firebaseFirestore.collection("Usernames")
                        .document(username_)
                        .set(usernameMap)
                        .addOnSuccessListener(aVoid -> task.getResult() // Sending email verification to the user's email
                                .getUser()
                                .sendEmailVerification()
                                .addOnSuccessListener(aVoid12 -> {

                                    final String userUid = task.getResult().getUser().getUid();
                                    final StorageReference user_profile = storageReference.child(userUid + ".png");
                                    user_profile.putFile(imageUri).addOnCompleteListener(task1 -> { // Uploading the profile image to storage
                                        if (task1.isSuccessful()) {

                                            user_profile.getDownloadUrl().addOnSuccessListener(uri -> {

                                                // TODO https://firebase.google.com/docs/cloud-messaging/android/client#retrieve-the-current-registration-token.

                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put("id", userUid);
                                                userMap.put("name", name_);
                                                userMap.put("image", uri.toString());
                                                userMap.put("email", email_);
                                                userMap.put("bio",getString(R.string.default_bio));
                                                userMap.put("username", username_);
                                                userMap.put("location", location_);

                                                firebaseFirestore.collection("Users").document(userUid).set(userMap)
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        mDialog.dismiss();
                                                        Toasty.success(RegisterActivity.this, "Successfully registered. Verification email sent", Toasty.LENGTH_LONG,true).show();
                                                        finish();

                                                    }).addOnFailureListener(e -> {
                                                        mDialog.dismiss();
                                                        Toasty.error(RegisterActivity.this, "Error: " + e.getMessage(), Toasty.LENGTH_SHORT,true).show();
                                                    });


                                            }).addOnFailureListener(e -> mDialog.dismiss());
                                        } else {
                                            mDialog.dismiss();
                                        }
                                    });

                                })
                                .addOnFailureListener(e -> task.getResult().getUser().delete()))
                        .addOnFailureListener(e -> Log.e("Error",e.getMessage()));


            } else {
                mDialog.dismiss();
                Toasty.error(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toasty.LENGTH_SHORT,true).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                imageUri=data.getData();
                // start crop activity
                UCrop.Options options = new UCrop.Options();
                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                options.setCompressionQuality(100);
                options.setShowCropGrid(true);


                UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), "user_profile_picture_" + count_reference + ".png")))
                    .withAspectRatio(1, 1)
                    .withOptions(options)
                    .start(this);

            }
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, os);

            String path = MediaStore.Images.Media.insertImage(getContentResolver(), photo, "title", null);
            imageUri = Uri.parse(path);

            // start crop activity
            UCrop.Options options = new UCrop.Options();
            options.setCompressionFormat(Bitmap.CompressFormat.PNG);
            options.setCompressionQuality(100);
            options.setShowCropGrid(true);

            UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), "user_profile_picture_" + count_reference + ".png")))
                    .withAspectRatio(1, 1)
                    .withOptions(options)
                    .start(this);
        }

        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                imageUri = UCrop.getOutput(data);
                if (imageUri != null) {
                    profile_image.setImageURI(imageUri);
                    count_reference++;
                }else {
                    Toast.makeText(this, "Image is null", Toast.LENGTH_SHORT).show();
                }

            } else if (resultCode == UCrop.RESULT_ERROR) {
                Log.e("Error", "Crop error:" + UCrop.getError(data).getMessage());
            }
        }


    }

    public void setProfilepic(View view) {
        new MaterialDialog.Builder(RegisterActivity.this)
                .title("Which type of image")
                .content("Pick from the following")
                .positiveText("Camera")
                .negativeText("Gallery")
                .onPositive((dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                        }
                        else
                        {
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        }
                    }
                })
                .onNegative((dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE);
                })
                .show();
    }

    public void onLogin(View view) {
        onBackPressed();
    }

    public void openPolicy(View view) {
        /*String url = "https://one-tap-manong.flycricket.io/privacy.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);*/
    }

    public void openTerms(View view) {
        /*String url = "https://www.freeprivacypolicy.com/privacy/view/f4382a1af24b862c934d09628a13cd20";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);*/
    }
}
