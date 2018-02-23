# BubbledFastScroller

RecyclerView Fast Scroller with a bubble indicator

![demo gif](https://raw.githubusercontent.com/adalpari/BubbledFastScroller/master/media/demo.gif)

# How to use

## 1.- Add the library into gradle

[![](https://jitpack.io/v/adalpari/BubbledFastScroller.svg)](https://jitpack.io/#adalpari/BubbledFastScroller)


```
allprojects {
  repositories {
  ...
  maven { url 'https://jitpack.io' }
  }
}
```

```
dependencies {
  implementation 'com.github.adalpari:BubbledFastScroller:1.1'
}
```

## 2.- Add the BubbledFastScrollerRecyclerViewHandler into your layout. This View replace the RecyclerView you are using.

```xml
<adalpari.github.com.bubbledfastscroller.BubbledFastScrollerRecyclerViewHandler
        android:id="@+id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        custom:bfs_thumb_color="#494949"
        custom:bfs_bubble_color="#909090"
        custom:bfs_text_color="#ffffff"/>
```

_custom:bfs_thumb_color is the color of the scroll bar

_custom:bfs_bubble_color is the background color of the info bubble

_custom:bfs_text_color is the text color of the info bubble

Don't forget to add the custom scheem in the top paent view _xmlns:custom="http://schemas.android.com/apk/res-auto"_

## 3.- Get the BubbledFastScrollerRecyclerViewHandler in your class and add the Adapter and LayoutManager

```java
BubbledFastScrollerRecyclerViewHandler bubbledFastScrollerRecyclerViewHandler = findViewById(R.id.recycler_view);
bubbledFastScrollerRecyclerViewHandler.setAdapter(myAdapter);

RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
bubbledFastScrollerRecyclerViewHandler.setLayoutManager(layoutManager);
```

## 4.- Inside your Adapter, implement the SectionTitleProvider

```java
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements SectionTitleProvider {
...

@Override
public String getPositionTitle(int position) {
    String item = items.get(position);
    // return the String you want to show inside the bubble when the list reach the position
}

If you don'n implement SectionTitleProvider, _BubbledFastScroller_ will work without show any bubble at all. An error message in logcal will be shown: 

_E/BubbledFastScroller: RecyclerView.Adapter should implement SectionTitleProvider in order to show section bubble_

...
}
```

## 5.- Add onDestoy call

In order to avoid memory leaks, call _bubbledFastScrollerRecyclerViewHandler.onDestroy()_ when your activity is destroyed.

```java
@Override
protected void onDestroy() {
    bubbledFastScrollerRecyclerViewHandler.onDestroy();
    super.onDestroy();
}
```

## Important
You can get the embedded _RecyclerView_ from the _BubbledFastScrollerRecyclerViewHandler_. But, if you want to subscribe to _OnScrollListener_, __don't do it directly__, use _setOnScrolledListener(...)_ instead.

```java
RecyclerView recyclerView = bubbledFastScrollerRecyclerViewHandler.getRecyclerView();

...

bubbledFastScrollerRecyclerViewHandler.setOnScrolledListener(new RecyclerView.OnScrollListener() {
    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        // your code
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        // your code
    }
});
```
