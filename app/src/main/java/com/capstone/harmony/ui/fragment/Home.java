package com.capstone.harmony.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.capstone.harmony.R;
import com.capstone.harmony.adapters.PostsAdapter;
import com.capstone.harmony.models.Post;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class Home extends Fragment implements View.OnClickListener {

    private List<Post> mPostsList;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private RecyclerView mPostsRecyclerView;
    private View mView;
    private List<String> mFriendIdList = new ArrayList<>();
    private View statsheetView;
    private BottomSheetDialog mmBottomSheetDialog;
    private SwipeRefreshLayout refreshLayout;
    private PostsAdapter mAdapter_v19;

    private TextView t1,t2,t3,t4,t5,tCommunity;
    public static String currentCommunity = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_home, container, false);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
//        mAdapter_v19.notifyDataSetChanged();
        getAllPosts();
        if (currentCommunity != null) {
            tCommunity.setText(currentCommunity);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        refreshLayout=view.findViewById(R.id.refreshLayout);

        statsheetView = getActivity().getLayoutInflater().inflate(R.layout.stat_bottom_sheet_dialog, null);
        mmBottomSheetDialog = new BottomSheetDialog(view.getContext());
        mmBottomSheetDialog.setContentView(statsheetView);
        mmBottomSheetDialog.setCanceledOnTouchOutside(true);
        mPostsRecyclerView = view.findViewById(R.id.posts_recyclerview);

        mPostsList = new ArrayList<>();

        mAdapter_v19 = new PostsAdapter(mPostsList, view.getContext(), getActivity(), mmBottomSheetDialog, statsheetView, false);
        mPostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPostsRecyclerView.setHasFixedSize(true);
        mPostsRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(),DividerItemDecoration.VERTICAL));
        mPostsRecyclerView.setAdapter(mAdapter_v19);

        refreshLayout.setOnRefreshListener(() -> {
            mPostsList.clear();
            mAdapter_v19.notifyDataSetChanged();
            getAllPosts();
        });

        t1=view.findViewById(R.id.t1); // Cooking
        t2=view.findViewById(R.id.t2); // Sports
        t3=view.findViewById(R.id.t3); // Counseling
        t4=view.findViewById(R.id.t4); // Travel
        t5=view.findViewById(R.id.t5); // Gaming

        tCommunity = view.findViewById(R.id.current_community);

        getAllCommunities();

    }

    private void getAllCommunities() {
        t1.setOnClickListener(this);
        t2.setOnClickListener(this);
        t3.setOnClickListener(this);
        t4.setOnClickListener(this);
        t5.setOnClickListener(this);
        if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
            t1.setVisibility(View.VISIBLE);
            t2.setVisibility(View.VISIBLE);
            t3.setVisibility(View.VISIBLE);
            t4.setVisibility(View.VISIBLE);
            t5.setVisibility(View.VISIBLE);
        }else {
            CollectionReference users = mFirestore.collection("Users");
            users.document(currentUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.getException() != null) {
                            Toasty.error(mView.getContext(), task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                            return;
                        }
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot == null) {
                            Toasty.error(mView.getContext(), "No user found.", Toasty.LENGTH_LONG, true).show();
                            return;
                        }
                        Map<String, String> communities = (Map<String, String>) documentSnapshot.get("communities");

                        if (communities != null && communities.size() > 0) {
                            for (String community: communities.keySet()) {
                                String status = communities.get(community);
                                if (community.equalsIgnoreCase("cooking")) {
                                    t1.setVisibility(View.VISIBLE);
                                    if (status != null && status.equalsIgnoreCase("pending")) {
                                        t1.setTextColor(getResources().getColor(R.color.colorTextSubtitle));
                                    }
                                }else if (community.equalsIgnoreCase("sports")) {
                                    t2.setVisibility(View.VISIBLE);
                                    if (status != null && status.equalsIgnoreCase("pending")) {
                                        t2.setTextColor(getResources().getColor(R.color.colorTextSubtitle));
                                    }
                                }else if (community.equalsIgnoreCase("counseling")) {
                                    t3.setVisibility(View.VISIBLE);
                                    if (status != null && status.equalsIgnoreCase("pending")) {
                                        t3.setTextColor(getResources().getColor(R.color.colorTextSubtitle));
                                    }
                                }else if (community.equalsIgnoreCase("travel")) {
                                    t4.setVisibility(View.VISIBLE);
                                    if (status != null && status.equalsIgnoreCase("pending")) {
                                        t4.setTextColor(getResources().getColor(R.color.colorTextSubtitle));
                                    }
                                }else if (community.equalsIgnoreCase("study")) {
                                    t5.setVisibility(View.VISIBLE);
                                    if (status != null && status.equalsIgnoreCase("pending")) {
                                        t5.setTextColor(getResources().getColor(R.color.colorTextSubtitle));
                                    }
                                }
                            }
                        }
                    });
        }

    }

    private void getAllPosts() {
        getAllPostsCommunity(currentCommunity);
        /*mView.findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);
        // Getting the friends porefreshLayout.setRefreshing(true);sts
        mFirestore.collection("Users")
            .document(currentUser.getUid())
            .collection("Friends")
            .get()
            .addOnSuccessListener(querySnapshot -> {

                List<String> friendsId = new ArrayList<>();
                if (!querySnapshot.isEmpty()) {
                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        friendsId.add(documentChange.getDocument().getId());
                    }
                }

                getCurrentUsersPosts(friendsId);

            })
            .addOnFailureListener(e -> {
                refreshLayout.setRefreshing(false);
                Toasty.error(mView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                Log.w("Error", "listen:error", e);
            });*/
    }

    private void getCurrentUsersPosts(List<String> friendsId) {
        // Getting all the posts
        mFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mPostsList.clear();
                    if(queryDocumentSnapshots.isEmpty()){
                        refreshLayout.setRefreshing(false);
                        mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                    }else{

                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                            String postUserId = documentChange.getDocument().getString("userId");
                            // User's post of Friends post
                            if (postUserId.equals(mAuth.getCurrentUser().getUid()) || friendsId.contains(postUserId)) {
                                Post post = documentChange.getDocument().toObject(Post.class).withId(documentChange.getDocument().getId());
                                mPostsList.add(post);
                                refreshLayout.setRefreshing(false);
                                mAdapter_v19.notifyDataSetChanged();
                            }
                        }

                        if (mPostsList.isEmpty()) {
                            mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            refreshLayout.setRefreshing(false);
                        }
                    }

                })
                .addOnFailureListener(e -> {
                    refreshLayout.setRefreshing(false);
                    Toasty.error(mView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                    Log.w("Error", "listen:error", e);
                });
    }

    private void getAllPostsCommunity(String community) {
        if (community == null) {
            mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
            refreshLayout.setRefreshing(false);
            return;
        }
        mView.findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);
        // Getting all the post of this community
        mFirestore.collection("Posts")
                .whereEqualTo("community", community.trim())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mPostsList.clear();
                    if(queryDocumentSnapshots.isEmpty()){
                        refreshLayout.setRefreshing(false);
                        mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                    }else{
                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                            Post post = documentChange.getDocument().toObject(Post.class).withId(documentChange.getDocument().getId());
                            mPostsList.add(post);
                        }
                        if (mPostsList.isEmpty()) {
                            mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }else {
                            mAdapter_v19.notifyDataSetChanged();
                        }
                        refreshLayout.setRefreshing(false);
                    }

                })
                .addOnFailureListener(e -> {
                    refreshLayout.setRefreshing(false);
                    Toasty.error(mView.getContext(), "Some technical error occurred.", Toasty.LENGTH_SHORT,true).show();
                    Log.w("Error", "listen:error", e);
                });
    }

    @Override
    public void onClick(View v) {
        TextView t = (TextView)v;
        if (t.getCurrentTextColor() == getResources().getColor(R.color.colorTextSubtitle)) {
            Toasty.info(mView.getContext(), "This community is currently pending", Toasty.LENGTH_LONG, true).show();
        }else {
            currentCommunity = t.getText().toString().trim();
            getAllPosts();
            tCommunity.setText(currentCommunity);
        }
    }
}
