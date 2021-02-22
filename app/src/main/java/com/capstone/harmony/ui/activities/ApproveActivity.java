package com.capstone.harmony.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.capstone.harmony.R;
import com.capstone.harmony.adapters.addFriends.AddFriendAdapter;
import com.capstone.harmony.adapters.addFriends.RecyclerViewTouchHelper;
import com.capstone.harmony.models.Friends;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ApproveActivity extends AppCompatActivity {

    private List<Friends> usersList;
    private AddFriendAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout refreshLayout;

    public static String communityName = null;
    public static boolean isShowRegistered = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        communityName = null;
        isShowRegistered = false;
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        if (isShowRegistered) {
            getAllUsers("registered");
        }else {
            getAllUsers("pending");
        }
        super.onStart();
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
        setContentView(R.layout.activity_approve);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = findViewById(R.id.recyclerView);
        refreshLayout= findViewById(R.id.refreshLayout);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerViewTouchHelper(0, ItemTouchHelper.LEFT, (viewHolder, direction, position) -> {
            if (viewHolder instanceof AddFriendAdapter.ViewHolder) {
                // get the removed item name to display it in snack bar
                String name = usersList.get(viewHolder.getAdapterPosition()).getName();

                // backup of removed item for undo purpose
                final Friends deletedItem = usersList.get(viewHolder.getAdapterPosition());
                final int deletedIndex = viewHolder.getAdapterPosition();

                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.layout), "Friend request sent to " + name, Snackbar.LENGTH_LONG);

                // remove the item from recycler view
                usersAdapter.removeItem(viewHolder.getAdapterPosition(), snackbar, deletedIndex, deletedItem);

            }
        });

        usersList = new ArrayList<>();
        usersAdapter = new AddFriendAdapter(usersList, this, findViewById(R.id.layout), true);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(usersAdapter);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Approve Request");

        getSupportActionBar().setTitle("Approve Request");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        communityName = getIntent().getStringExtra("community");
        ((TextView) findViewById(R.id.text_community)).setText("Request for " + communityName + " Community");

        ((Switch) findViewById(R.id.switch_registered)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                getAllUsers("registered");
                ((TextView) findViewById(R.id.text_community)).setText("Registered in " + communityName + " Community");
            }else {
                getAllUsers("pending");
                ((TextView) findViewById(R.id.text_community)).setText("Request for " + communityName + " Community");
            }
            isShowRegistered = isChecked;
        });

        refreshLayout.setOnRefreshListener(() -> {
            if (isShowRegistered) {
                getAllUsers("registered");
            }else {
                getAllUsers("pending");
            }
        });

    }

    public void getAllUsers(String value) {
        usersList.clear();
        usersAdapter.notifyDataSetChanged();
        findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);

        firestore.collection("Users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {

                        for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                Map<String, String> communities = (Map<String, String>) doc.getDocument().get("communities");
                                if (communities != null && communities.size() > 0) {
                                    for (String community: communities.keySet()) {
                                        String status = communities.get(community);
                                        if (!doc.getDocument().getId().equals(mAuth.getCurrentUser().getUid()) && status != null && status.equalsIgnoreCase(value)) {
                                            if (community.equalsIgnoreCase(communityName)) {
                                                Friends friends = doc.getDocument().toObject(Friends.class).withId(doc.getDocument().getString("id"));
                                                usersList.add(friends);
                                                break;
                                            }
                                        }
                                    }
                                }

                            }
                        }
                        refreshLayout.setRefreshing(false);
                        if(usersList.isEmpty()){
                            findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            if (value.equalsIgnoreCase("pending")) {
                                ((TextView) findViewById(R.id.default_title)).setText("No request found");
                            }else {
                                ((TextView) findViewById(R.id.default_title)).setText("No registered found");
                            }
                        }else {
                            usersAdapter.notifyDataSetChanged();
                        }

                    }else{
                        refreshLayout.setRefreshing(false);
                        findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {

                    refreshLayout.setRefreshing(false);
                    Toasty.error(this, "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                    Log.w("Error", "listen:error", e);

                });



    }
}
