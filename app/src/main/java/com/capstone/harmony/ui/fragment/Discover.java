package com.capstone.harmony.ui.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.capstone.harmony.R;
import com.capstone.harmony.newsapi.Adapter;
import com.capstone.harmony.newsapi.models.Article;
import com.capstone.harmony.newsapi.models.Source;
import com.capstone.harmony.ui.activities.ApproveActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class Discover extends Fragment implements SwipeRefreshLayout.OnRefreshListener {


    private RecyclerView recyclerView;
    private Context context;
    private SwipeRefreshLayout refreshLayout;
    private View view;
    private FirebaseAuth mAuth;
    private LinearLayout default_item;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private ImageView default_image;
    private TextView default_title,default_text;
    private Button retryBtn;

    private TextView t1,t2,t3,t4,t5,t6,t7;
    private String category;
    private int d1, d2, d3, d4, d5, cooking_advisers, sports_advisers, counseling_advisers, travel_advisers, study_advisers;
    public static String cooking_name, sports_name, technology_name, gaming_name, travel_name;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_discover, container, false);
        return view;
    }

    @Override
    public void onStart() {
        refreshLayout.setRefreshing(true);
        setUpCommunity();
        ApproveActivity.communityName = null;
        ApproveActivity.isShowRegistered = false;
        super.onStart();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        refreshLayout=view.findViewById(R.id.refreshLayout);

        mAuth=FirebaseAuth.getInstance();
        default_item=view.findViewById(R.id.default_item);
        default_image=view.findViewById(R.id.default_image);
        default_text=view.findViewById(R.id.default_text);
        default_title=view.findViewById(R.id.default_title);
        retryBtn=view.findViewById(R.id.retry_btn);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        t1=view.findViewById(R.id.t1);
        t2=view.findViewById(R.id.t2);
        t3=view.findViewById(R.id.t3);
        t4=view.findViewById(R.id.t4);
        t5=view.findViewById(R.id.t5);
        t6=view.findViewById(R.id.t6);
        t7=view.findViewById(R.id.t7);


        /*category="general";
        loadJSON("","");*/

        refreshLayout.setOnRefreshListener(this);

        t1.setOnClickListener(v -> {
            if(!category.equals("general")) {
                loadJSON("","");
                category = "general";
            }
        });

        t2.setOnClickListener(v -> {
            if(!category.equals("business")) {
                category = "business";
                loadJSON("",category);
            }
        });

        t3.setOnClickListener(v -> {
            if(!category.equals("entertainment")) {
                category = "entertainment";
                loadJSON("",category);
            }
        });

        t4.setOnClickListener(v -> {
            if(!category.equals("health")) {
                category = "health";
                loadJSON("",category);
            }
        });

        t5.setOnClickListener(v -> {
            if(!category.equals("science")) {
                category = "science";
                loadJSON("",category);
            }
        });

        t6.setOnClickListener(v -> {
            if(!category.equals("sports")) {
                category = "sports";
                loadJSON("",category);
            }
        });

        t7.setOnClickListener(v -> {
            if(!category.equals("counseling")) {
                category = "counseling";
                loadJSON("",category);
            }
        });

    }

    private void setUpCommunity() {
        if (/*FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(getString(R.string.admin_email))*/ true) {
            refreshLayout.setRefreshing(true);
            CollectionReference users = FirebaseFirestore.getInstance().collection("Users");
            users.get().addOnCompleteListener(task -> {
                if (task.getException() != null) {
                    Toasty.error(context, task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                    return;
                }
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot == null) {
                    Toasty.error(context, "No documents found.", Toasty.LENGTH_LONG, true).show();
                    return;

                }
                d1 = 0; d2 = 0; d3 = 0; d4 = 0; d5 = 0;
                for (DocumentSnapshot documentSnapshot: querySnapshot) {
                    Map<String, String> communities = (Map<String, String>) documentSnapshot.get("communities");

                    if (communities != null && communities.size() > 0) {
                        for (String community: communities.keySet()) {
                            String status = communities.get(community);
                            if (status != null && !status.equalsIgnoreCase("pending")) {
                                if (community.equalsIgnoreCase("cooking")) {
                                    d1++;
                                }else if (community.equalsIgnoreCase("sports")) {
                                    d2++;
                                }else if (community.equalsIgnoreCase("counseling")) {
                                    d3++;
                                }else if (community.equalsIgnoreCase("travel")) {
                                    d4++;
                                }else if (community.equalsIgnoreCase("study")) {
                                    d5++;
                                }
                            }
                        }
                    }
                }
                CollectionReference advisers = FirebaseFirestore.getInstance().collection("Advisers");
                advisers.document("1").get()
                        .addOnCompleteListener(task1 -> {
                            if (task1.getException() != null) {
                                Toast.makeText(context, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            DocumentSnapshot documentSnapshot = task1.getResult();
                            if (documentSnapshot == null) {
                                Toast.makeText(context, "Cannot fetch the advisers.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            cooking_advisers = 0;
                            sports_advisers = 0;
                            counseling_advisers = 0;
                            travel_advisers = 0;
                            study_advisers = 0;

                            List<String> cookingIds = (List<String>) documentSnapshot.get("Cooking");
                            List<String> sportsIds = (List<String>) documentSnapshot.get("Sports");
                            List<String> technologyIds = (List<String>) documentSnapshot.get("Counseling");
                            List<String> travelIds = (List<String>) documentSnapshot.get("Travel");
                            List<String> gamingIds = (List<String>) documentSnapshot.get("Study");

                            if (cookingIds != null) {
                                cooking_advisers = cookingIds.size();
                            }
                            if (sportsIds != null) {
                                sports_advisers = sportsIds.size();
                            }
                            if (technologyIds != null) {
                                counseling_advisers = technologyIds.size();
                            }
                            if (travelIds != null) {
                                travel_advisers = travelIds.size();
                            }
                            if (gamingIds != null) {
                                study_advisers = gamingIds.size();
                            }

                            users.get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.getException() != null){
                                            Toast.makeText(context, task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        QuerySnapshot queryDocumentSnapshots = task2.getResult();
                                        if (queryDocumentSnapshots == null) {
                                            Toast.makeText(context, "Cannot fetch the advisers data.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        cooking_name = "No adviser";
                                        sports_name = "No adviser";
                                        technology_name = "No adviser";
                                        travel_name = "No adviser";
                                        gaming_name = "No adviser";

                                        for (DocumentSnapshot documentSnapshot1: queryDocumentSnapshots) {
                                            if (cookingIds != null && cookingIds.contains(documentSnapshot1.getId())) {
                                                if (documentSnapshot1.getId().equalsIgnoreCase(mAuth.getUid())) {
                                                    cooking_name = mAuth.getUid();
                                                }
                                            }if (sportsIds != null && sportsIds.contains(documentSnapshot1.getId())) {
                                                if (documentSnapshot1.getId().equalsIgnoreCase(mAuth.getUid())) {
                                                    sports_name =  mAuth.getUid();
                                                }
                                            }if (technologyIds != null && technologyIds.contains(documentSnapshot1.getId())) {
                                                if (documentSnapshot1.getId().equalsIgnoreCase(mAuth.getUid())) {
                                                    technology_name =  mAuth.getUid();
                                                }
                                            }if (travelIds != null && travelIds.contains(documentSnapshot1.getId())) {
                                                if (documentSnapshot1.getId().equalsIgnoreCase(mAuth.getUid())) {
                                                    travel_name =  mAuth.getUid();
                                                }
                                            }if (gamingIds != null && gamingIds.contains(documentSnapshot1.getId())) {
                                                if (documentSnapshot1.getId().equalsIgnoreCase(mAuth.getUid())) {
                                                    gaming_name =  mAuth.getUid();
                                                }
                                            }
                                        }
                                        initArticlesAdmin(cooking_name, sports_name, technology_name, travel_name, gaming_name);
                                    });
                        });

            });
        }else {
            refreshLayout.setRefreshing(true);
            CollectionReference users = FirebaseFirestore.getInstance().collection("Users");
            CollectionReference advisers = FirebaseFirestore.getInstance().collection("Advisers");
            advisers.document("1").get()
                    .addOnCompleteListener(task1 -> {
                        if (task1.getException() != null) {
                            Toast.makeText(context, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        DocumentSnapshot documentSnapshot = task1.getResult();
                        if (documentSnapshot == null) {
                            Toast.makeText(context, "Cannot fetch the advisers.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String cookingId = (String) documentSnapshot.get("Cooking");
                        String sportsId = (String) documentSnapshot.get("Sports");
                        String technologyId = (String) documentSnapshot.get("Counseling");
                        String travelId = (String) documentSnapshot.get("Travel");
                        String gamingId = (String) documentSnapshot.get("Study");
                        users.get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.getException() != null){
                                        Toast.makeText(context, task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    QuerySnapshot queryDocumentSnapshots = task2.getResult();
                                    if (queryDocumentSnapshots == null) {
                                        Toast.makeText(context, "Cannot fetch the advisers data.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    String cooking_name = "No adviser";
                                    String sports_name = "No adviser";
                                    String technology_name = "No adviser";
                                    String travel_name = "No adviser";
                                    String gaming_name = "No adviser";
                                    for (DocumentSnapshot documentSnapshot1: queryDocumentSnapshots) {
                                        if (cookingId != null && documentSnapshot1.getId().equalsIgnoreCase(cookingId)) {
                                            cooking_name = (String) documentSnapshot1.get("name");
                                        }else if (sportsId != null && documentSnapshot1.getId().equalsIgnoreCase(sportsId)) {
                                            sports_name =  (String) documentSnapshot1.get("name");
                                        }else if (technologyId != null && documentSnapshot1.getId().equalsIgnoreCase(technologyId)) {
                                            technology_name =  (String) documentSnapshot1.get("name");
                                        }else if (travelId != null && documentSnapshot1.getId().equalsIgnoreCase(travelId)) {
                                            travel_name =  (String) documentSnapshot1.get("name");
                                        }else if (gamingId != null && documentSnapshot1.getId().equalsIgnoreCase(gamingId)) {
                                            gaming_name =  (String) documentSnapshot1.get("name");
                                        }
                                    }
                                    initArticles(cooking_name, sports_name, technology_name, travel_name, gaming_name);
                                });
                    });
        }
    }

    private void initArticlesAdmin(String cooking, String sports, String technology, String travel, String gaming) {
        Source source1 = new Source();
        source1.setName(String.valueOf(d1));
        source1.setId("asd324rdafa34r23");

        Article article1 = new Article();
        article1.setUrlToImage("https://www.phoenixfuels.ph/wp-content/uploads/2019/06/6-4-Phoenix-SUPER-LPG-brings-the-joys-of-cooking-to-Filipino-communities-Horseshoe-Game-1024x768.jpg");
        article1.setTitle("Cooking");
        article1.setDescription("We share recipes and write about restaurants, markets, chefs, home cooks, personal stories and world history. Politics, too! (Yes, we are going THERE.) We post photos of our mouth-watering dishes, our adventures in grocery shopping, travel and much more.");
        article1.setSource(source1);
        article1.setAuthor(cooking_advisers + " Advisers");
        if (cooking.equalsIgnoreCase("No adviser")) {
            article1.setPublishedAt("Register as Adviser");
            article1.setPopulation("Join now");
        }else {
            article1.setPublishedAt("Adviser");
            article1.setPopulation(d1 + " population");
        }


        Article article2 = new Article();
        article2.setUrlToImage("https://asset-sports.abs-cbn.com/web/dev/articles/1559645612_image.jpeg");
        article2.setTitle("Sports");
        article2.setDescription("For many clubs, sponsorship is the financial lifeblood of the club. It sounds awesome but not everyone necessarily knows the answer to “what is sponsorship”. Raising funds through sponsorship doesn’t have to be difficult. Before we look at the strategies…");
        article2.setSource(source1);
        article2.setAuthor(sports_advisers + " Advisers");
        if (sports.equalsIgnoreCase("No adviser")) {
            article2.setPublishedAt("Register as Adviser");
            article2.setPopulation("Join now");
        }else {
            article2.setPublishedAt("Adviser");
            article2.setPopulation(d2 + " population");
        }

        Article article3 = new Article();
        article3.setUrlToImage("https://filipinotimes.net/wp-content/uploads/2018/05/31960850_10156347634314894_1094731621233328128_n.jpg");
        article3.setTitle("Counseling");
        article3.setDescription("The field of Community Counseling is broad and diverse, applying principles of both counseling and social work in a community setting. Effective community counseling involves helping clients work through their mental health concerns, while also helping to prevent those concerns from proliferating in the community.");
        article3.setSource(source1);
        article3.setAuthor(counseling_advisers + " Advisers");
        if (technology.equalsIgnoreCase("No adviser")) {
            article3.setPublishedAt("Register as Adviser");
            article3.setPopulation("Join now");
        }else {
            article3.setPublishedAt("Adviser");
            article3.setPopulation(d3 + " population");
        }

        Article article4 = new Article();
        article4.setUrlToImage("https://imagevars.gulfnews.com/2016/4/9/1_16a082b8ae7.1707186_2664026384_16a082b8ae7_large.jpg");
        article4.setTitle("Travel");
        article4.setDescription("The internet is a vast resource when it comes to travel – there’s an abundance of websites and apps designed to aid those looking to explore. At Lonely Planet, we curate collections of our experts' best recommendations, but we also realise the value of community, of conversing with fellow travellers who have been where you want to go, and who can answer questions and share experiences in real time.");
        article4.setSource(source1);
        article4.setAuthor(travel_advisers + " Advisers");
        if (travel.equalsIgnoreCase("No adviser")) {
            article4.setPublishedAt("Register as Adviser");
            article4.setPopulation("Join now");
        }else {
            article4.setPublishedAt("Adviser");
            article4.setPopulation(d4 + " population");
        }

        Article article5 = new Article();
        article5.setUrlToImage("https://s3.amazonaws.com/spweb-uploads/2018/11/1862PH-A-161-Philippines-764x460.jpg");
        article5.setTitle("Study");
        article5.setDescription("The thing with communities, in general, is that it provides a haven for people to talk about what they are truly passionate about. The same goes for the communities centered around studies and ideas. Not only do they offer a place to socialize and share ideas among people with similar interests, it also acts as a hub for friends to be made, and connections to be formed.");
        article5.setSource(source1);
        article5.setAuthor(study_advisers + " Advisers");
        if (technology.equalsIgnoreCase("No adviser")) {
            article5.setPublishedAt("Register as Adviser");
            article5.setPopulation("Join now");
        }else {
            article5.setPublishedAt("Adviser");
            article5.setPopulation(d5 + " population");
        }

        articles.clear();
        articles.add(article1);
        articles.add(article2);
        articles.add(article3);
        articles.add(article4);
        articles.add(article5);

        adapter = new Adapter(articles, context, FirebaseAuth.getInstance().getCurrentUser().getEmail());
        recyclerView.setAdapter(adapter);
        initListener();
        refreshLayout.setRefreshing(false);
    }

    private void initArticles(String cooking, String sports, String technology, String travel, String gaming) {
        Article article1 = new Article();
        article1.setUrlToImage("https://d1n37dg5546nto.cloudfront.net/sites/jmf/files/styles/hero/public/media/community_groups_carousel_1.jpg?itok=JV2CH89o");
        article1.setTitle("Cooking");
        article1.setDescription("We share recipes and write about restaurants, markets, chefs, home cooks, personal stories and world history. Politics, too! (Yes, we are going THERE.) We post photos of our mouth-watering dishes, our adventures in grocery shopping, travel and much more.");
        Source source1 = new Source();
        source1.setName("Join Now");
        source1.setId("asd324rdafa34r23");
        article1.setSource(source1);
        article1.setPublishedAt("02 May 2019");
        article1.setAuthor(cooking);
        article1.setPopulation("Join Now");

        Article article2 = new Article();
        article2.setUrlToImage("https://tybody.com/wp-content/uploads/2015/04/group.jpg");
        article2.setTitle("Sports");
        article2.setDescription("Physical fitness is a state of health and well-being and, more specifically, the ability to perform aspects of sports, occupations and daily activities. Physical fitness is generally achieved through proper nutrition, moderate-vigorous physical exercise, and sufficient rest.");
        article2.setSource(source1);
        article2.setPublishedAt("08 September 2017");
        article2.setAuthor(sports);
        article2.setPopulation("Join Now");

        Article article3 = new Article();
        article3.setUrlToImage("https://www.news.iastate.edu/media/2014/02/PY2W.jpg");
        article3.setTitle("Counseling");
        article3.setDescription("Counseling psychology is a psychological specialty that encompasses research and applied work in several broad domains: counseling process and outcome; supervision and training; career development and counseling; and prevention and health.");
        article3.setSource(source1);
        article3.setPublishedAt("19 January 2018");
        article3.setAuthor(technology);
        article3.setPopulation("Join Now");

        Article article4 = new Article();
        article4.setUrlToImage("https://www.sheswanderful.com/wp-content/uploads/2015/10/IMG_2212.jpg");
        article4.setTitle("Travel");
        article4.setDescription("The internet is a vast resource when it comes to travel – there’s an abundance of websites and apps designed to aid those looking to explore. At Lonely Planet, we curate collections of our experts' best recommendations, but we also realise the value of community, of conversing with fellow travellers who have been where you want to go, and who can answer questions and share experiences in real time.");
        article4.setSource(source1);
        article4.setPublishedAt("05 August 2019");
        article4.setAuthor(travel);
        article4.setPopulation("Join Now");

        Article article5 = new Article();
        article5.setUrlToImage("https://cdn.studyinternational.com/news/wp-content/uploads/2016/11/uploads_shutterstock_402458263.jpg");
        article5.setTitle("Study");
        article5.setDescription("Study skills, academic skill, or study strategies are approaches applied to learning. They are generally critical to success in school, considered essential for acquiring good grades, and useful for learning throughout one's life.");
        article5.setSource(source1);
        article5.setPublishedAt("16 December 2016");
        article5.setAuthor(gaming);
        article5.setPopulation("Join Now");

        articles.add(article1);
        articles.add(article2);
        articles.add(article3);
        articles.add(article4);
        articles.add(article5);

        adapter = new Adapter(articles, context, FirebaseAuth.getInstance().getCurrentUser().getEmail());
        recyclerView.setAdapter(adapter);
        initListener();
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        setUpCommunity();
        /*if(category.equals("general")) {
            loadJSON("", "");
            refreshLayout.setRefreshing(false);
        }else{
            if(category.length()>0){
                loadJSON("",category);
                refreshLayout.setRefreshing(false);
            }
        }*/
    }

    private void loadJSON(final String keyword,final String category){

        /*default_item.setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String country = Utils.getCountry();
        String language = Utils.getLanguage();

        Call<News> call;

        if (keyword.length() > 0 ){
            call = apiInterface.getNewsSearch(keyword, language, "publishedAt", Config.NEWS_API_KEY);
        } else {

            if(category.length()>0) {
                call = apiInterface.getNewsByCategory(country,category, Config.NEWS_API_KEY);
            }else{
                call = apiInterface.getNews(country, Config.NEWS_API_KEY);
            }
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null){

                    if (!articles.isEmpty()){
                        articles.clear();
                    }

                    Log.i("NewsApi",response.toString());

                    articles = response.body().getArticle();
                    adapter = new Adapter(articles, context);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();

                    //topHeadline.setVisibility(View.VISIBLE);
                    refreshLayout.setRefreshing(false);


                } else {

                    //topHeadline.setVisibility(View.INVISIBLE);
                    refreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode = "unknown error";
                            break;
                    }

                    showErrorMessage(
                            R.drawable.no_result,
                            "No Result",
                            "Please try again!\n"+
                                    errorCode);

                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                //topHeadline.setVisibility(View.INVISIBLE);
                refreshLayout.setRefreshing(false);
                showErrorMessage(
                        R.drawable.oops,
                        "Oops..",
                        "You are not connected to the internet");
            }
        });*/

    }

    private void showErrorMessage(int imageView, String title, String message){

        if (default_item.getVisibility() == View.GONE) {
            default_item.setVisibility(View.VISIBLE);
        }

        default_image.setImageResource(imageView);
        default_title.setText(title);
        default_text.setText(message);

        retryBtn.setOnClickListener(v -> loadJSON("",""));

    }

    private void initListener(){
        adapter.setOnItemClickListener((view, position) -> {
            /*ImageView imageView = view.findViewById(R.id.img);
            Intent intent = new Intent(context, MainActivity.class);

            intent.putExtra("url", article.getUrl());
            intent.putExtra("title", article.getTitle());
            intent.putExtra("img",  article.getUrlToImage());
            intent.putExtra("date",  article.getPublishedAt());
            intent.putExtra("source",  article.getSource().getName());
            intent.putExtra("author",  article.getAuthor());

            Article article = articles.get(position);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(article.getUrl()));
            startActivity(i);*/
            Article article = articles.get(position);
            if(article.getPublishedAt().equalsIgnoreCase("Adviser") || FirebaseAuth.getInstance().getCurrentUser().getEmail().equalsIgnoreCase(getString(R.string.admin_email))) {
                Intent intent = new Intent(getContext(), ApproveActivity.class);
                intent.putExtra("community", articles.get(position).getTitle());
                startActivity(intent);
            }else {
                // Adding to the community
                new MaterialDialog.Builder(getActivity())
                        .title("Join Community")
                        .content("Are you sure to join for community " + article.getTitle() + "?")
                        .positiveText("I'M IN")
                        .negativeText("CANCEL")
                        .onPositive((dialog, which) -> registerCommunity(article))
                        .onNegative((dialog, which) -> dialog.dismiss()).show();
            }
        });
    }

    private void registerCommunity(Article article) {
        CollectionReference users = FirebaseFirestore.getInstance().collection("Users");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> community = new HashMap<>();
        community.put(article.getTitle(), "pending");
        Map<String, Map<String, Object>> communities = new HashMap<>();
        communities.put("communities", community);
        users.document(user.getUid()).set(communities, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.getException() != null) {
                        Toasty.error(getActivity(), task.getException().getMessage(), Toasty.LENGTH_LONG, true).show();
                        return;
                    }
                    Toasty.info(getActivity(), "Congratulations! Please wait for the adviser\'s confirmation.", Toasty.LENGTH_LONG, true).show();

                    CollectionReference adviser_notice = FirebaseFirestore.getInstance().collection("Adviser_Notice");
                    String docId = user.getUid() + "****" + article.getTitle();
                    Map<String, Object> notice = new HashMap<>();
                    notice.put("community", article.getTitle());
                    notice.put("isAllCommunity", false);
                    notice.put("type", "request");
                    long tsLong = System.currentTimeMillis()/1000;
                    String ts = Long.toString(tsLong);
                    notice.put("timestamp", ts);
                    notice.put("uid", user.getUid());
                    adviser_notice.add(notice);
                });
    }


}

