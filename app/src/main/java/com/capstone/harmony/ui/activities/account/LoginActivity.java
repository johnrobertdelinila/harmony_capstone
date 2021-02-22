package com.capstone.harmony.ui.activities.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.capstone.harmony.R;
import com.capstone.harmony.ui.activities.MainActivity;
import com.capstone.harmony.utils.AnimationUtil;
import com.capstone.harmony.utils.Config;
import com.capstone.harmony.utils.database.UserHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.marcoscg.dialogsheet.DialogSheet;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    public static Activity activity;
    private EditText email,password;
    private Button login,register;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private UserHelper userHelper;
    private ProgressDialog mDialog;

    public static void startActivityy(Context context) {
        Intent intent=new Intent(context,LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
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

        setContentView(R.layout.activity_login);

        activity = this;
        mAuth=FirebaseAuth.getInstance();
        mFirestore=FirebaseFirestore.getInstance();
        userHelper = new UserHelper(this);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        email=findViewById(R.id.email);
        password=findViewById(R.id.password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Fade fade = new Fade();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fade.excludeTarget(findViewById(R.id.layout), true);
                fade.excludeTarget(android.R.id.statusBarBackground, true);
                fade.excludeTarget(android.R.id.navigationBarBackground, true);
                getWindow().setEnterTransition(fade);
                getWindow().setExitTransition(fade);
            }
        }

    }


    public void performLogin() {

        String email_ = email.getText().toString();
        String pass_ = password.getText().toString();

        if (TextUtils.isEmpty(email_)) {
            Toasty.warning(LoginActivity.this, "Email must not be empty.", Toasty.LENGTH_SHORT).show();
            AnimationUtil.shakeView(email, this);
            return;
        }

        if (!isEmailValid(email.getText())) {
            Toasty.warning(LoginActivity.this, "The email is not valid.", Toasty.LENGTH_SHORT).show();
            AnimationUtil.shakeView(email, this);
            return;
        }

        if (TextUtils.isEmpty(pass_)) {
            Toasty.warning(LoginActivity.this, "You forgot to enter your password.", Toasty.LENGTH_SHORT).show();
            AnimationUtil.shakeView(password, this);
            return;
        }

        if (pass_.length() < 6) {
            Toasty.warning(LoginActivity.this, "The password should at least 6 characters.", Toasty.LENGTH_SHORT).show();
            AnimationUtil.shakeView(password, this);
            return;
        }

        mDialog.show();

        mAuth.signInWithEmailAndPassword(email_, pass_).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Log.i(TAG, "Login Successful, continue to email verified");

                if (email_.equalsIgnoreCase(getString(R.string.admin_email)) || task.getResult().getUser().isEmailVerified()) {

                    Log.i(TAG, "Email is verified Successful, continue to get token");

                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(taskInstanceToken -> {
                        if (!taskInstanceToken.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", taskInstanceToken.getException());
                            if (taskInstanceToken.getException() != null) {
                                Toast.makeText(activity, taskInstanceToken.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        // TODO Get new Instance ID token
                        String token_id = taskInstanceToken.getResult().getToken();

                        Log.i(TAG, "Get Token Listener, Token ID (token_id): " + token_id);

                        final String current_id = task.getResult().getUser().getUid();


                        mFirestore.collection("Users").document(current_id).get().addOnSuccessListener(documentSnapshot -> {
                            // TODO How to update only one field that is list of string.
                            //https://firebase.google.com/docs/firestore/manage-data/add-data#update-data

                            final Map<String, Object> tokenMap = new HashMap<>();
                            tokenMap.put("token_ids", FieldValue.arrayUnion(token_id));

                            mFirestore.collection("Users")
                                .document(current_id)
                                .update(tokenMap)
                                .addOnSuccessListener(aVoid -> FirebaseFirestore.getInstance().collection("Users").document(current_id).get().addOnSuccessListener(documentSnapshot1 -> {

                                        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("regId", token_id);
                                        editor.apply();

                                        String username = documentSnapshot1.getString("username");
                                        String name = documentSnapshot1.getString("name");
                                        String email = documentSnapshot1.getString("email");
                                        String image = documentSnapshot1.getString("image");
                                        String password = pass_;
                                        String location = documentSnapshot1.getString("location");
                                        String bio = documentSnapshot1.getString("bio");

                                        userHelper.insertContact(username, name, email, image, password, location, bio);

                                        mDialog.dismiss();
                                        MainActivity.startActivity(LoginActivity.this);
                                        finish();

                                    }).addOnFailureListener(e -> {
                                        Log.e("Error", ".." + e.getMessage());
                                        mDialog.dismiss();
                                })).addOnFailureListener(e -> {
                                    mDialog.dismiss();
                                    Toasty.error(LoginActivity.this, "Error: " + e.getMessage(), Toasty.LENGTH_SHORT,true).show();
                            });

                        });

                    });

                } else{

                    mDialog.dismiss();
                    new DialogSheet(LoginActivity.this)
                            .setTitle("Information")
                            .setCancelable(true)
                            .setRoundedCorners(true)
                            .setColoredNavigationBar(true)
                            .setMessage("Email has not been verified, please verify and continue.")
                            .setPositiveButton("Send again", v -> task.getResult()
                                    .getUser()
                                    .sendEmailVerification()
                                    .addOnSuccessListener(aVoid -> Toasty.success(LoginActivity.this, "Verification email has been sent your email address", Toasty.LENGTH_LONG,true).show())
                                    .addOnFailureListener(e -> Log.e("Error",e.getMessage())))
                            .setNegativeButton("Ok", v -> {})
                            .show();

                    if (mAuth.getCurrentUser() != null) {
                        mAuth.signOut();
                    }

                }

            } else {
                if (task.getException().getMessage().contains("The password is invalid")) {
                    Toasty.error(LoginActivity.this, "The password you have entered is invalid.", Toasty.LENGTH_LONG,true).show();
                } else if (task.getException().getMessage().contains("There is no user record")) {
                    Toasty.error(LoginActivity.this, "Invalid user, please register using the button below.", Toasty.LENGTH_LONG,true).show();
                } else {
                    Toasty.error(LoginActivity.this, "Error: " + task.getException().getMessage(), Toasty.LENGTH_LONG,true).show();
                }
                mDialog.dismiss();
            }
        });

    }


    public void onLogin(View view) {
        performLogin();
    }

    public void onRegister(View view) {
        RegisterActivity.startActivity(this);
    }

    public void onForgotPassword(View view) {

        if(TextUtils.isEmpty(email.getText().toString())) {
            Toasty.info(activity, "Enter your email to send reset password mail.", Toasty.LENGTH_SHORT,true).show();
            AnimationUtil.shakeView(email, this);
            return;
        }

        if (!isEmailValid(email.getText())) {
            Toasty.info(activity, "The email that you entered is invalid.", Toasty.LENGTH_SHORT,true).show();
            AnimationUtil.shakeView(email, this);
            return;
        }

        mDialog.show();

        FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                .addOnSuccessListener(aVoid -> {
                    mDialog.dismiss();
                    Toasty.success(LoginActivity.this, "Reset password mail sent", Toasty.LENGTH_SHORT,true).show();
                })
                .addOnFailureListener(e -> {
                    mDialog.dismiss();
                    Toasty.error(LoginActivity.this, "Error sending mail : "+e.getLocalizedMessage(), Toasty.LENGTH_SHORT,true).show();
                });
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
