package adalpari.github.com.example;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Adalberto Plaza on 17/02/2018.
 */

public class BubbledFastScrollerRecyclerViewHandler extends RelativeLayout {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ImageView imageThumb;
    private TextView textBubble;

    private volatile boolean manuallyChangingPosition = false;
    private volatile int oldScrollState = RecyclerView.SCROLL_STATE_IDLE;
    private volatile boolean hidingThumb = true;

    private AnimatorSet animShow;
    private AnimatorSet animHide;

    private int thumbColor = Color.GRAY;
    private int bubbleColor = Color.GRAY;
    private int bubbleTextColor = Color.WHITE;

    private FastScrollerInfoProvider fastScrollerInfoProvider;

    public interface FastScrollerInfoProvider {
        String getPositionTitle(int position);
    }

    public BubbledFastScrollerRecyclerViewHandler(Context context) {
        super(context);
        initViews(context, null);
    }

    public BubbledFastScrollerRecyclerViewHandler(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
    }

    public BubbledFastScrollerRecyclerViewHandler(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context, attrs);
    }

    public BubbledFastScrollerRecyclerViewHandler(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews(context, attrs);
    }

    private void initViews(Context context, AttributeSet attrs) {
        initAnimations(context);
        initColors(context, attrs);
        initRecyclerView(context);
        initRecyclerViewListener();
        initThumb(context);
        initBubble(context);
        initThumbListener();
    }

    private void initAnimations(Context context) {
        animShow = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.show);
        animShow.setTarget(imageThumb);
        animHide = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.hide);
        animHide.setStartDelay(1000);
        animHide.setTarget(imageThumb);
    }

    private void initColors(Context context, AttributeSet attrs) {
        if (context != null && attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BubbledFastScroller);

            try {
                thumbColor = a.getColor(R.styleable.BubbledFastScroller_bfs_thumb_color, Color.GRAY);
                bubbleColor = a.getColor(R.styleable.BubbledFastScroller_bfs_bubble_color, Color.GRAY);
                bubbleTextColor = a.getColor(R.styleable.BubbledFastScroller_bfs_text_color, Color.WHITE);
            } catch (Throwable throwable) {
            } finally {
                a.recycle();
            }
        }
    }

    //TODO: check if one can pass the attributes (check with layoutmanager)
    private void initRecyclerView(Context context) {
        this.recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.addView(recyclerView);
    }

    private void initRecyclerViewListener() {
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newScrollState) {
                if (newScrollState == RecyclerView.SCROLL_STATE_IDLE && oldScrollState != RecyclerView.SCROLL_STATE_IDLE) {
                    hideScroll();
                } else if (newScrollState != RecyclerView.SCROLL_STATE_IDLE && oldScrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    showScroll(false);
                }
                oldScrollState = newScrollState;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!manuallyChangingPosition) {
                    updateHandlePosition(recyclerView);
                }
            }
        });
    }

    private void initThumb(Context context) {
        this.imageThumb = new ImageView(context);
        imageThumb.setLayoutParams(createThumbLayoutParams(context));
        imageThumb.setVisibility(View.GONE);

        GradientDrawable thumbBackground = getThumbBackground();
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            imageThumb.setBackgroundDrawable(thumbBackground);
        } else {
            imageThumb.setBackground(thumbBackground);
        }

        this.addView(imageThumb);
    }

    private RelativeLayout.LayoutParams createThumbLayoutParams(Context context) {
        int width = context.getResources().getDimensionPixelOffset(R.dimen.thumb_width);
        int height = context.getResources().getDimensionPixelOffset(R.dimen.thumb_height);
        final RelativeLayout.LayoutParams layoutParams = new LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        int marginInPx = context.getResources().getDimensionPixelOffset(R.dimen.thumb_margin);
        layoutParams.setMargins(0, 0, marginInPx, 0);

        return layoutParams;
    }

    private GradientDrawable getThumbBackground() {
        GradientDrawable roundedBackground = new GradientDrawable();
        roundedBackground.setShape(GradientDrawable.RECTANGLE);
        roundedBackground.setCornerRadii(new float[] { 20, 20, 20, 20, 20, 20, 20, 20 });
        roundedBackground.setColor(thumbColor);

        return roundedBackground;
    }

    private void initBubble(Context context) {
        int paddingInPx = context.getResources().getDimensionPixelOffset(R.dimen.bubble_padding);

        textBubble = new TextView(context);

        textBubble.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
        textBubble.setGravity(Gravity.RIGHT);
        textBubble.setLayoutParams(createBubbleLayoutParams(context));
        textBubble.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textBubble.setTextColor(bubbleTextColor);
        textBubble.setVisibility(View.GONE);

        GradientDrawable roundedBackground = getBubbleBackground();
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            textBubble.setBackgroundDrawable(roundedBackground);
        } else {
            textBubble.setBackground(roundedBackground);
        }

        this.addView(textBubble);
    }

    private RelativeLayout.LayoutParams createBubbleLayoutParams(Context context) {
        final RelativeLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        int marginInPx = context.getResources().getDimensionPixelOffset(R.dimen.bubble_margin);
        layoutParams.setMargins(0, 0, marginInPx, 0);

        return layoutParams;
    }

    private GradientDrawable getBubbleBackground() {
        GradientDrawable roundedBackground = new GradientDrawable();
        roundedBackground.setShape(GradientDrawable.RECTANGLE);
        roundedBackground.setCornerRadii(new float[] { 20, 20, 20, 20, 0, 0, 20, 20 });
        roundedBackground.setColor(bubbleColor);

        return roundedBackground;
    }

    private void initThumbListener() {
        imageThumb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        showScroll(true);
                        manuallyChangingPosition = true;
                        float relativePos = getRelativeTouchPosition(event);
                        setScrollerPosition(relativePos);
                        setRecyclerViewPosition(relativePos);
                        return true;
                    case MotionEvent.ACTION_UP:
                        hideScroll();
                        manuallyChangingPosition = false;
                        return true;
                }
                return false;
            }


        });
    }

    private void showScroll(boolean showBubble) {
        showThumb();
        if (showBubble) {
            showBubble();
        }
    }

    private void hideScroll() {
        hideThumb();
        hideBubble();
        clearBubble();
    }

    private void showThumb() {
        if (imageThumb != null && hidingThumb) {
            hidingThumb = false;
            imageThumb.setVisibility(View.VISIBLE);
            animHide.cancel();
            animShow.start();
        }
    }

    private void hideThumb() {
        if (imageThumb != null && !hidingThumb) {
            hidingThumb = true;
            animShow.cancel();
            animHide.start();
        }
    }

    private void showBubble() {
        if (fastScrollerInfoProvider != null && textBubble != null) {
            String text = fastScrollerInfoProvider.getPositionTitle(findFirstVisibleItemPosition());
            textBubble.setVisibility(View.VISIBLE);
            textBubble.setText(text);
        }
    }

    private int findFirstVisibleItemPosition() {
        final View child = findOneVisibleChild(0, layoutManager.getChildCount(), false, true);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }

    View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
            boolean acceptPartiallyVisible) {
        OrientationHelper helper;
        if (layoutManager.canScrollVertically()) {
            helper = OrientationHelper.createVerticalHelper(layoutManager);
        } else {
            helper = OrientationHelper.createHorizontalHelper(layoutManager);
        }

        final int start = helper.getStartAfterPadding();
        final int end = helper.getEndAfterPadding();
        final int next = toIndex > fromIndex ? 1 : -1;
        View partiallyVisible = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child = layoutManager.getChildAt(i);
            final int childStart = helper.getDecoratedStart(child);
            final int childEnd = helper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child;
                    } else if (acceptPartiallyVisible && partiallyVisible == null) {
                        partiallyVisible = child;
                    }
                } else {
                    return child;
                }
            }
        }
        return partiallyVisible;
    }

    private void hideBubble() {
        if (textBubble != null) {
            textBubble.setVisibility(View.GONE);
        }
    }

    private void clearBubble() {
        textBubble.setText("");
    }

    private void setRecyclerViewPosition(float relativePos) {
        if (recyclerView == null) {
            return;
        }

        int itemCount = recyclerView.getAdapter().getItemCount();
        int targetPos = (int) getValueInRange(0, itemCount - 1, (int) (relativePos * (float) itemCount));

        recyclerView.scrollToPosition(targetPos);
    }

    private static float getValueInRange(float min, float max, float value) {
        float minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    private float getRelativeTouchPosition(MotionEvent event) {
        float yInParent = event.getRawY() - getViewRawY(imageThumb);
        return yInParent / (recyclerView.getHeight() - imageThumb.getHeight());
    }

    private static float getViewRawY(View view) {
        int[] location = new int[2];
        location[0] = 0;
        location[1] = (int) view.getY();
        ((View) view.getParent()).getLocationInWindow(location);
        return location[1];
    }

    private void updateHandlePosition(RecyclerView rv) {
        int offset = rv.computeVerticalScrollOffset();
        int extent = rv.computeVerticalScrollExtent();
        int range = rv.computeVerticalScrollRange();
        float relativePos = offset / (float) (range - extent);
        setScrollerPosition(relativePos);
    }

    private void setScrollerPosition(float relativePos) {
        float max = recyclerView.getHeight() - imageThumb.getHeight();
        float value = relativePos * recyclerView.getHeight() - imageThumb.getHeight();
        imageThumb.setY(getValueInRange(0, max, value));
        textBubble.setY(getValueInRange(0, max, value));
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        recyclerView.setLayoutManager(layoutManager);
        this.layoutManager = layoutManager;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setFastScrollerInfoProvider(FastScrollerInfoProvider fastScrollerInfoProvider) {
        this.fastScrollerInfoProvider = fastScrollerInfoProvider;
    }

    public void onDestroy() {
        recyclerView = null;
        imageThumb = null;
        textBubble = null;
        fastScrollerInfoProvider = null;
    }
}

