package com.capstone.harmony.newsapi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.capstone.harmony.R;
import com.capstone.harmony.newsapi.models.Article;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder>{

    private List<Article> articles;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private String email;


    public Adapter(List<Article> articles, Context context, String email) {
        this.articles = articles;
        this.context = context;
        this.email = email;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_discover, parent, false);
        return new MyViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holders, int position) {
        final MyViewHolder holder = holders;
        Article article = articles.get(position);

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(Utils.getRandomDrawbleColor())
                .error(Utils.getRandomDrawbleColor())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();

        Glide.with(context)
                .load(article.getUrlToImage())
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imageView);

        holder.title.setText(article.getTitle());
        holder.desc.setText(article.getDescription());
        holder.source.setText(article.getPopulation());
        if (email.equalsIgnoreCase(context.getString(R.string.admin_email))) {
            holder.time.setText(String.format(" • population"));
        }else {
            holder.time.setText("");
        }
        holder.published_ad.setText(Utils.DateFormat(article.getPublishedAt()));
        holder.published_ad.setOnClickListener(v -> {

            if (!article.getPublishedAt().equalsIgnoreCase("Adviser")) {
                final DocumentSnapshot[] documentSnapshot = {null};
                CollectionReference community_questions = FirebaseFirestore.getInstance().collection("Community_Questions");
                community_questions.document("1").get()
                        .addOnCompleteListener(task -> {
                            if (task.getException() != null) {
                                return;
                            }
                            documentSnapshot[0] = task.getResult();
                            if (documentSnapshot[0] == null) {
                                return;
                            }
                        });

                new MaterialDialog.Builder(context)
                        .title("Adviser " + article.getTitle())
                        .content("Are you sure you want to be Adviser for this community?")
                        .positiveText("YES")
                        .negativeText("CANCEL")
                        .onPositive((dialog, which) -> {
                            List<String> questions = (List<String>) documentSnapshot[0].get(article.getTitle());
                            if (questions != null) {
                                dialog.dismiss();
                                View view = LayoutInflater.from(context).inflate(R.layout.community_question_layout, null);
                                LinearLayout linearLayout = view.findViewById(R.id.verticalLayout);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                                List<EditText> answers = new ArrayList<>();
                                for (String question: questions) {
                                    View view2 = LayoutInflater.from(context).inflate(R.layout.community_questions, null);
                                    TextView textView = view2.findViewById(R.id.text_question);
                                    EditText answer = view2.findViewById(R.id.answer_edittext);
                                    textView.setText(question);
                                    view2.setLayoutParams(params);
                                    linearLayout.addView(view2);
                                    answers.add(answer);
                                }

                                new MaterialDialog.Builder(context)
                                        .title("Questions for " + article.getTitle())
                                        .customView(view, true)
                                        .positiveText("SUBMIT")
                                        .negativeText("BACKOUT")
                                        .onNegative((dialog1, which1) -> dialog1.dismiss())
                                        .onPositive((dialog1, which1) -> {
                                            CollectionReference users = FirebaseFirestore.getInstance().collection("Users");
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                            if (user != null) {
                                                Map<String, Object> adviser = new HashMap<>();
                                                adviser.put(article.getTitle(), "pending");

                                                Map<String, Object> answer_string = new HashMap<>();
                                                Map<String, String> string_answers = new HashMap<>();
                                                int i = 0;
                                                for (EditText editText: answers) {
                                                    string_answers.put(questions.get(i), editText.getText().toString());
                                                    i++;
                                                }
                                                answer_string.put(article.getTitle(), string_answers);
                                                Map<String, Map<String, Object>> advisers = new HashMap<>();
                                                advisers.put("adviser", adviser);
                                                advisers.put("answers", answer_string);
                                                users.document(user.getUid()).set(advisers, SetOptions.merge())
                                                        .addOnCompleteListener(task -> {
                                                            if (task.getException() != null && task.getException().getMessage() != null) {
                                                                Toasty.error(context, task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                                                                return;
                                                            }
                                                            Toasty.info(context, "Congratulations! Please wait for the admin confirmation.", Toasty.LENGTH_LONG, true).show();
                                                        });



                                            }
                                        })
                                        .show();
                            }

                        })
                        .onNegative((dialog, which) -> dialog.dismiss()).show();
            }
        });
        holder.author.setText(article.getAuthor());

    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {

        TextView title, desc, author, published_ad, source, time;
        ImageView imageView;
        ProgressBar progressBar;
        OnItemClickListener onItemClickListener;

        public MyViewHolder(View itemView, OnItemClickListener onItemClickListener) {

            super(itemView);

            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            author = itemView.findViewById(R.id.author);
            published_ad = itemView.findViewById(R.id.publishedAt);
            source = itemView.findViewById(R.id.source);
            time = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.img);
            progressBar = itemView.findViewById(R.id.prograss_load_photo);

            this.onItemClickListener = onItemClickListener;

        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }
}
