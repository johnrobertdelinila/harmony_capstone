package com.capstone.harmony.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.capstone.harmony.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.marcoscg.dialogsheet.DialogSheet;

import es.dmoral.toasty.Toasty;

import static com.capstone.harmony.ui.activities.MainActivity.add_post;
import static com.capstone.harmony.ui.activities.MainActivity.showFragment;
import static com.capstone.harmony.ui.activities.MainActivity.toolbar;

public class Dashboard extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemReselectedListener{

    View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_dashboard, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BottomNavigationView bottomNavigationView=mView.findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemReselectedListener(this);

        loadfragment(new Home());
        checkFriendRequest();
        if (add_post != null && FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
            add_post.setVisible(false);
        }
    }

    public void loadfragment(androidx.fragment.app.Fragment fragment) {
        ((AppCompatActivity)getActivity()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_home:
                if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
                    add_post.setVisible(true);
                }
                loadfragment(new Home());
                break;

            case R.id.action_discover:
                add_post.setVisible(false);
                loadfragment(new Discover());
                break;

        }
        return true;
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_home:
                break;

            case R.id.action_discover:
                break;

        }
    }
    private CardView request_alert;
    private TextView request_alert_text;

    private void checkFriendRequest(){

        request_alert=mView.findViewById(R.id.friend_req_alert);
        request_alert_text=mView.findViewById(R.id.friend_req_alert_text);

        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friend_Requests")
                .addSnapshotListener(getActivity(), (queryDocumentSnapshots, e) -> {

                    if(e!=null){
                        e.printStackTrace();
                        return;
                    }

                    if(!queryDocumentSnapshots.isEmpty()){
                        try {
                            request_alert_text.setText(String.format(getString(R.string.you_have_d_new_friend_request_s), queryDocumentSnapshots.size()));
                            request_alert.setVisibility(View.VISIBLE);
                            request_alert.setAlpha(0.0f);

                            request_alert.animate()
                                    .setDuration(300)
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .alpha(1.0f)
                                    .start();

                            request_alert.setOnClickListener(v -> {
                                if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                    toolbar.setTitle("Manage Friends");
                                    try {
                                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Manage Friends");
                                    } catch (Exception e12) {
                                        Log.e("Error", e12.getMessage());
                                    }
                                    showFragment(FriendsFragment.newInstance("request"));
                                } else {
                                    showDialog();
                                }
                            });
                        }catch (Exception e1){
                            e1.printStackTrace();
                        }
                    }

                });

    }

    public void showDialog(){

        new DialogSheet(mView.getContext())
                .setTitle("Information")
                .setMessage("Email has not been verified, please verify and continue. If you have verified we recommend you to logout and login again")
                .setPositiveButton("Send again", v -> FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification()
                        .addOnSuccessListener(aVoid -> Toasty.success(mView.getContext(), "Verification email sent", Toasty.LENGTH_SHORT,true).show())
                        .addOnFailureListener(e -> Log.e("Error", e.getMessage())))
                .setNegativeButton("Ok", v -> {

                })
                .setCancelable(true)
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .show();

    }

}
