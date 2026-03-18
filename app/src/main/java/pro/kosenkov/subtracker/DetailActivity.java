package pro.kosenkov.subtracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LangHelper.wrap(base));
    }

    private DbHelper db;
    private long subId;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = DbHelper.get(this);
        subId = getIntent().getLongExtra("id", -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Subscription s = db.getById(subId);
        if (s == null) { finish(); return; }

        setTitle(s.getName());

        TextView tvStatus   = findViewById(R.id.tv_status);
        TextView tvStart    = findViewById(R.id.tv_start);
        TextView tvTrialEnd = findViewById(R.id.tv_trial_end);
        TextView tvPrice    = findViewById(R.id.tv_price);
        TextView tvDays     = findViewById(R.id.tv_days);
        TrialDaysView daysView = findViewById(R.id.days_view);

        tvStart.setText("Начало:  " + fmt.format(new Date(s.getStartDate())));
        tvTrialEnd.setText("Конец пробного:  " + fmt.format(new Date(s.getTrialEndDate())));
        tvPrice.setText("Стоимость:  " + s.getPriceLabel());

        int days = s.getDaysRemaining();
        if (s.isExpired()) {
            tvStatus.setText("💳 Активная платная подписка");
            tvStatus.setTextColor(Color.parseColor("#007AFF"));
            tvDays.setText("Бесплатный период закончился");
            tvDays.setTextColor(Color.parseColor("#FF3B30"));
            daysView.setVisibility(View.GONE);
        } else {
            tvStatus.setText("✅ Пробный период активен");
            tvStatus.setTextColor(Color.parseColor("#34C759"));
            tvDays.setText("Осталось дней: " + days);
            tvDays.setTextColor(days <= 3 ? Color.parseColor("#FF3B30") :
                    days <= 7 ? Color.parseColor("#FF9F0A") : Color.parseColor("#34C759"));
            daysView.setVisibility(View.VISIBLE);
            daysView.setDaysRemaining(Math.min(days, 10));
        }

        Button btnEdit   = findViewById(R.id.btn_edit);
        Button btnDelete = findViewById(R.id.btn_delete);

        btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, AddEditActivity.class);
            i.putExtra("id", subId);
            startActivity(i);
        });

        btnDelete.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("Удалить «" + s.getName() + "»?")
                .setPositiveButton("Удалить", (d, w) -> { db.delete(subId); finish(); })
                .setNegativeButton("Отмена", null)
                .show()
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
