package pro.kosenkov.subtracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import java.util.Locale;

public class LangHelper {

    private static final String PREFS  = "subtracker_prefs";
    private static final String KEY    = "language_code";
    private static final String DEFAULT = "ru";

    // Ordered list matching the dialog shown to the user
    public static final String[] CODES = {
            "en", "zh", "hi", "es", "ar", "fr", "bn", "ru", "pt", "ur", "in"
    };
    public static final String[] NAMES = {
            "English", "中文 (普通话)", "हिन्दी", "Español", "العربية",
            "Français", "বাংলা", "Русский", "Português", "اردو", "Indonesia"
    };

    /** Save chosen language code and return a context with that locale applied. */
    public static Context apply(Context ctx, String langCode) {
        save(ctx, langCode);
        return wrap(ctx, langCode);
    }

    /** Wrap context with the stored locale — call in every Activity.attachBaseContext(). */
    public static Context wrap(Context ctx) {
        return wrap(ctx, getSaved(ctx));
    }

    private static Context wrap(Context ctx, String langCode) {
        Locale locale = toLocale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration(ctx.getResources().getConfiguration());
        config.setLocale(locale);
        return ctx.createConfigurationContext(config);
    }

    public static void save(Context ctx, String langCode) {
        prefs(ctx).edit().putString(KEY, langCode).apply();
    }

    public static String getSaved(Context ctx) {
        return prefs(ctx).getString(KEY, DEFAULT);
    }

    public static int savedIndex(Context ctx) {
        String saved = getSaved(ctx);
        for (int i = 0; i < CODES.length; i++) {
            if (CODES[i].equals(saved)) return i;
        }
        return 7; // default: Russian
    }

    private static Locale toLocale(String code) {
        switch (code) {
            case "zh": return Locale.SIMPLIFIED_CHINESE;
            case "in": return new Locale("in");
            default:   return new Locale(code);
        }
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
