package adalpari.github.com.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import adalpari.github.com.bubbledfastscroller.BubbledFastScrollerRecyclerViewHandler;

public class MainActivity extends AppCompatActivity implements BubbledFastScrollerRecyclerViewHandler.FastScrollerInfoProvider {

    private BubbledFastScrollerRecyclerViewHandler mBubbledFastScrollerRecyclerViewHandler;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBubbledFastScrollerRecyclerViewHandler = findViewById(R.id.recycler_view);

        List<String> items = createListItems();
        myAdapter = new MyAdapter(items);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mBubbledFastScrollerRecyclerViewHandler.setLayoutManager(mLayoutManager);
        mBubbledFastScrollerRecyclerViewHandler.setAdapter(myAdapter);
        mBubbledFastScrollerRecyclerViewHandler.setFastScrollerInfoProvider(this);
    }

    private List<String> createListItems() {
        List<String> items = new ArrayList<>();

        for (int i=1; i<100; i++) {
            items.add(i + "_Item");
        }

        return items;
    }

    @Override
    protected void onDestroy() {
        mBubbledFastScrollerRecyclerViewHandler.onDestroy();
        super.onDestroy();
    }

    @Override
    public String getPositionTitle(int position) {
        return myAdapter.getIdentifierFrom(position);
    }
}
