package com.lawstub.funnyclub.fragments;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lawstub.funnyclub.R;
import com.lawstub.funnyclub.adapters.RecyclerViewAdapter;
import com.lawstub.funnyclub.javabeans.Picture;
import com.lawstub.funnyclub.javabeans.PictureInfo;
import com.lawstub.funnyclub.utils.GetTabLayoutView;
import com.lawstub.funnyclub.utils.NetworkStateUtil;
import com.lawstub.funnyclub.utils.ShowStorePopupWindow;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SQLQueryListener;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.support.v7.recyclerview.R.styleable.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FunnyPageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,View.OnClickListener {

    //轮播图uri集合
    List<String> images;

    //访问bmob后端云认证密匙
    private static final String BMOB_KEY = "5ade83b03a1f7abdc8e9a63b5f773987";

    //glide 内存缓存大小
    private static final int MEMERY_SIZE = 1024*1024*80;

    private android.support.v7.widget.RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button goToListTopBt;
    private TextView networkInfo;


    //所有图片uri列表
    private List<Picture> pictures = new ArrayList<>();

    //分批加载到recyclerview的图片uri列表
    private List<Picture> adapterPictures = new ArrayList<>();

    //记录adapter数据源已经加载uri数量
    private int itemLoaded = 0;

    RecyclerViewAdapter adapter;

    private boolean isQueryFromBmob = false;
    private boolean isNetworkAvailble = false;
    private boolean isRecyclerViewScrolling = false;

    private static final String TAG = "FunnyPageFragment";
    private static final String NOTIFY = "点击图片可以进入看图模式哦！";
    private static final int DELAY_TO_CANCLE_POPUPWINDOW = 2000;

    //当recyclerview滚动到哪个position时 显示返回顶部按钮
    private static final int WHEN_TO_SHOW_TO_TOP_BT = 7;

    private VelocityTracker velocityTracker;

    private static  final int SPEED = 3000;


    public FunnyPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        images = new ArrayList<>();
        images.add("http://bmob-cdn-13743.b0.upaiyun.com/2017/08/28/9b289298408a4612806e1819da5b3ff4.jpg");
        images.add("http://t1.niutuku.com/190/13/13-117402.jpg");
        images.add("http://pic.58pic.com/58pic/13/87/72/73t58PICjpT_1024.jpg");
        GlideBuilder builder = new GlideBuilder(getContext());
        builder.setMemoryCache(new LruResourceCache(MEMERY_SIZE));
        Bmob.initialize(getContext(),BMOB_KEY);
        isNetworkAvailble = NetworkStateUtil.netState(this.getContext());
        if(NetworkStateUtil.IsMobileNetConnect(this.getContext())){
            Toast.makeText(this.getContext(),"正在使用本地流量观看",Toast.LENGTH_SHORT).show();
        }
        velocityTracker = VelocityTracker.obtain();
        Log.d(TAG,"onCreate"+ "itemLoaded=" + itemLoaded + " " +"isFirst" + isQueryFromBmob);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_funny_page,container,false);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        progressBar = (ProgressBar)view.findViewById(R.id.progressbar);
        goToListTopBt = (Button)view.findViewById(R.id.go_to_top);
        goToListTopBt.setOnClickListener(this);
        goToListTopBt.setAlpha(0.5f);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        networkInfo = (TextView)view.findViewById(R.id.network_info);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.clearAnimation();
        adapter = new RecyclerViewAdapter(images,adapterPictures,FunnyPageFragment.this);
        adapter.setOnGifPlayListener(new FragmentOnGifPlayListener());
        adapter.setOnLoadMoreListener(new MyOnloadMoreListener());
        recyclerView.setAdapter(adapter);
        //监听图片列表滑动状态,gif图滑出屏幕时，取消播放
        recyclerView.addOnScrollListener(new MyOnScrollListener(pictures) );
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(500);
                int y = (int)velocityTracker.getYVelocity();
                Log.i(TAG,"onTouch"+"speed=" + y);
                if(Math.abs(y)>SPEED && isRecyclerViewScrolling){
                    View target =  GetTabLayoutView.getTabLayoutChildViewAtIndex(FunnyPageFragment.this.getActivity(),3);
                    ShowStorePopupWindow.showPopupWindow(FunnyPageFragment.this.getContext(),target,NOTIFY);
                }
                return false;
            }
        });
        //获取bmob云端服务器图片列表
        if(!isQueryFromBmob && isNetworkAvailble){
            progressBar.setVisibility(View.VISIBLE);
            queryFunnyPicturesFromBmob(new OnQueryPicturesFormBmobDoneListener() {
                @Override
                public void onDone(final List<PictureInfo> list) {
                    final List<Picture> queryPictures = new ArrayList<>();
                    handlePictureInfo(list,queryPictures,false);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        Log.d(TAG,"onCreateViewFinish");
        }else {
            networkInfo.setVisibility(View.VISIBLE);
        }
        if(isQueryFromBmob) recyclerView.setVisibility(View.VISIBLE);
        return view;
    }

    //处理queryFunnyPicturesFromBmob返回的list<PictureInfo>
    public void handlePictureInfo(final List<PictureInfo> list, final List<Picture> queryPictures,final boolean isFromSwipeLayoutRefresh){
        if(list.size()>0 ) {
            Observable.range(0, list.size())
                    .concatMap(new Func1<Integer, Observable<PictureInfo>>() {
                        @Override
                        public Observable<PictureInfo> call(Integer integer) {
                            return Observable.just(list.get(list.size() - 1 - integer));
                        }
                    })
                    .concatMap(new Func1<PictureInfo, Observable<Picture>>() {
                        @Override
                        public Observable<Picture> call(PictureInfo pictureInfo) {
                            Gson gson = new Gson();
                            List<Picture> pictures = gson.fromJson(pictureInfo.getUris(), new TypeToken<List<Picture>>() {
                            }.getType());
                            return Observable.from(pictures);
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Picture>() {
                        @Override
                        public void onCompleted() {
                            if(isFromSwipeLayoutRefresh){
                                if(pictures.size() < queryPictures.size()){
                                    int count = queryPictures.size() - pictures.size();
                                    pictures.clear();
                                    adapterPictures.clear();
                                    pictures.addAll(queryPictures);
                                    itemLoaded = 0;
                                    loadData(pictures,adapterPictures);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(getContext(),"已更新"+count + "张图片",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getContext(),"没有更新",Toast.LENGTH_SHORT).show();
                                }
                                swipeRefreshLayout.setRefreshing(false);
                            }else {
                                pictures.addAll(queryPictures);
                                progressBar.setVisibility(View.GONE);
                                isQueryFromBmob = true;
                                loadData(pictures,adapterPictures);
                                adapter.notifyDataSetChanged();
                            }
                            Log.d(TAG, "onCompleted add picture to list" + queryPictures.toString());
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            if(isFromSwipeLayoutRefresh){
                                swipeRefreshLayout.setRefreshing(false);
                            }else {
                                progressBar.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onNext(Picture picture) {
                            queryPictures.add(picture);
                        }
                    });
        }else {
            if(isFromSwipeLayoutRefresh){
                swipeRefreshLayout.setRefreshing(false);
            }else {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG,"图片列表为空");
            }
        }
    }

    // 从Bmob云端服务器获取图片列表
    private List<PictureInfo> queryFunnyPicturesFromBmob(final OnQueryPicturesFormBmobDoneListener listener){
        if(!isNetworkAvailble){
            if(swipeRefreshLayout!=null && swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
            return null;
        }
        final List<PictureInfo> pictureInfos = new ArrayList<>();
        BmobQuery<PictureInfo> bmobQuery = new BmobQuery<>();
        bmobQuery.doSQLQuery("select * from PictureInfo", new SQLQueryListener<PictureInfo>() {
            @Override
            public void done(BmobQueryResult<PictureInfo> bmobQueryResult, BmobException e) {
                if(bmobQueryResult != null){
                    pictureInfos.addAll(bmobQueryResult.getResults());
                    Log.d(TAG,bmobQueryResult.getResults().toString());
                    Log.d(TAG,pictureInfos.toString());
                    if(listener != null && pictureInfos.size()>0){
                        listener.onDone(pictureInfos);
                    }
                }else {
                    Log.d(TAG,"onQueryPicturesFromBmob fail"+e.getMessage());
                }
            }
        });
        return pictureInfos;
    }

    //下拉刷新
    @Override
    public void onRefresh() {
        queryFunnyPicturesFromBmob(new OnQueryPicturesFormBmobDoneListener() {
            @Override
            public void onDone(List<PictureInfo> list) {
                final List<Picture> queryPictures = new ArrayList<>();
                handlePictureInfo(list,queryPictures,true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.go_to_top:
                recyclerView.scrollToPosition(0);
                v.setVisibility(View.GONE);
                break;
        }
    }


    //获取图片列表回调接口
    interface OnQueryPicturesFormBmobDoneListener{
        void onDone(List<PictureInfo> list);
    }



    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        velocityTracker.clear();
        velocityTracker.recycle();
        super.onDestroy();
    }

    //recyclerView滑动监听器
    class MyOnScrollListener extends RecyclerView.OnScrollListener{
        //记录当前屏幕第一个view
        private int lastFirstVisibleItemPosition = -1;
        private int currFirstVisibleItemPosition = -1;

        //记录当前屏幕最后一个view
        private int lastVisibleItemPosition = -1;
        private int currLastVisibleItemPosition = -1;

        private View firstView;
        private View lastView;

        private AlphaAnimation alphaAnimation;

        private List<Picture> mPictures;

        public MyOnScrollListener(List<Picture> pictures){
            super();
            mPictures = pictures;
            alphaAnimation = new AlphaAnimation(1f,0f);
            alphaAnimation.setDuration(3000);
            alphaAnimation.setFillAfter(false);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    goToListTopBt.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager manager = (LinearLayoutManager)recyclerView.getLayoutManager();
            currFirstVisibleItemPosition = manager.findFirstVisibleItemPosition();
            currLastVisibleItemPosition = manager.findLastVisibleItemPosition();
            if(currFirstVisibleItemPosition-1 == lastFirstVisibleItemPosition){
                stopGif(firstView,lastFirstVisibleItemPosition,mPictures);
                Log.d(TAG,"onStopGif"+lastFirstVisibleItemPosition);
            }
            if(lastVisibleItemPosition-1 == currLastVisibleItemPosition){
                stopGif(lastView,lastVisibleItemPosition,mPictures);
            }
            firstView = recyclerView.getChildAt(0);
            lastView = recyclerView.getChildAt(manager.getChildCount()-1);
            lastFirstVisibleItemPosition = currFirstVisibleItemPosition;
            lastVisibleItemPosition = currLastVisibleItemPosition;
            if(manager.findLastVisibleItemPosition() == pictures.size()){
                View childView = manager.getChildAt(manager.getChildCount()-1);
                Button loadMore = (Button) childView.findViewById(R.id.load_more);
                loadMore.setText("木有更多了。。。。。");
                loadMore.setClickable(false);
            }
            if(currFirstVisibleItemPosition >= WHEN_TO_SHOW_TO_TOP_BT){
                goToListTopBt.setVisibility(View.VISIBLE);
                Log.i(TAG,"currPosition=" + "setVisible");
            }else if(currFirstVisibleItemPosition < WHEN_TO_SHOW_TO_TOP_BT ) {
                goToListTopBt.clearAnimation();
                goToListTopBt.setVisibility(View.INVISIBLE);
                Log.i(TAG,"currPosition=" + currFirstVisibleItemPosition);
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState){
                case android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING:
                    if(!alphaAnimation.hasEnded()){
                        alphaAnimation.cancel();
                    }
                    isRecyclerViewScrolling = true;
                    break;
                case android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE:
                    if(goToListTopBt.getVisibility() == View.VISIBLE){
                        goToListTopBt.startAnimation(alphaAnimation);
                    }
                    isRecyclerViewScrolling = false;
                    ShowStorePopupWindow.canclePopupWindow();
                    break;
            }
        }
    }


    void cancelGifPlay(RecyclerView recyclerView,int position,int childPosition,List<Picture> pictures){
        View childView = recyclerView.getChildAt(childPosition);
        ImageView picture = (ImageView)childView.findViewById(R.id.picture);
        final ImageView gif = (ImageView)childView.findViewById(R.id.gif);
        final TextView showGif = (TextView)childView.findViewById(R.id.show_gif);
        if(gif.getVisibility() == View.VISIBLE ){
            Glide.with(getContext())
                    .load(pictures.get(position).getUri())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(picture);
            picture.setVisibility(View.VISIBLE);
            gif.setVisibility(View.INVISIBLE);
            showGif.setVisibility(View.VISIBLE);
        }
    }

    //实现recyclerview的gif播放监听接口
    class FragmentOnGifPlayListener implements RecyclerViewAdapter.OnGifPlayListener{

        @Override
        public void onPlay(int position,List<Picture> pictures) {
            LinearLayoutManager manager = (LinearLayoutManager)recyclerView.getLayoutManager();
            int firstPosition = manager.findFirstVisibleItemPosition();
            int childCount = manager.getChildCount();
            int lastPosition = manager.findLastVisibleItemPosition();
            Log.d(TAG,"OnGifPlayListener childCount=" + childCount +
                    " firstPosition=" + firstPosition +" lastPosition=" + lastPosition);
            if(childCount == 1) return;
            //当点击的gif位于recyclerviewy在当前屏幕的第一位，往后关闭播放中的gif;
            if(position == firstPosition){
                for(int i = 1;i<childCount;i++){
                    stopGif(manager,position+i-1,i,pictures);
                }
            }
            //当点击的gif位于recyclerviewy在当前屏幕的中部位置，往后往后关闭播放中的gif;
            if(position > firstPosition && position < lastPosition){
                for(int i = position-firstPosition+1;i<childCount;i++){
                    stopGif(manager,firstPosition+i-1,i,pictures);
                }
                for(int i = position-firstPosition-1;i>=0;i--){
                    stopGif(manager,firstPosition+i-1,i,pictures);
                }
            }
            //当点击的gif位于recyclerviewy在当前屏幕的最后一位，往前关闭播放中的gif;
            if(position == lastPosition){
                for(int i = lastPosition-firstPosition-1;i>=0;i--){
                    stopGif(manager,position-(childCount-i),i,pictures);
                }
            }
        }
    }

    //停止gif播放
    private void stopGif(LinearLayoutManager manager,int picturesPosition,int childPosition,List<Picture> pictures){
        View childView = manager.getChildAt(childPosition);
        Log.i(TAG,"childView="+childView.toString());
        final ImageView picture = (ImageView) childView.findViewById(R.id.picture);
        if(picture.getVisibility() != View.INVISIBLE) return;
        final ImageView gif = (ImageView)childView.findViewById(R.id.gif);
        final TextView showGif = (TextView)childView.findViewById(R.id.show_gif);
        Glide.with(getContext())
                .load(pictures.get(picturesPosition).getUri())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        picture.setImageBitmap(resource);
                        picture.setVisibility(View.VISIBLE);
                        showGif.setVisibility(View.VISIBLE);
                        gif.setVisibility(View.INVISIBLE);
                    }
                });

    }

    private void stopGif(View childView,int position,List<Picture> pictures){
        if(childView == null || position <=0) return;
        final ImageView picture = (ImageView)childView.findViewById(R.id.picture);
        Log.d(TAG,"onStopGif pictureview visiublity=" + picture.getVisibility());
        Log.d(TAG,"onStopGif childview=" + childView.toString());
        if(picture.getVisibility() == View.VISIBLE) return;
        final ImageView gif = (ImageView)childView.findViewById(R.id.gif);
        final TextView showGif = (TextView)childView.findViewById(R.id.show_gif);
        Glide.with(getContext())
                .load(pictures.get(position-1).getUri())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        picture.setImageBitmap(resource);
                        picture.setVisibility(View.VISIBLE);
                        gif.setVisibility(View.INVISIBLE);
                        showGif.setVisibility(View.VISIBLE);
                    }
                });
    }

    //分批加载图片uri到adapter的数据源
    void loadData(List<Picture> source,List<Picture> target){
        int start = itemLoaded;
        int count = start+15;
        for(int i = start;i<count;i++){
            if(itemLoaded >= source.size()) return;
            target.add(source.get(itemLoaded));
            itemLoaded++;
        }
    }

    class MyOnloadMoreListener implements RecyclerViewAdapter.OnLoadMoreListener{

        @Override
        public void onLoad(int position, Button info) {
            info.setText("正在加载。。。");
            loadData(pictures,adapterPictures);
            adapter.notifyDataSetChanged();
        }
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
