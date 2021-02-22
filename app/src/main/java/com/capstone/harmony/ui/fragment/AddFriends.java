package com.capstone.harmony.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import es.dmoral.toasty.Toasty;


public class AddFriends extends Fragment {

    View mView;
    private List<Friends> usersList;
    private AddFriendAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_add_friends, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = mView.findViewById(R.id.recyclerView);
        refreshLayout=mView.findViewById(R.id.refreshLayout);

        usersList = new ArrayList<>();
        usersAdapter = new AddFriendAdapter(usersList, view.getContext(), mView.findViewById(R.id.layout), false);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));

        if (!mAuth.getCurrentUser().getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
            ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerViewTouchHelper(0, ItemTouchHelper.LEFT, (viewHolder, direction, position) -> {
                if (viewHolder instanceof AddFriendAdapter.ViewHolder) {
                    // get the removed item name to display it in snack bar
                    String name = usersList.get(viewHolder.getAdapterPosition()).getName();

                    // backup of removed item for undo purpose
                    final Friends deletedItem = usersList.get(viewHolder.getAdapterPosition());
                    final int deletedIndex = viewHolder.getAdapterPosition();

                    Snackbar snackbar = Snackbar
                            .make(mView.findViewById(R.id.layout), "Friend request sent to " + name, Snackbar.LENGTH_LONG);

                    // remove the item from recycler view
                    usersAdapter.removeItem(viewHolder.getAdapterPosition(), snackbar, deletedIndex, deletedItem);

                }
            });
            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        }

        mRecyclerView.setAdapter(usersAdapter);
        refreshLayout.setOnRefreshListener(() -> getAllUsers());

        getAllUsers();

    }

    public void getAllUsers() {
        usersList.clear();
        usersAdapter.notifyDataSetChanged();
        mView.findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);

        firestore.collection("Users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.getDocuments().isEmpty()) {

                        for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                if (!doc.getDocument().getId().equals(mAuth.getCurrentUser().getUid()) && doc.getDocument().get("email") != null && !((String) doc.getDocument().get("email")).equalsIgnoreCase(getString(R.string.admin_email))) {
                                    Friends friends = doc.getDocument().toObject(Friends.class).withId(doc.getDocument().getString("id"));
                                    usersList.add(friends);
                                }

                            }
                        }
                        refreshLayout.setRefreshing(false);
                        if(usersList.isEmpty()){
                            mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }else {
                            usersAdapter.notifyDataSetChanged();
                        }

                    }else{
                        refreshLayout.setRefreshing(false);
                        mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                    }

                })
                .addOnFailureListener(e -> {

                    refreshLayout.setRefreshing(false);
                    Toasty.error(mView.getContext(), "Some technical error occurred", Toasty.LENGTH_SHORT,true).show();
                    Log.w("Error", "listen:error", e);

                });



    }


}
