package pro.kosenkov.subtracker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SubAdapter adapter;
    private DbHelper db;
    private TextView tvEmpty;
    private RecyclerView rv;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LangHelper.wrap(base));
    }

    // Export: pick save location
    private final ActivityResultLauncher<String> exportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"),
                    uri -> {
                        if (uri == null) return;
                        try {
                            List<Subscription> subs = db.getAll();
                            JsonHelper.exportToUri(this, subs, uri);
                            Toast.makeText(this,
                                    getString(R.string.export_success, subs.size()),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            showError(getString(R.string.error_export, e.getMessage()));
                        }
                    });

    // Import: pick a JSON file
    private final ActivityResultLauncher<String[]> importLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri == null) return;
                        try {
                            List<Subscription> imported = JsonHelper.importFromUri(this, uri);
                            if (imported.isEmpty()) {
                                Toast.makeText(this, R.string.import_empty, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            new AlertDialog.Builder(this)
                                    .setTitle(getString(R.string.import_title, imported.size()))
                                    .setMessage(R.string.import_question)
                                    .setPositiveButton(R.string.import_merge, (d, w) -> {
                                        for (Subscription s : imported) db.insert(s);
                                        load();
                                        Toast.makeText(this,
                                                getString(R.string.import_merged, imported.size()),
                                                Toast.LENGTH_LONG).show();
                                    })
                                    .setNeutralButton(R.string.import_replace, (d, w) -> {
                                        for (Subscription s : db.getAll()) db.delete(s.getId());
                                        for (Subscription s : imported) db.insert(s);
                                        load();
                                        Toast.makeText(this,
                                                getString(R.string.import_replaced, imported.size()),
                                                Toast.LENGTH_LONG).show();
                                    })
                                    .setNegativeButton(R.string.cancel, null)
                                    .show();
                        } catch (Exception e) {
                            showError(getString(R.string.error_import, e.getMessage()));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db      = DbHelper.get(this);
        tvEmpty = findViewById(R.id.tv_empty);
        rv      = findViewById(R.id.recycler);
        FloatingActionButton fab = findViewById(R.id.fab);

        // Push FAB above the navigation bar (handles both button nav and gesture nav)
        ViewCompat.setOnApplyWindowInsetsListener(fab, (v, insets) -> {
            int navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = navBar + getResources().getDimensionPixelSize(R.dimen.fab_margin);
            v.setLayoutParams(lp);
            return insets;
        });

        adapter = new SubAdapter(new SubAdapter.Listener() {
            @Override public void onClick(Subscription s) {
                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                i.putExtra("id", s.getId());
                startActivity(i);
            }
            @Override public boolean onLongClick(Subscription s) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.delete_title, s.getName()))
                        .setPositiveButton(R.string.delete_confirm, (d, w) -> {
                            db.delete(s.getId()); load();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditActivity.class)));
    }

    @Override protected void onResume() { super.onResume(); load(); }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, R.string.menu_export).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, 2, 1, R.string.menu_import).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, 3, 2, R.string.menu_language).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, 4, 3, R.string.menu_about).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 1) {
            if (db.getAll().isEmpty()) {
                Toast.makeText(this, R.string.export_empty, Toast.LENGTH_SHORT).show();
            } else {
                String fname = "subtracker_" +
                        new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) +
                        ".json";
                exportLauncher.launch(fname);
            }
            return true;
        }
        if (id == 2) {
            importLauncher.launch(new String[]{"application/json", "*/*"});
            return true;
        }
        if (id == 3) {
            showLanguageDialog();
            return true;
        }
        if (id == 4) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLanguageDialog() {
        int current = LangHelper.savedIndex(this);
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_language)
                .setSingleChoiceItems(LangHelper.NAMES, current, (dialog, which) -> {
                    dialog.dismiss();
                    String code = LangHelper.CODES[which];
                    LangHelper.apply(this, code);
                    // Recreate to apply new locale across all views
                    recreate();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void load() {
        List<Subscription> list = db.getAll();
        adapter.setItems(list);
        boolean empty = list.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
}
