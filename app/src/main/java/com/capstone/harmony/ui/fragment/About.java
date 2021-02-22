package com.capstone.harmony.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.capstone.harmony.BuildConfig;
import com.capstone.harmony.R;
import com.mikepenz.aboutlibraries.LibsBuilder;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class About extends Fragment {

    LinearLayout email,instagram,google,libraries,support;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        email=view.findViewById(R.id.email);
        instagram=view.findViewById(R.id.instagram);
        libraries=view.findViewById(R.id.libraries);
        support=view.findViewById(R.id.support);

        TextView version=view.findViewById(R.id.version);
        version.setText(BuildConfig.VERSION_NAME);

        support.setOnClickListener(v -> {

            String url = "https://play.google.com/store/apps/details?id=lorma.ccse.johnrobert.manongcustomer";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

        });

        libraries.setOnClickListener(v -> new LibsBuilder()
                 .withAutoDetect(true)
                 .withActivityTitle("Open Source Libraries")
                 .withActivityTheme(R.style.AppTheme)
                 .start(getView().getContext()));

        email.setOnClickListener(v -> {

            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.email_account)});
            email.putExtra(Intent.EXTRA_SUBJECT, "Sent from "+Build.BRAND+" "+Build.DEVICE);
            email.putExtra(Intent.EXTRA_TEXT, "Harmony\nversion:"+BuildConfig.VERSION_NAME+"\nandroid version:"+Build.VERSION.CODENAME);
            email.setType("message/rfc822");
            startActivity(Intent.createChooser(email, "Select email app"));

        });

        instagram.setOnClickListener(v -> {

            String url = getResources().getString(R.string.insta_url);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

        });


    }

}
