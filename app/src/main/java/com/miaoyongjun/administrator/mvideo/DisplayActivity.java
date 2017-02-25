package com.miaoyongjun.administrator.mvideo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.miaoyongjun.mdragvideo.MVideo;
import com.miaoyongjun.mdragvideo.media.IjkVideoView;

import java.util.ArrayList;
import java.util.List;

public class DisplayActivity extends AppCompatActivity {
    RecyclerView mRecyclerView;
    String videoPath = "http://bmob-cdn-982.b0.upaiyun.com/2017/02/23/266454624066f2b680707492a0664a97.mp4";
    String imagePath;
    List<String> imageUrls;
    int activityPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_display);
        activityPosition = getIntent().getIntExtra("position", 0);
        switch (activityPosition) {
            case 0:
                imagePath = "http://bmob-cdn-982.b0.upaiyun.com/2017/02/24/98754a6a401d5c48806b2b3863e32bed.jpg";
                break;
            case 1:
                imagePath = "http://bmob-cdn-982.b0.upaiyun.com/2017/02/24/98754a6a401d5c48806b2b3863e32bed.jpg";
                break;
            case 2:
                imagePath = "http://bmob-cdn-982.b0.upaiyun.com/2017/02/24/f387251e4038bf4380169a6c5e5d64f9.jpg";
                break;
            case 3:
                imagePath = "http://bmob-cdn-982.b0.upaiyun.com/2017/02/24/f387251e4038bf4380169a6c5e5d64f9.jpg";
                break;
        }
        imageUrls = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            imageUrls.add(imagePath);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("朋友圈");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new MainAdapter());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
    }

    public static void newIntent(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity, DisplayActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = null;
            if (activityPosition == 0 || activityPosition == 1) {
                holder = new MyViewHolder(LayoutInflater.from(
                        DisplayActivity.this).inflate(R.layout.item_horizontal, parent,
                        false));
            } else {
                holder = new MyViewHolder(LayoutInflater.from(
                        DisplayActivity.this).inflate(R.layout.item_vertical, parent,
                        false));
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            Glide.with(DisplayActivity.this)
                    .load(imageUrls.get(position))
                    .into(holder.srcImageView);
            holder.srcImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View srcView) {
                    MVideo.getInstance()
                            .setProgressColor(0xFF00FF22)
                            .setPreviewImage(imageUrls.get(position))
                            .setRotateDirection(
                                    (activityPosition == 1 || activityPosition == 3) ?
                                            IjkVideoView.RotateDirection.LEFT :
                                            IjkVideoView.RotateDirection.DEFAULT)
                            .start(DisplayActivity.this, srcView, videoPath);

                }
            });
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView srcImageView;

            public MyViewHolder(View view) {
                super(view);
                srcImageView = (ImageView) view.findViewById(R.id.srcImageView);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
