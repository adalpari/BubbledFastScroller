package adalpari.github.com.example;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Adalberto Plaza on 17/02/2018.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<String> items;

    public MyAdapter(List<String> items) {
        this.items = items;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String itemName = items.get(position);
        holder.refreshViewHolder("Title: " + itemName, "Subtitle: " + itemName);
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        } else {
            return items.size();
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvSubtitle;

        MyViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.title);
            tvSubtitle = view.findViewById(R.id.subtitle);
        }

        void refreshViewHolder(String title, String subtitle) {
            tvTitle.setText(title);
            tvSubtitle.setText(subtitle);
        }
    }

    public String getIdentifierFrom(int position) {
        String item = items.get(position);
        String[] tokens = item.split("_");
        return tokens[0];
    }
}