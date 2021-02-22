package com.capstone.harmony.adapters;

import android.content.Context;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.capstone.harmony.R;
import com.capstone.harmony.models.Images;
import com.capstone.harmony.ui.views.HifyImageView;
import com.bumptech.glide.Glide;

import java.util.List;

public class PagerPhotosAdapter extends PagerAdapter {


    private List<Images> IMAGES;
    private Context context;
    private LayoutInflater inflater;


    public PagerPhotosAdapter(Context context, List<Images> IMAGES) {
        this.context = context;
        this.IMAGES =IMAGES;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, final int position) {
        View imageLayout = inflater.inflate(R.layout.item_viewpager_image, view, false);

        assert imageLayout !=null;
        HifyImageView imageView=imageLayout.findViewById(R.id.image);
        VideoView videoView = imageLayout.findViewById(R.id.video);
        RelativeLayout relativeLayout = imageLayout.findViewById(R.id.video_container);
        ProgressBar progressBar = imageLayout.findViewById(R.id.progress);

        String mediaPath = IMAGES.get(position).getPath();

        String tempMediaPath = mediaPath.toLowerCase().trim();
        if (tempMediaPath.contains(".png") || tempMediaPath.contains("jpg") || tempMediaPath.contains("jpeg")) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(mediaPath)
                    .into(imageView);
        }else {
            relativeLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            videoView.setVideoPath(mediaPath);
            MediaController mediaController = new MediaController(context);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
            videoView.requestFocus();
            videoView.start();
        }


        view.addView(imageLayout,0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }


    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
