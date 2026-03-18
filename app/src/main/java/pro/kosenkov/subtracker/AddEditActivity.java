package pro.kosenkov.subtracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.content.Context;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LangHelper.wrap(base));
    }

    private EditText etName, etStart, etTrialEnd, etPrice;
    private RadioGroup rgCycle;
    private RadioButton rbMonthly, rbYearly;

    private final Calendar calStart = Calendar.getInstance();
    private final Calendar calEnd   = Calendar.getInstance();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private DbHelper db;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db       = DbHelper.get(this);
        etName   = findViewById(R.id.et_name);
        etStart  = findViewById(R.id.et_start);
        etTrialEnd = findViewById(R.id.et_trial_end);
        etPrice  = findViewById(R.id.et_price);
        rgCycle  = findViewById(R.id.rg_cycle);
        rbMonthly = findViewById(R.id.rb_monthly);
        rbYearly  = findViewById(R.id.rb_yearly);

        // Set default dates
        calEnd.add(Calendar.DAY_OF_MONTH, 30);
        etStart.setText(fmt.format(calStart.getTime()));
        etTrialEnd.setText(fmt.format(calEnd.getTime()));

        etStart.setFocusable(false);
        etTrialEnd.setFocusable(false);
        etStart.setOnClickListener(v -> pickDate(calStart, etStart));
        etTrialEnd.setOnClickListener(v -> pickDate(calEnd, etTrialEnd));

        editId = getIntent().getLongExtra("id", -1);
        if (editId != -1) {
            setTitle(R.string.title_edit);
            Subscription s = db.getById(editId);
            if (s != null) {
                etName.setText(s.getName());
                etPrice.setText(String.valueOf((int) s.getPrice()));
                calStart.setTimeInMillis(s.getStartDate());
                calEnd.setTimeInMillis(s.getTrialEndDate());
                etStart.setText(fmt.format(calStart.getTime()));
                etTrialEnd.setText(fmt.format(calEnd.getTime()));
                if ("yearly".equals(s.getBillingCycle())) rbYearly.setChecked(true);
            }
        } else {
            setTitle(R.string.title_add);
        }

        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> save());
    }

    private void pickDate(Calendar cal, EditText target) {
        new DatePickerDialog(this, (view, y, m, d) -> {
            cal.set(y, m, d, 23, 59, 59);
            target.setText(fmt.format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        if (name.isEmpty()) { etName.setError(getString(R.string.err_name)); return; }
        if (priceStr.isEmpty()) { etPrice.setError(getString(R.string.err_price_empty)); return; }

        double price;
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException e) { etPrice.setError(getString(R.string.err_price_invalid)); return; }

        Subscription s = new Subscription();
        s.setName(name);
        s.setStartDate(calStart.getTimeInMillis());
        s.setTrialEndDate(calEnd.getTimeInMillis());
        s.setPrice(price);
        s.setBillingCycle(rbYearly.isChecked() ? "yearly" : "monthly");

        if (editId != -1) { s.setId(editId); db.update(s); }
        else db.insert(s);

        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
