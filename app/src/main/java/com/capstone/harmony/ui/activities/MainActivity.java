package com.capstone.harmony.ui.activities;

import android.Manifest;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.capstone.harmony.R;
import com.capstone.harmony.adapters.DrawerAdapter;
import com.capstone.harmony.models.DrawerItem;
import com.capstone.harmony.models.Images;
import com.capstone.harmony.models.SimpleItem;
import com.capstone.harmony.ui.activities.account.LoginActivity;
import com.capstone.harmony.ui.activities.account.UpdateAvailable;
import com.capstone.harmony.ui.activities.forum.AddQuestion;
import com.capstone.harmony.ui.activities.forum.AnswersActivity;
import com.capstone.harmony.ui.activities.friends.FriendProfile;
import com.capstone.harmony.ui.activities.notification.NotificationActivity;
import com.capstone.harmony.ui.activities.notification.NotificationImage;
import com.capstone.harmony.ui.activities.notification.NotificationImageReply;
import com.capstone.harmony.ui.activities.notification.NotificationReplyActivity;
import com.capstone.harmony.ui.activities.notification.Notifications;
import com.capstone.harmony.ui.activities.post.CommentsActivity;
import com.capstone.harmony.ui.activities.post.PostImage;
import com.capstone.harmony.ui.activities.post.PostText;
import com.capstone.harmony.ui.activities.post.SinglePostView;
import com.capstone.harmony.ui.fragment.About;
import com.capstone.harmony.ui.fragment.Dashboard;
import com.capstone.harmony.ui.fragment.FlashMessage;
import com.capstone.harmony.ui.fragment.Forum;
import com.capstone.harmony.ui.fragment.FriendsFragment;
import com.capstone.harmony.ui.fragment.Home;
import com.capstone.harmony.ui.fragment.ProfileFragment;
import com.capstone.harmony.utils.Config;
import com.capstone.harmony.utils.NetworkUtil;
import com.capstone.harmony.utils.NotificationUtil;
import com.capstone.harmony.utils.database.NotificationsHelper;
import com.capstone.harmony.utils.database.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.marcoscg.dialogsheet.DialogSheet;
import com.tapadoo.alerter.Alerter;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {

    private static final int POS_DASHBOARD = 0;
    private static final int POS_FORUM = 1;
    private static final int POS_SEND_MESSAGE = 2;
    private static final int POS_FRIENDS = 3;
    private static final int POS_ABOUT = 4;
    private static final int POS_LOGOUT = 6;
    public static String userId;
    public static MainActivity activity;
    DrawerAdapter adapter;
    View sheetView;
    private String[] screenTitles;
    private Drawable[] screenIcons;
    private SlidingRootNav slidingRootNav;
    private FirebaseAuth mAuth;
    public static FirebaseUser currentuser;
    private FirebaseFirestore firestore;
    private UserHelper userHelper;
    private StorageReference storageReference;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Intent resultIntent;
    public static CircleImageView imageView;
    public static TextView username, tCommunity;
    private AuthCredential credential;
    public static Fragment mCurrentFragment;
    public BroadcastReceiver NetworkChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            Log.i("Network reciever", "OnReceive");
            if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                if (status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    updateToken();
                    performUploadTask();
                    try {
                        Snackbar.make(findViewById(R.id.activity_main), "Syncing...", Snackbar.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        Snackbar.make(findViewById(R.id.activity_main), "No Internet Connection...", Snackbar.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    };
    private BottomSheetDialog mBottomSheetDialog;
    public static Toolbar toolbar;
    private boolean mState=true;
    private boolean mStateForum=false;
    private MenuItem add_question;
    private List<Images> imagesList=new ArrayList<>();
    private boolean validate;
    public static MenuItem add_post;

    public static void startActivity(Context context) {
        Intent intent=new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }

    public static void startActivity(Context context,boolean validate) {
        Intent intent=new Intent(context,MainActivity.class).putExtra("validate",validate);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        slidingRootNav.closeMenu(true);
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        if(!TextUtils.isEmpty(getIntent().getStringExtra("openFragment"))){

            if(getIntent().getStringExtra("openFragment").equals("forLike")) {
                startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",false));
            }else{
                startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",true));
            }
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSharedPreferences("fcm_activity",MODE_PRIVATE).edit().putBoolean("active",false).apply();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseFirestore.getInstance().collection("Users").document(mAuth.getCurrentUser().getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.getResult() != null) {
                        Boolean isDisabled = task.getResult().getBoolean("isDisabled");
                        if (isDisabled != null && isDisabled) {
                            logout();
                        }
                    }
                });

        getSharedPreferences("fcm_activity",MODE_PRIVATE).edit().putBoolean("active",true).apply();

        username = findViewById(R.id.username);
        imageView = findViewById(R.id.profile_image);

        validate=getIntent().getBooleanExtra("validate",false);

        if(currentuser!=null)
        {
            try {
                performUploadTask();
            }catch (Exception e){
                Log.e("Error","."+e.getLocalizedMessage());
            }

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.REGISTRATION_COMPLETE));

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.PUSH_NOTIFICATION));


        }else{
            LoginActivity.startActivityy(this);
            finish();
        }
    }

    @Override
    public void onBackPressed() {

        if (!mState) {

            toolbar.setTitle("Dashboard");
            try {
                getSupportActionBar().setTitle("Dashboard");
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            this.invalidateOptionsMenu();
            mState = true;
            showFragment(new Dashboard());
            if (slidingRootNav.isMenuOpened()) {
                slidingRootNav.closeMenu(true);
            }
            adapter.setSelected(POS_DASHBOARD);

        } else if (slidingRootNav.isMenuOpened()) {
            slidingRootNav.closeMenu(true);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());


        setContentView(R.layout.activity_main);
        activity=this;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Dashboard");
        try {
            getSupportActionBar().setTitle("Dashboard");
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }

        userHelper = new UserHelper(this);
        firestore = FirebaseFirestore.getInstance();

        registerReceiver(NetworkChangeReceiver
                , new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Config.createNotificationChannels(this);
        }

        if (currentuser == null) {

            LoginActivity.startActivityy(this);
            finish();

        } else {

            mCurrentFragment = new Dashboard();
            firebaseMessagingService();
            askPermission();

            updateToken();

            userId = currentuser.getUid();
            storageReference = FirebaseStorage.getInstance().getReference().child("images").child(currentuser.getUid() + ".jpg");

            slidingRootNav = new SlidingRootNavBuilder(this)
                    .withToolbarMenuToggle(toolbar)
                    .withMenuOpened(false)
                    .withContentClickableWhenMenuOpened(false)
                    .withSavedState(savedInstanceState)
                    .withMenuLayout(R.layout.activity_main_drawer)
                    .inject();

            screenIcons = loadScreenIcons();
            screenTitles = loadScreenTitles();

            if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
                adapter = new DrawerAdapter(Arrays.asList(
                        createItemFor(POS_DASHBOARD).setChecked(true),
                        createItemFor(POS_FORUM),
                        createItemFor(POS_FRIENDS),
                        createItemFor(POS_ABOUT),
                        new SpaceItem(48),
                        createItemFor(POS_LOGOUT)));
                adapter.setListener(this);
            }else {
                adapter = new DrawerAdapter(Arrays.asList(
                        createItemFor(POS_DASHBOARD).setChecked(true),
                        createItemFor(POS_FORUM),
                        createItemFor(POS_SEND_MESSAGE),
                        createItemFor(POS_FRIENDS),
                        createItemFor(POS_ABOUT),
                        new SpaceItem(48),
                        createItemFor(POS_LOGOUT)));
                adapter.setListener(this);
            }

            RecyclerView list = findViewById(R.id.list);
            list.setNestedScrollingEnabled(false);
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(adapter);

            adapter.setSelected(POS_DASHBOARD);
            setUserProfile();

            mBottomSheetDialog = new BottomSheetDialog(this);
            sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);
            mBottomSheetDialog.setContentView(sheetView);

            LinearLayout text_post = sheetView.findViewById(R.id.text_post);
            LinearLayout photo_post = sheetView.findViewById(R.id.image_post);

            tCommunity = sheetView.findViewById(R.id.current_community);

            text_post.setOnClickListener(view -> {
                if (Home.currentCommunity == null) {
                    Toasty.warning(MainActivity.this, "Select a community first to post.", Toasty.LENGTH_LONG, true).show();
                    return;
                }
                mBottomSheetDialog.dismiss();
                PostText.startActivity(MainActivity.this);
            });

            photo_post.setOnClickListener(view -> {
                if (Home.currentCommunity == null) {
                    Toasty.warning(MainActivity.this, "Select a community first to post.", Toasty.LENGTH_LONG, true).show();
                    return;
                }
                mBottomSheetDialog.dismiss();
                startPickImage();
            });

            if(!TextUtils.isEmpty(getIntent().getStringExtra("openFragment"))){
                if(getIntent().getStringExtra("openFragment").equals("forLike")) {
                    startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",false));
                }else{
                    startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",true));
                }
            }
        }
    }

    private void updateToken() {
        final String token_id = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE).getString("regId","");
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("token_ids", FieldValue.arrayUnion(token_id));

        if(isOnline()) {

            firestore.collection("Users").document(currentuser.getUid()).update(tokenMap)
                    .addOnSuccessListener(aVoid -> Log.d("TOKEN", token_id))
                    .addOnFailureListener(e -> Log.d("Error Token", e.getMessage()));

        }
    }

    private void askPermission() {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                         if(report.isAnyPermissionPermanentlyDenied()){
                            Toasty.info(MainActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toasty.LENGTH_LONG,true).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();

    }

    private void setUserProfile() {

        Cursor rs = userHelper.getData(1);
        if (rs.moveToFirst()) {
            String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
            String imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));

            if (!rs.isClosed()) {
                rs.close();
            }

            username = findViewById(R.id.username);
            imageView = findViewById(R.id.profile_image);
            username.setText(nam);
            Glide.with(this)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                    .load(imag)
                    .into(imageView);
        }else {
            userHelper.deleteContact(1);
            Toasty.error(MainActivity.this,"Authentication revoked",Toasty.LENGTH_SHORT,true).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    public void showDialog(){

        new DialogSheet(this)
                .setTitle("Information")
                .setMessage("Email has not been verified, please verify and continue. If you have verified we recommend you to logout and login again")
                .setPositiveButton("Send again", v -> mAuth.getCurrentUser().sendEmailVerification()
                        .addOnSuccessListener(aVoid -> Toasty.success(MainActivity.this, "Verification email sent", Toasty.LENGTH_SHORT,true).show())
                        .addOnFailureListener(e -> Log.e("Error", e.getMessage())))
                .setNegativeButton("Ok", v -> {

                })
                .setCancelable(true)
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .show();

    }

    @Override
    public void onItemSelected(int position) {

        Fragment selectedScreen;
        switch (position) {
            case POS_DASHBOARD:
                toolbar.setTitle("Dashboard");
                try {
                    getSupportActionBar().setTitle("Dashboard");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=true;
                mStateForum=false;
                selectedScreen = new Dashboard();
                showFragment(selectedScreen);

                slidingRootNav.closeMenu(true);

                return;

            case POS_FORUM:
                toolbar.setTitle("Forum");
                try {
                    getSupportActionBar().setTitle("Forum");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=false;
                mStateForum=true;
                selectedScreen = new Forum();
                showFragment(selectedScreen);

                slidingRootNav.closeMenu(true);

                return;

            case POS_SEND_MESSAGE:

                if(currentuser.getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
                    String friendTitle = "Manage Users";
                    toolbar.setTitle(friendTitle);
                    try {
                        final String finalTitle = friendTitle;
                        getSupportActionBar().setTitle(finalTitle);
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
                    selectedScreen = new FriendsFragment();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                }else if (currentuser.isEmailVerified()) {
                    toolbar.setTitle("Flash Messages");
                    try {
                        getSupportActionBar().setTitle("Flash Messages");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
                    selectedScreen = new FlashMessage();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                }
                else{
                    showDialog();
                }

                return;

            case POS_FRIENDS:

                if(currentuser.getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
                    toolbar.setTitle("About");
                    try {
                        getSupportActionBar().setTitle("About");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
                    selectedScreen = new About();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                }else if (currentuser.isEmailVerified()) {
                    String friendTitle = "Manage Friends";
                    if (currentuser.getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
                        friendTitle = "Manage Users";
                    }
                    toolbar.setTitle(friendTitle);
                    try {
                        final String finalTitle = friendTitle;
                        getSupportActionBar().setTitle(finalTitle);
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
                    selectedScreen = new FriendsFragment();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                } else{
                    showDialog();
                }
                return;

            case POS_ABOUT:

                if(currentuser.getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
                    if (currentuser != null && isOnline()) {

                        new MaterialDialog.Builder(this)
                                .title("Logout")
                                .content("Are you sure do you want to logout from this account?")
                                .positiveText("Yes")
                                .onPositive((dialog, which) -> {
                                    logout();
                                    dialog.dismiss();
                                }).negativeText("No")
                                .onNegative((dialog, which) -> dialog.dismiss()).show();

                    } else {

                        new MaterialDialog.Builder(this)
                                .title("Logout")
                                .content("A technical occurred while logging you out, Check your network connection and try again.")
                                .positiveText("Done")
                                .onPositive((dialog, which) -> dialog.dismiss()).show();

                    }
                }else if (currentuser.isEmailVerified()) {
                    toolbar.setTitle("About");
                    try {
                        getSupportActionBar().setTitle("About");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
                    selectedScreen = new About();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                } else{
                    showDialog();
                }
                return;


            case POS_LOGOUT:

                if (currentuser != null && isOnline()) {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("Are you sure do you want to logout from this account?")
                            .positiveText("Yes")
                            .onPositive((dialog, which) -> {
                                logout();
                                dialog.dismiss();
                            }).negativeText("No")
                            .onNegative((dialog, which) -> dialog.dismiss()).show();

                } else {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("A technical occurred while logging you out, Check your network connection and try again.")
                            .positiveText("Done")
                            .onPositive((dialog, which) -> dialog.dismiss()).show();

                }

                return;

            default:
                if(currentuser.getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
                    if (currentuser != null && isOnline()) {

                        new MaterialDialog.Builder(this)
                                .title("Logout")
                                .content("Are you sure do you want to logout from this account?")
                                .positiveText("Yes")
                                .onPositive((dialog, which) -> {
                                    logout();
                                    dialog.dismiss();
                                }).negativeText("No")
                                .onNegative((dialog, which) -> dialog.dismiss()).show();

                    } else {

                        new MaterialDialog.Builder(this)
                                .title("Logout")
                                .content("A technical occurred while logging you out, Check your network connection and try again.")
                                .positiveText("Done")
                                .onPositive((dialog, which) -> dialog.dismiss()).show();

                    }
                }else {
                    selectedScreen = new Dashboard();
                    showFragment(selectedScreen);
                }
        }

        slidingRootNav.closeMenu(true);

    }

    public void logout() {
        performUploadTask();
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage("Logging you out...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE);

        Map<String, Object> tokenRemove = new HashMap<>();
        tokenRemove.put("token_ids", FieldValue.arrayRemove(pref.getString("regId","")));

        firestore.collection("Users").document(userId).update(tokenRemove).addOnSuccessListener(aVoid -> {
            deleteAllnotifications();
            userHelper.deleteContact(1);
            mAuth.signOut();
            LoginActivity.startActivityy(MainActivity.this);
            mDialog.dismiss();
            finish();
        }).addOnFailureListener(e -> {
            Toasty.error(MainActivity.this, "Error logging out", Toasty.LENGTH_SHORT,true).show();
            mDialog.dismiss();
            Log.e("Logout Error", e.getMessage());
        });

    }

    private void deleteAllnotifications() {

        NotificationsHelper notificationsHelper=new NotificationsHelper(this);
        notificationsHelper.deleteAll();

    }

    public static void showFragment(Fragment fragment) {
        activity.getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        mCurrentFragment=fragment;
    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
            .withIconTint(color(R.color.minimal_black))
            .withTextTint(color(R.color.minimal_black))
            .withSelectedIconTint(color(R.color.colorAccentt))
            .withSelectedTextTint(color(R.color.colorAccentt));
    }

    @NonNull
    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    private void firebaseMessagingService() {

        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("OnBroadcastReceiver", "received");

               if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    Log.i("OnBroadcastReceiver", "push_received");

                    String click_action = intent.getStringExtra("click_action");

                    switch (click_action) {
                        case "com.amsavarthan.hify.TARGETNOTIFICATION":

                            resultIntent = new Intent(MainActivity.this, NotificationActivity.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY":

                            resultIntent = new Intent(MainActivity.this, NotificationReplyActivity.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.amsavarthan.hify.TARGETNOTIFICATION_IMAGE":

                            resultIntent = new Intent(MainActivity.this, NotificationImage.class);

                            showAlert(resultIntent, intent);


                            break;
                        case "com.amsavarthan.hify.TARGETNOTIFICATIONREPLY_IMAGE":

                            resultIntent = new Intent(MainActivity.this, NotificationImageReply.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.amsavarthan.hify.TARGET_FRIENDREQUEST":
                        case "REPORT USER":

                            resultIntent = new Intent(MainActivity.this, FriendProfile.class);

                            showAlert(resultIntent, intent);


                            break;
                        case "com.amsavarthan.hify.TARGET_COMMENT":

                            resultIntent = new Intent(MainActivity.this, CommentsActivity.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.amsavarthan.hify.TARGET_UPDATE":

                            resultIntent = new Intent(MainActivity.this, UpdateAvailable.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.amsavarthan.hify.TARGET_LIKE":
                        case "REPORT POST":

                            resultIntent = new Intent(MainActivity.this, SinglePostView.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.amsavarthan.hify.TARGET_FORUM":

                            resultIntent = new Intent(MainActivity.this, AnswersActivity.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "SHOW COMMUNITY":
                            resultIntent = new Intent(MainActivity.this, ApproveActivity.class);
                            showAlert(resultIntent,intent);
                            break;
                        default:

                            resultIntent = null;
                            break;
                    }

                }
            }
        };
    }

    private void showAlert(final Intent resultIntent, Intent intent) {

        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        String name = intent.getStringExtra("name");
        String from_image = intent.getStringExtra("from_image");
        String message = intent.getStringExtra("message");
        String from_id = intent.getStringExtra("from_id");
        String timestamp = intent.getStringExtra("timestamp");
        String reply_for = intent.getStringExtra("reply_for");
        String image = intent.getStringExtra("image");
        String reply_image = intent.getStringExtra("reply_image");

        String f_id = intent.getStringExtra("f_id");
        String f_name = intent.getStringExtra("f_name");
        String f_email = intent.getStringExtra("f_email");
        String f_token = intent.getStringExtra("f_token");
        String f_image = intent.getStringExtra("f_image");

        String user_id=intent.getStringExtra("user_id");
        String post_id=intent.getStringExtra("post_id");
        String post_desc=intent.getStringExtra("post_desc");
        String admin_id=intent.getStringExtra("admin_id");

        String channel=intent.getStringExtra("channel");
        String version=intent.getStringExtra("version");
        String improvements=intent.getStringExtra("improvements");
        String link=intent.getStringExtra("link");
        String question_id=intent.getStringExtra("question_id");
        String question_timestamp=intent.getStringExtra("question_timestamp");
        String notification_type=intent.getStringExtra("notification_type");

        String community = intent.getStringExtra("community");

        resultIntent.putExtra("community", community);

        resultIntent.putExtra("title", title);
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("from_image", from_image);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("from_id", from_id);
        resultIntent.putExtra("timestamp", timestamp);
        resultIntent.putExtra("reply_for", reply_for);
        resultIntent.putExtra("image", image);
        resultIntent.putExtra("reply_image", reply_image);

        resultIntent.putExtra("f_id", f_id);
        resultIntent.putExtra("f_name", f_name);
        resultIntent.putExtra("f_email", f_email);
        resultIntent.putExtra("f_image", f_image);
        resultIntent.putExtra("f_token", f_token);

        resultIntent.putExtra("user_id", user_id);
        resultIntent.putExtra("post_id", post_id);
        resultIntent.putExtra("post_desc", post_desc);
        resultIntent.putExtra("admin_id", admin_id);

        resultIntent.putExtra("channel", channel);
        resultIntent.putExtra("version", version);
        resultIntent.putExtra("improvements", improvements);
        resultIntent.putExtra("link", link);
        resultIntent.putExtra("question_id", question_id);
        resultIntent.putExtra("question_timestamp", question_timestamp);
        resultIntent.putExtra("notification_type", notification_type);

        Alerter.create(MainActivity.this)
                .setTitle(title)
                .setText(body)
                .enableSwipeToDismiss()
                .setDuration(4000)//4sec
                .enableVibration(true)
                .setBackgroundColorRes(R.color.colorAccentt)
                .setProgressColorRes(R.color.colorPrimaryy)
                .setTitleAppearance(R.style.AlertTextAppearance_Title)
                .setTextAppearance(R.style.AlertTextAppearance_Text)
                .setOnClickListener(view -> startActivity(resultIntent)).show();


    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onPause();
    }

    public void performUploadTask(){

        if(isOnline()){

            Cursor rc =userHelper.getData(1);
            rc.moveToFirst();

            final String nam = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
            final String emai = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
            final String imag = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
            final String password = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_PASS));
            final String usernam = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_USERNAME));
            final String loc = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_LOCATION));
            final String bi = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_BIO));

            if(!rc.isClosed()){
                rc.close();
            }

            if(!validate){

                FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnSuccessListener(documentSnapshot -> {

                    String name = documentSnapshot.getString("name");
                    String image = documentSnapshot.getString("image");
                    final String email = documentSnapshot.getString("email");
                    String bio = documentSnapshot.getString("bio");
                    String usrname = documentSnapshot.getString("username");
                    String location = documentSnapshot.getString("location");

                    username.setText(name);
                    Glide.with(MainActivity.this)
                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                            .load(image)
                            .into(imageView);


                    if (!image.equals(imag)) {
                        storageReference.putFile(Uri.parse(imag)).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                storageReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("image", downloadUri.toString());

                                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> {
                                        userHelper.updateContactImage(1, downloadUri.toString());
                                        Glide.with(MainActivity.this)
                                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                                .load(downloadUri)
                                                .into(imageView);

                                    });
                                });

                            }
                        });
                    }

                    if (!bio.equals(bi)) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("bio", bi);

                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> userHelper.updateContactBio(1, bi));
                    }

                    if (!location.equals(loc)) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("location", loc);

                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> userHelper.updateContactLocation(1, loc));
                    }

                    if (!name.equals(nam)) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", nam);

                        FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> {
                            userHelper.updateContactName(1, nam);
                            username.setText(nam);

                        });
                    }

                    if (!currentuser.getEmail().equals(emai)) {


                        credential = EmailAuthProvider
                                .getCredential(currentuser.getEmail(), password);

                        currentuser.reauthenticate(credential)
                                .addOnCompleteListener(task -> currentuser.updateEmail(emai).addOnCompleteListener(task1 -> {

                                    if (task1.isSuccessful()) {

                                        if (!email.equals(emai)) {
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("email", emai);

                                            FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    userHelper.updateContactEmail(1, emai);
                                                }

                                            });
                                        }

                                    } else {

                                        Log.e("Update email error", task1.getException().getMessage() + "..");

                                    }

                                }));
                    }
                });

            }else {

                FirebaseAuth.getInstance().signOut();
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emai, password)
                        .addOnFailureListener(e -> {
                            userHelper.deleteContact(1);
                            Toasty.error(MainActivity.this, "Authentication revoked", Toasty.LENGTH_SHORT, true).show();
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        })
                        .addOnSuccessListener(authResult -> FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                String name = documentSnapshot.getString("name");
                                String image = documentSnapshot.getString("image");
                                final String email = documentSnapshot.getString("email");
                                String bio = documentSnapshot.getString("bio");
                                String usrname = documentSnapshot.getString("username");
                                String location = documentSnapshot.getString("location");

                                username.setText(name);
                                Glide.with(MainActivity.this)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(image)
                                        .into(imageView);


                                if (!image.equals(imag)) {
                                    storageReference.putFile(Uri.parse(imag)).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {

                                            storageReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put("image", downloadUri.toString());

                                                FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        userHelper.updateContactImage(1, downloadUri.toString());
                                                        Glide.with(MainActivity.this)
                                                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                                                .load(downloadUri)
                                                                .into(imageView);

                                                    }

                                                });
                                            });

                                        }
                                    });
                                }

                                if (!bio.equals(bi)) {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("bio", bi);

                                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> userHelper.updateContactBio(1, bi));
                                }

                                if (!location.equals(loc)) {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("location", loc);

                                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> userHelper.updateContactLocation(1, loc));
                                }

                                if (!name.equals(nam)) {
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("name", nam);

                                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(aVoid -> {
                                        userHelper.updateContactName(1, nam);
                                        username.setText(nam);

                                    });
                                }

                                if (!currentuser.getEmail().equals(emai)) {


                                    credential = EmailAuthProvider
                                            .getCredential(currentuser.getEmail(), password);

                                    currentuser.reauthenticate(credential)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    currentuser.updateEmail(emai).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()) {

                                                                if (!email.equals(emai)) {
                                                                    Map<String, Object> userMap = new HashMap<>();
                                                                    userMap.put("email", emai);

                                                                    FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {

                                                                            userHelper.updateContactEmail(1, emai);
                                                                        }

                                                                    });
                                                                }

                                                            } else {

                                                                Log.e("Update email error", task.getException().getMessage() + "..");

                                                            }

                                                        }
                                                    });

                                                }
                                            });
                                }
                            }
                        }));

            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(NetworkChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onDestroy();

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static MainActivity getApplicationInstance(){
        return activity;
    }

    public void onViewProfileClicked(View view) {

        toolbar.setTitle("My Profile");
        try {
            getSupportActionBar().setTitle("My Profile");
        }catch (Exception e){
            Log.e("Error",e.getMessage());
        }
        this.invalidateOptionsMenu();
        mState=false;
        showFragment(new ProfileFragment());
        slidingRootNav.closeMenu(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_posts, menu);
        add_post=menu.findItem(R.id.action_new);
        add_question=menu.findItem(R.id.action_new_question);

        MenuItem notification=menu.findItem(R.id.action_notifications);
        View badge_action=notification.getActionView();

        if(badge_action!=null){

            CircleImageView badge=badge_action.findViewById(R.id.badge);

            if(!NotificationUtil.read){
                badge.setVisibility(View.VISIBLE);
            }else{
                badge.setVisibility(View.GONE);
            }

        }

        if(mStateForum){
            add_question.setVisible(true);
        }else{
            add_question.setVisible(false);
        }

        if(mState){
            if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
                add_post.setVisible(true);
            }
        }else {
            add_post.setVisible(false);
        }

        if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
            menu.findItem(R.id.action_notifications).setVisible(false);
            add_post.setVisible(false);
            add_question.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Config.PICK_IMAGES ) {
            if (resultCode == RESULT_OK && data != null) {
                imagesList.clear();
                List<Image> pickedImages=ImagePicker.getImages(data);

                for(Image image:pickedImages){
                    imagesList.add(new Images(image.getName(),image.getPath(),image.getPath(),image.getId()));
                }

                new MaterialDialog.Builder(this)
                        .title("Confirmation")
                        .content("Are you sure do you want to continue?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .cancelable(false)
                        .canceledOnTouchOutside(false)
                        .neutralText("Cancel")
                        .onPositive((dialog, which) -> {

                            Intent intent=new Intent(MainActivity.this,PostImage.class);
                            intent.putParcelableArrayListExtra("imagesList",(ArrayList<? extends Parcelable>) imagesList);
                            startActivity(intent);

                        })
                        .onNegative((dialog, which) -> ImagePicker.create(MainActivity.this)
                                .folderMode(true)
                                .toolbarFolderTitle("Select Image(s)")
                                .toolbarImageTitle("Tap to select")
                                .includeVideo(true)
                                .multi()
                                .limit(7)
                                .showCamera(true)
                                .enableLog(true)
                                .imageDirectory("Hify")
                                .start(Config.PICK_IMAGES)).show();

            }
        }

    }

    private void startPickImage() {
        ImagePicker.create(this)
                .folderMode(true)
                .toolbarFolderTitle("Folder")
                .toolbarImageTitle("Tap to select")
                .includeVideo(true)
                .multi()
                .limit(7)
                .showCamera(true)
                .enableLog(true)
                .imageDirectory("Hify")
                .start(Config.PICK_IMAGES);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_new:
                if(currentuser.isEmailVerified()) {
                    if (Home.currentCommunity == null) {
                        Toasty.warning(MainActivity.this, "Select a community first to post.", Toasty.LENGTH_LONG, true).show();
                    }else {
                        tCommunity.setText(Home.currentCommunity);
                        mBottomSheetDialog.show();
                    }
                }else{
                    showDialog();
                }
                return true;

            case R.id.action_notifications:
                startActivity(new Intent(MainActivity.this,Notifications.class));
                return true;
            case R.id.action_new_question:
                startActivity(new Intent(MainActivity.this, AddQuestion.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
