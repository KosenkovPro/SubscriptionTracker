package pro.kosenkov.subtracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubAdapter extends RecyclerView.Adapter<SubAdapter.VH> {

    interface Listener {
        void onClick(Subscription s);
        boolean onLongClick(Subscription s);
    }

    private List<Subscription> items = new ArrayList<>();
    private final Listener listener;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    SubAdapter(Listener listener) { this.listener = listener; }

    void setItems(List<Subscription> list) { items = list; notifyDataSetChanged(); }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Subscription s = items.get(pos);
        h.tvName.setText(s.getName());
        h.tvPrice.setText(s.getPriceLabel());
        h.tvDates.setText(fmt.format(new Date(s.getStartDate()))
                + " → " + fmt.format(new Date(s.getTrialEndDate())));

        int days = s.getDaysRemaining();
        if (s.isExpired()) {
            h.tvDays.setText("💳 Платная: " + s.getPriceLabel());
            h.tvDays.setTextColor(Color.parseColor("#007AFF"));
            h.daysView.setVisibility(View.GONE);
        } else {
            h.tvDays.setText(days <= 1 ? "Остался 1 день!" : "Осталось дней: " + days);
            h.tvDays.setTextColor(days <= 3 ? Color.parseColor("#FF3B30") :
                    days <= 7 ? Color.parseColor("#FF9F0A") : Color.parseColor("#34C759"));
            h.daysView.setVisibility(View.VISIBLE);
            h.daysView.setDaysRemaining(Math.min(days, 10));
        }

        h.itemView.setOnClickListener(v -> listener.onClick(s));
        h.itemView.setOnLongClickListener(v -> listener.onLongClick(s));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvDates, tvDays;
        TrialDaysView daysView;
        VH(View v) {
            super(v);
            tvName  = v.findViewById(R.id.tv_name);
            tvPrice = v.findViewById(R.id.tv_price);
            tvDates = v.findViewById(R.id.tv_dates);
            tvDays  = v.findViewById(R.id.tv_days);
            daysView = v.findViewById(R.id.days_view);
        }
    }
}
