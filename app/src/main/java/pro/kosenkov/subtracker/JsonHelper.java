package pro.kosenkov.subtracker;

import android.content.Context;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonHelper {

    /** Serialise all subscriptions to JSON and write to the given Uri. */
    public static void exportToUri(Context ctx, List<Subscription> subs, Uri uri) throws Exception {
        JSONArray arr = new JSONArray();
        for (Subscription s : subs) {
            JSONObject obj = new JSONObject();
            obj.put("name",          s.getName());
            obj.put("startDate",     s.getStartDate());
            obj.put("trialEndDate",  s.getTrialEndDate());
            obj.put("price",         s.getPrice());
            obj.put("billingCycle",  s.getBillingCycle());
            arr.put(obj);
        }
        JSONObject root = new JSONObject();
        root.put("version",       1);
        root.put("exportedAt",    System.currentTimeMillis());
        root.put("subscriptions", arr);

        try (OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
            if (os == null) throw new Exception("Не удалось открыть файл для записи");
            os.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
        }
    }

    /** Read JSON from the given Uri and return a list of Subscription objects. */
    public static List<Subscription> importFromUri(Context ctx, Uri uri) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = ctx.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        JSONObject root = new JSONObject(sb.toString());
        JSONArray arr   = root.getJSONArray("subscriptions");

        List<Subscription> result = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            Subscription s = new Subscription();
            s.setName(obj.getString("name"));
            s.setStartDate(obj.getLong("startDate"));
            s.setTrialEndDate(obj.getLong("trialEndDate"));
            s.setPrice(obj.getDouble("price"));
            s.setBillingCycle(obj.getString("billingCycle"));
            result.add(s);
        }
        return result;
    }
}
