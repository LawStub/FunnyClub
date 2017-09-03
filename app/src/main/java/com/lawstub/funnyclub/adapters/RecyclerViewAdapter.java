package com.lawstub.funnyclub.adapters;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.lawstub.funnyclub.R;
import com.lawstub.funnyclub.activities.CommentsActivity;
import com.lawstub.funnyclub.animator_utlis.PositionEvaluator;
import com.lawstub.funnyclub.fragments.FunnyPageFragment;
import com.lawstub.funnyclub.javabeans.Picture;
import com.lawstub.funnyclub.javabeans.Position;
import com.lawstub.funnyclub.utils.GetTabLayoutView;
import com.lawstub.funnyclub.utils.ShowStorePopupWindow;
import com.lawstub.funnyclub.utils.TintDrawableTransformer;
import com.youth.banner.Banner;
import com.youth.banner.loader.ImageLoader;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * 搞笑图片页面recyclerview 适配器
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements View.OnClickListener{

    public static final String TAG = "RecyclerViewAdapter";

    private List<String> mImages;
    private List<Picture> mPictures;

    private Fragment mFragment;

    private Map<Integer,Boolean> likedMap;

    private int mCount;

    //播放gif监听
    private OnGifPlayListener mListener;
    //加载更多图片监听
    private OnLoadMoreListener mOnLoadMoreListener;

    /**
     * 由于构造器传来了两个数据源，因此通过position获取list的数据时，要注意实际的位置；
     * @param images banner轮播图数据，占用一个position位置
     * @param pictures 图片集
     * @param fragment 用于Glide管理其生命周期
     */
    public RecyclerViewAdapter(List<String> images, List<Picture> pictures, FunnyPageFragment fragment){
        mImages = images;
        mPictures = pictures;
        mFragment = fragment;
        likedMap = new HashMap<>();
        Log.d(TAG,"RecyclerViewAdapter构造器 list.size=" + mPictures.size());
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG,"onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.funny_page_item_view,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.plusOne.clearAnimation();
        Log.d(TAG,"onBindHolder position=" + position);
        if(!likedMap.containsKey(position)){
            likedMap.put(position,false);
        }
        if(position==0){
            setViewVisible(holder.bannerLayout);
            setViewGone(holder.pictureLayout,holder.loadMore,holder.showGIF);
            holder.banner.setDelayTime(3000).setImages(mImages).setImageLoader(new GlideImageLoader()).start();
        }else {
            setViewVisible(holder.pictureLayout);
            setViewGone(holder.bannerLayout,holder.loadMore,holder.showGIF,holder.gif,holder.picture,
                    holder.loadCompletly,holder.progressBar);
            setViewInvisible(holder.bottom);
            holder.picture.setImageDrawable(null);
            holder.loadCompletly.setTag("not");
            holder.like.setOnClickListener(new LikedImageOnClickListenner(holder.likeImage,holder.plusOne,holder.likedCount,position));
            Drawable mdrawable = ContextCompat.getDrawable(mFragment.getContext(),R.mipmap.store);
            holder.storeImage.setImageDrawable(mdrawable);
            holder.store.setOnClickListener(new StoreOnClickListener(holder.storeImage));
            holder.comment.setOnClickListener(new CommentOnClickListener());
            if(likedMap.get(position)){
                holder.likeImage.setImageDrawable(getLikedDrawble(mFragment.getContext(),R.color.gold));
                Log.i(TAG,"like");
            }else {
                Drawable drawable = ContextCompat.getDrawable(mFragment.getContext(),R.mipmap.like);
                holder.likeImage.setImageDrawable(drawable);
            }
            final Picture picture = mPictures.get(position-1);
            holder.title.setText(picture.getTitle());
            String uri = picture.getUri();
            holder.picture.setTag(uri);
            //如果时gif图片,显示gif,隐藏picture
            if(uri.endsWith(".gif")){
                Log.d(TAG,"loadGifAsBitmap position="+ position);
                Log.d(TAG,"loadGifAsBitmap uri="+ picture.getUri());
                setViewVisible(holder.picture);
                loadImageByGlideAsBitmap(picture,holder);
                holder.showGIF.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mListener != null) mListener.onPlay(position,mPictures);
                        setViewVisible(holder.gif);
                        loadImageByGlideAsGif(picture,holder);
                        setViewInvisible(holder.showGIF);
                    }
                });
            }else {
                setViewVisible(holder.picture);
                loadImageByGlideAsBitmap(picture,holder);
            }
        }
        if(position == 3) holder.banner.stopAutoPlay();
        if(position == mPictures.size() && position > 0){
            holder.loadMore.setAlpha(0.5F);
            setViewVisible(holder.loadMore);
            holder.loadMore.setClickable(true);
            holder.loadMore.setText("查看更多");
            holder.loadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnLoadMoreListener != null){
                        mOnLoadMoreListener.onLoad(position,holder.loadMore);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPictures.size()+1;
    }

    @Override
    public void onClick(View v) {
    }

    public static Drawable getLikedDrawble(Context context,int color){
        Drawable drawable = ContextCompat.getDrawable(context,R.mipmap.like);
        Drawable.ConstantState state = drawable.getConstantState();
        Drawable drawable1 = DrawableCompat.wrap(state == null ? drawable : state.newDrawable()).mutate();
        DrawableCompat.setTint(drawable1,ContextCompat.getColor(context,color));
        return drawable1;
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private Banner banner;
        private TextView title;
        private LinearLayout pictureLayout;
        private FrameLayout bannerLayout;
        private ImageView picture;
        private Button loadMore;
        private TextView showGIF;
        private ImageView gif;
        private TextView loadCompletly;
        private ProgressBar progressBar;
        private LinearLayout like;
        private ImageView likeImage;
        private TextView plusOne;
        private TextView likedCount;
        private LinearLayout store;
        private ImageView storeImage;
        private LinearLayout comment;
        private LinearLayout bottom;
        public MyViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG,"new MyViewHolder");
            banner = (Banner)itemView.findViewById(R.id.banner);
            title = (TextView)itemView.findViewById(R.id.title);
            pictureLayout = (LinearLayout)itemView.findViewById(R.id.picture_layout);
            bannerLayout = (FrameLayout)itemView.findViewById(R.id.banner_layout);
            picture = (ImageView)itemView.findViewById(R.id.picture);
            loadMore = (Button)itemView.findViewById(R.id.load_more);
            showGIF = (TextView)itemView.findViewById(R.id.show_gif);
            gif = (ImageView)itemView.findViewById(R.id.gif);
            loadCompletly = (TextView)itemView.findViewById(R.id.load_completly);
            progressBar = (ProgressBar)itemView.findViewById(R.id.picture_loading);
            like = (LinearLayout)itemView.findViewById(R.id.like);
            likeImage = (ImageView)itemView.findViewById(R.id.like_image);
            plusOne = (TextView)itemView.findViewById(R.id.plus_one);
            likedCount = (TextView)itemView.findViewById(R.id.like_count);
            store = (LinearLayout)itemView.findViewById(R.id.store);
            storeImage = (ImageView) itemView.findViewById(R.id.store_image);
            comment = (LinearLayout)itemView.findViewById(R.id.comment);
            bottom = (LinearLayout)itemView.findViewById(R.id.bottom);
        }
    }

    private class GlideImageLoader extends ImageLoader {

        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context)
                    .load(Uri.parse((String)path))
                    .asBitmap()
                    .into(imageView);
        }
    }
    //以bitmap的形式加载静态图片到imageview
    public void loadImageByGlideAsBitmap(final Picture p, final MyViewHolder holder){
        String tag = (String) holder.loadCompletly.getTag();
        if(tag.equals("not")){
            setViewVisible(holder.progressBar);
        }
        Glide.with(mFragment).load(p.getUri())
                .asBitmap()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(R.mipmap.error)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        setViewGone(holder.progressBar);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        Log.d(TAG,"bitmapHeight="+resource.getHeight());
                        Log.d(TAG,"onLoadImage"+holder.picture.getTag().toString() + " " + p.getUri());
                        if(!((String)holder.picture.getTag()).equals(p.getUri())) {
                            Log.d(TAG,"串图");
                            holder.picture.setBackgroundResource(R.mipmap.android);
                            return;
                        }
                        String tag = (String)holder.loadCompletly.getTag();
                        if(resource.getHeight()>700 && tag.equals("not")){
                            Bitmap bitmap = Bitmap.createBitmap(resource,0,0,resource.getWidth(),700);
                            setViewVisible(holder.loadCompletly);
                            holder.loadCompletly.getBackground().setAlpha(100);
                            holder.loadCompletly.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    holder.loadCompletly.setTag("complete");
                                    loadImageByGlideAsBitmap(p,holder);
                                    setViewGone(holder.loadCompletly);
                                }
                            });
                            holder.picture.setImageBitmap(bitmap);
                        }else {
                            holder.picture.setImageBitmap(resource);
                        }
                        setViewGone(holder.progressBar);
                        setViewVisible(holder.bottom);
                        if(p.getUri().endsWith(".gif")) setViewVisible(holder.showGIF);
                    }
                });
    }

    //加载gif
    public void loadImageByGlideAsGif(final Picture p, final MyViewHolder holder){
        Glide.with(mFragment).load(p.getUri())
                .asGif()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GifDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GifDrawable> target,
                                               boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        if(!((String)holder.picture.getTag()).equals(p.getUri())) return true ;
                        Observable.timer(50,TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Long>() {
                                    @Override
                                    public void call(Long aLong) {
                                        setViewInvisible(holder.picture);
                                    }
                                });
                        return false;
                    }
                })
                .error(R.mipmap.error)
               .into(holder.gif);
    }
    //播放gif监听
    public interface OnGifPlayListener{
        void onPlay(int position,List<Picture> pictures);
    }

    public void setOnGifPlayListener(OnGifPlayListener onGifPlayListener){
        mListener = onGifPlayListener;
    }


    public interface OnLoadMoreListener{
        void onLoad(int position,Button info);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener){
        if(listener != null){
            mOnLoadMoreListener = listener;
        }
    }

   void setViewInvisible(View...views){
       for(int i=0;i<views.length;i++){
           views[i].setVisibility(View.INVISIBLE);
       }
   }

    void setViewVisible(View...views){
        for(int i=0;i<views.length;i++){
            views[i].setVisibility(View.VISIBLE);
        }
    }

    void setViewGone(View...views){
        for(int i=0;i<views.length;i++){
            views[i].setVisibility(View.GONE);
        }
    }

    /**
     * 收藏图标点击监听器
     */
    class StoreOnClickListener implements View.OnClickListener{

        private ImageView mStoreImage;

        public StoreOnClickListener(ImageView storeImage){
            mStoreImage = storeImage;
        }

        @Override
        public void onClick(final View v) {
            //获得收藏图标的屏幕坐标
            final int[] locations = new int[2];
            mStoreImage.getLocationOnScreen(locations);
            //取得收藏tabview的实例
            final View view = GetTabLayoutView.getTabLayoutChildViewAtIndex(mFragment.getActivity(),3);
            final int[] tabLocactions = new int[2];
            //取得导航栏收藏按钮屏幕坐标
            view.getLocationOnScreen(tabLocactions);
            int tabViewWidth = view.getWidth();
            final int tabViewHeight = view.getHeight();
            int storeImageWidth = mStoreImage.getWidth();
            int storeImageHeight = mStoreImage.getHeight();
            Log.d(TAG,"tab layout location =" + tabLocactions[0] + " " + tabLocactions[1]);
            Log.d(TAG,"locations ="+locations[0] +" " + locations[1]);
            //在收藏图标的位置创建一个同样的imageview;
            final ImageView imageView = new ImageView(mFragment.getActivity());
            imageView.setImageDrawable(TintDrawableTransformer.getLikedDrawble(
                    mFragment.getContext(),R.color.gold,R.mipmap.store,null));
            //取得windowmanager 用于把imageView添加到window;
            final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(mStoreImage.getWidth(),mStoreImage.getHeight(),0,0, PixelFormat.TRANSPARENT);
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
            layoutParams.gravity = Gravity.LEFT|Gravity.TOP;
            layoutParams.x =locations[0];
            layoutParams.y=locations[1];
            final WindowManager manager = mFragment.getActivity().getWindowManager();
            manager.addView(imageView,layoutParams);
            //由于imageview初始化是visibility是gone,取不到屏幕的坐标，所以辗转到通过反射拿到tabview的实例，取得坐标，也许可以通过setOnlayoutChangedListener获得
            //不过通过反射取得听起来高大上些
            final ImageView storeImageAtTablayout = (ImageView) (mFragment.getActivity().findViewById(R.id.store_image_at_tablayout));
            //记录imageView位移的两个坐标
            final Position startPosition = new Position(locations[0],locations[1]);
            final Position endPosition = new Position(tabLocactions[0]+tabViewWidth/2-storeImageWidth/2,0);
            //属性动画，通过不断改变x y 的值 改变imageview的坐标，达到收藏图标向按钮移动的动画效果
            final ValueAnimator animator = ValueAnimator.ofObject(new PositionEvaluator(),startPosition,endPosition);
            animator.setDuration(1500);
            animator.setInterpolator(new AccelerateInterpolator(0.2f));
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                private float lastFraction;
                private float currFraction;
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //取得属性动画的各种属性，计算下一次位移的值，有一定的误差，但是可以忽略不算
                    currFraction = animation.getAnimatedFraction();
                    float diff = currFraction - lastFraction;
                    float nextFraction = currFraction + diff;
                    Log.i(TAG,"next fraction=" + nextFraction);
                    int deltaX =  endPosition.getX() - startPosition.getX();
                    int deltaY = endPosition.getY() - startPosition.getY();
                    Log.i(TAG,"deltaY="+deltaY);
                    int[] locations = new int[2];
                    //由于tablayout的位置在动画过程中会变化，动态获得收藏按钮的坐标
                    view.getLocationOnScreen(locations);
                    Log.i(TAG,"new tab location = " + locations[0] + " " +locations[1]);
                    Position position = (Position)animation.getAnimatedValue();
                    int nextX = startPosition.getX() + (int)(nextFraction*deltaX);
                    int nextY = startPosition.getY() + (int)(nextFraction*deltaY);
                    Log.i(TAG,"startPosition=" + startPosition.getY());
                    Log.i(TAG,"nextY=" + nextY);
                    //防止位移越界
                    if(nextY < locations[1]+tabViewHeight/2){
                        Log.i(TAG,"onAnimator cancel" + nextY + " " + (locations[1]+tabViewHeight/2));
                        layoutParams.y= locations[1]+tabViewHeight/2;
                        layoutParams.x = position.getX();
                        manager.updateViewLayout(imageView,layoutParams);
                        manager.removeView(imageView);
                        storeImageAtTablayout.setVisibility(View.VISIBLE);
                        animator.cancel();
                        //导航栏收藏图标消失动画
                        ScaleAnimation scaleAnimation = new ScaleAnimation(1f,2f,1f,2f);
                        AlphaAnimation alphaAnimation = new AlphaAnimation(1f,0f);
                        AnimationSet animationSet = new AnimationSet(true);
                        animationSet.setDuration(500);
                        animationSet.addAnimation(alphaAnimation);
                        animationSet.addAnimation(scaleAnimation);
                        animationSet.setInterpolator(new AnticipateInterpolator());
                        animationSet.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                storeImageAtTablayout.setVisibility(View.GONE);
                                ShowStorePopupWindow.showPopupWindow(mFragment.getContext(),view,"收藏成功^_^");
                                v.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ShowStorePopupWindow.canclePopupWindow();
                                    }
                                },1000);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        storeImageAtTablayout.startAnimation(animationSet);
                    }else {
                        layoutParams.y = position.getY();
                        layoutParams.x = position.getX();
                        manager.updateViewLayout(imageView,layoutParams);
                    }
                    lastFraction = currFraction;
                }
            });
            animator.start();
            mStoreImage.setImageDrawable(TintDrawableTransformer.getLikedDrawble(
                    mFragment.getContext(),R.color.gold,R.mipmap.store,null));
            v.setClickable(false);
        }
    }

    //评论点击监听
    private class CommentOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mFragment.getContext(), CommentsActivity.class);
            mFragment.getActivity().startActivity(intent);
        }
    }

    //点赞监听器
    class LikedImageOnClickListenner implements View.OnClickListener{

        private ImageView mImageView;
        private int mPosition;
        private TextView mPlusOne;
        private TextView count;

        public LikedImageOnClickListenner(ImageView iv,TextView tv,TextView count,int position){
            super();
            mImageView = iv;
            mPosition = position;
            mPlusOne = tv;
            this.count = count;
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG,"onClick");
            mImageView.setImageDrawable(TintDrawableTransformer.getLikedDrawble(
                    mFragment.getContext(),R.color.gold,R.mipmap.like,null));
            likedMap.remove(mPosition);
            likedMap.put(mPosition,true);
            setViewVisible(mPlusOne);
            mPlusOne.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    float currTranslationY = mPlusOne.getTop();
                    Log.d(TAG,"translationY="+ currTranslationY);
                    TranslateAnimation translateAnimation = new TranslateAnimation(0f,0f,0f,-100f);
                    AlphaAnimation alphaAnimation = new AlphaAnimation(1f,0f);
                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.addAnimation(alphaAnimation);
                    animationSet.addAnimation(translateAnimation);
                    animationSet.setDuration(1000);
                    animationSet.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            setViewGone(mPlusOne);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    mPlusOne.startAnimation(animationSet);
                }
            });
            v.setClickable(false);
            mCount++;
            count.setText(""+mCount);
        }
    }
}
