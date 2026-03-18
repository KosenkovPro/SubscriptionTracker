package pro.kosenkov.subtracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TrialDaysView extends View {

    private static final int TOTAL = 10;
    private static final float GAP = 5f;
    private static final float RADIUS = 5f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int daysRemaining = 10;

    public TrialDaysView(Context ctx) { super(ctx); }
    public TrialDaysView(Context ctx, AttributeSet attrs) { super(ctx, attrs); }

    public void setDaysRemaining(int days) {
        daysRemaining = Math.max(0, Math.min(days, TOTAL));
        invalidate();
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int w = MeasureSpec.getSize(wSpec);
        int sq = (int) ((w - GAP * (TOTAL - 1)) / TOTAL);
        setMeasuredDimension(w, sq + 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float sq = (w - GAP * (TOTAL - 1)) / TOTAL;

        for (int i = 0; i < TOTAL; i++) {
            // i=0 → day 10 from end (green), i=9 → day 1 (red)
            int dayFromEnd = TOTAL - i; // 10 down to 1
            float left = i * (sq + GAP);
            RectF rect = new RectF(left, 1, left + sq, sq + 1);

            if (dayFromEnd > daysRemaining) {
                // unused / past days → grey
                paint.setColor(Color.parseColor("#3A3A3C"));
            } else {
                // gradient green→red: t=0 at day10, t=1 at day1
                float t = (TOTAL - dayFromEnd) / (float) (TOTAL - 1);
                int r = (int) (52  + t * (255 - 52));
                int g = (int) (199 + t * (59  - 199));
                int b = (int) (89  + t * (48  - 89));
                paint.setColor(Color.rgb(r, g, b));
            }
            canvas.drawRoundRect(rect, RADIUS, RADIUS, paint);
        }
    }
}
