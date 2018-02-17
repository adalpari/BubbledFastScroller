package adalpari.github.com.example;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Adalberto Plaza on 17/02/2018.
 */

public class BubbledFastScroller {
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ImageView imageThumb;
    private TextView textBubble;

    private volatile boolean manuallyChangingPosition = false;
    private volatile int oldScrollState = RecyclerView.SCROLL_STATE_IDLE;
    private volatile boolean hidingThumb = true;

    private final AnimatorSet animShow;
    private final AnimatorSet animHide;

    private FastScrollerProvider provider;

    public interface FastScrollerProvider {
        String getPositionTitle(int position);
    }

    public BubbledFastScroller(FastScrollerProvider provider, RecyclerView recyclerView, final ImageView thumb, final TextView bubble) {
        this.provider = provider;
        this.recyclerView = recyclerView;
        this.layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        this.imageThumb = thumb;
        this.textBubble = bubble;

        animShow = (AnimatorSet) AnimatorInflater.loadAnimator(recyclerView.getContext(), R.animator.show);
        animShow.setTarget(imageThumb);
        animHide = (AnimatorSet) AnimatorInflater.loadAnimator(recyclerView.getContext(), R.animator.hide);
        animHide.setStartDelay(1000);
        animHide.setTarget(imageThumb);

        initRecyclerViewListener();
        initThumbListener();
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
            fillBubble();
        }
    }

    private void hideScroll() {
        hideThumb();
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

    private void fillBubble() {
        if (provider != null && textBubble != null) {
            String text = provider.getPositionTitle(layoutManager.findLastCompletelyVisibleItemPosition());
            textBubble.setVisibility(View.VISIBLE);
            textBubble.setText(text);
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

    public void onDestroy() {
        recyclerView = null;
        imageThumb = null;
        textBubble = null;
        provider = null;
    }
}

