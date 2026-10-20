package com.verity.firetv;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String PREFS = "verity_save";
    private static final String[] ITEMS = {"Glitch Eyes", "Rusty Crown", "Void Aura", "Red Smile", "Deep-Sea Hoodie"};
    private static final int[] COSTS = {15, 30, 45, 60, 75};
    private final int bg = Color.rgb(8, 13, 17);
    private final int panel = Color.rgb(19, 29, 32);
    private final int green = Color.rgb(88, 255, 136);
    private SharedPreferences prefs;
    private LinearLayout root, content, header;
    private TextView coinsView, statusView, answerView, avatarView;
    private EditText questionInput;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        buildHome();
    }

    private GradientDrawable rounded(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(18);
        if (stroke != 0) d.setStroke(2, stroke);
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(8, 5, 8, 5);
        return t;
    }

    private Button button(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(17);
        b.setAllCaps(false);
        b.setFocusable(true);
        b.setFocusableInTouchMode(true);
        b.setBackground(rounded(Color.rgb(30, 48, 47), green));
        b.setOnClickListener(v -> action.run());
        b.setPadding(18, 8, 18, 8);
        return b;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(18, 12, 18, 12);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private void startScreen(String title) {
        root = column();
        root.setBackgroundColor(bg);
        setContentView(root);
        header = row();
        TextView heading = text(title, 27, green, true);
        header.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        coinsView = text("◈ " + coins() + " coins", 18, Color.YELLOW, true);
        header.addView(coinsView);
        root.addView(header);
        View line = new View(this);
        line.setBackgroundColor(green);
        root.addView(line, new LinearLayout.LayoutParams(-1, 2));
        ScrollView scroll = new ScrollView(this);
        content = column();
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private int coins() { return prefs.getInt("coins", 0); }
    private void updateCoins() {
        if (coinsView != null) coinsView.setText("◈ " + coins() + " coins");
    }

    private void buildHome() {
        startScreen("VERITY  //  ENTITY 001");
        LinearLayout hero = row();
        hero.setBackground(rounded(panel, green));
        hero.setPadding(18, 12, 18, 12);
        avatarView = text(avatarArt(), 22, green, true);
        avatarView.setGravity(Gravity.CENTER);
        hero.addView(avatarView, new LinearLayout.LayoutParams(0, 160, 1));
        LinearLayout intro = column();
        intro.addView(text("I SEE YOUR QUESTION.", 22, Color.WHITE, true));
        intro.addView(text("Ask anything. Earn coins. Change the entity.", 17, Color.LTGRAY, false));
        intro.addView(text("AI answers need an API key in Settings.", 15, Color.rgb(255, 210, 110), false));
        hero.addView(intro, new LinearLayout.LayoutParams(0, -2, 1));
        content.addView(hero);

        questionInput = new EditText(this);
        questionInput.setSingleLine(false);
        questionInput.setMinLines(2);
        questionInput.setTextSize(20);
        questionInput.setHint("Type your question here...");
        questionInput.setHintTextColor(Color.GRAY);
        questionInput.setTextColor(Color.WHITE);
        questionInput.setPadding(18, 12, 18, 12);
        questionInput.setBackground(rounded(Color.rgb(25, 34, 38), Color.GRAY));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.topMargin = 16;
        content.addView(questionInput, ip);

        LinearLayout actions = row();
        Button ask = button("ASK VERITY", this::askQuestion);
        actions.addView(ask, new LinearLayout.LayoutParams(0, -2, 1));
        Button shop = button("COSMETIC SHOP", this::buildShop);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, -2, 1);
        sp.leftMargin = 10;
        actions.addView(shop, sp);
        content.addView(actions);

        answerView = text("VERITY: Speak your question, human.", 19, Color.WHITE, false);
        answerView.setGravity(Gravity.TOP);
        answerView.setPadding(18, 16, 18, 16);
        answerView.setBackground(rounded(panel, Color.rgb(60, 80, 80)));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, -2);
        ap.topMargin = 14;
        content.addView(answerView, ap);

        statusView = text("Each question earns 10 coins. Purchases and coins are saved on this device.", 14, Color.LTGRAY, false);
        content.addView(statusView);

        LinearLayout bottom = row();
        bottom.addView(button("SETTINGS / API KEY", this::showSettings), new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1);
        bp.leftMargin = 10;
        bottom.addView(button("CUSTOMIZE", this::buildCustomize), bp);
        content.addView(bottom);
        questionInput.requestFocus();
    }

    private String avatarArt() {
        String eye = prefs.getBoolean("owned_0", false) && prefs.getBoolean("equip_0", false) ? "◉" : "●";
        String crown = prefs.getBoolean("equip_1", false) ? "♛ " : "";
        String smile = prefs.getBoolean("equip_3", false) ? "﹏" : "⌣";
        return crown + "┏━━━━━━━━┓\n┃  " + eye + "    " + eye + "  ┃\n┃    ▄    ┃\n┃   " + smile + "   ┃\n┗━━━━━━━━┛";
    }

    private void askQuestion() {
        String q = questionInput.getText().toString().trim();
        if (q.isEmpty()) {
            answerView.setText("VERITY: You must ask something first.");
            questionInput.requestFocus();
            return;
        }
        String key = prefs.getString("api_key", "").trim();
        if (key.isEmpty()) {
            answerView.setText("VERITY: I need an AI API key first. Open SETTINGS / API KEY. You can still explore the shop.");
            return;
        }
        answerView.setText("VERITY is thinking... the pixels are watching.");
        statusView.setText("Connecting to AI...");
        questionInput.setEnabled(false);
        executor.execute(() -> {
            String result;
            try { result = callAI(key, q); }
            catch (Exception e) { result = "I couldn't get an answer. Check your internet and API key, then try again. (" + safeError(e) + ")"; }
            final String answer = result;
            mainHandler.post(() -> {
                questionInput.setEnabled(true);
                answerView.setText("VERITY:\n" + answer);
                int newCoins = coins() + 10;
                prefs.edit().putInt("coins", newCoins).apply();
                updateCoins();
                statusView.setText("+10 coins earned! Total: " + newCoins + ". Coins are awarded for each submitted question.");
                questionInput.requestFocus();
            });
        });
    }

    private String safeError(Exception e) {
        String m = e.getMessage();
        if (m == null) return e.getClass().getSimpleName();
        return m.length() > 100 ? m.substring(0, 100) : m;
    }

    private String callAI(String apiKey, String question) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("https://api.openai.com/v1/responses").openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(20000);
        c.setReadTimeout(30000);
        c.setDoOutput(true);
        c.setRequestProperty("Authorization", "Bearer " + apiKey);
        c.setRequestProperty("Content-Type", "application/json");
        JSONObject body = new JSONObject();
        body.put("model", "gpt-4.1-mini");
        body.put("instructions", "You are Verity, a creepy but friendly fictional blocky Minecraft-inspired entity. Answer the user's question accurately, clearly, and concisely for a general audience. If unsure, say so. Do not pretend to have supernatural powers. Keep answers suitable for teens.");
        body.put("input", question);
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = c.getOutputStream()) { os.write(bytes); }
        int code = c.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        JSONObject response = new JSONObject(sb.toString());
        if (code < 200 || code >= 300) {
            String msg = response.optJSONObject("error") != null
                    ? response.optJSONObject("error").optString("message", "API request failed")
                    : "API request failed with HTTP " + code;
            throw new Exception(msg);
        }
        StringBuilder answer = new StringBuilder();
        JSONArray output = response.optJSONArray("output");
        if (output != null) {
            for (int i = 0; i < output.length(); i++) {
                JSONObject item = output.optJSONObject(i);
                if (item == null) continue;
                JSONArray parts = item.optJSONArray("content");
                if (parts == null) continue;
                for (int j = 0; j < parts.length(); j++) {
                    JSONObject part = parts.optJSONObject(j);
                    if (part != null && "output_text".equals(part.optString("type"))) {
                        if (answer.length() > 0) answer.append("\n");
                        answer.append(part.optString("text", ""));
                    }
                }
            }
        }
        return answer.length() == 0 ? "I received no text answer. Please try again." : answer.toString().trim();
    }

    private void showSettings() {
        LinearLayout box = column();
        TextView note = text("Paste your OpenAI API key. This prototype saves it only in this app's local preferences. Do not use a key you cannot afford to expose; a production app should use a private backend.", 15, Color.DKGRAY, false);
        box.addView(note);
        EditText keyInput = new EditText(this);
        keyInput.setSingleLine(true);
        keyInput.setHint("sk-...");
        keyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        keyInput.setText(prefs.getString("api_key", ""));
        box.addView(keyInput);
        new AlertDialog.Builder(this)
                .setTitle("Verity AI Settings")
                .setView(box)
                .setPositiveButton("Save key", (d, w) -> {
                    prefs.edit().putString("api_key", keyInput.getText().toString().trim()).apply();
                    answerView.setText("VERITY: Settings saved. Ask your next question.");
                })
                .setNeutralButton("Remove key", (d, w) -> {
                    prefs.edit().remove("api_key").apply();
                    answerView.setText("VERITY: API key removed.");
                })
                .setNegativeButton("Cancel", null).show();
    }

    private boolean owned(int i) { return prefs.getBoolean("owned_" + i, false); }
    private boolean equipped(int i) { return prefs.getBoolean("equip_" + i, false); }

    private void buildShop() {
        startScreen("VERITY // COSMETIC SHOP");
        content.addView(text("Spend coins on entity cosmetics. Your purchases are saved.", 18, Color.LTGRAY, false));
        for (int i = 0; i < ITEMS.length; i++) {
            final int index = i;
            LinearLayout item = row();
            item.setBackground(rounded(panel, Color.rgb(54, 74, 74)));
            item.setPadding(10, 6, 10, 6);
            LinearLayout info = column();
            info.addView(text(ITEMS[i], 20, Color.WHITE, true));
            info.addView(text(owned(i) ? "OWNED" : "Cost: " + COSTS[i] + " coins", 15, Color.YELLOW, false));
            item.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            String label = owned(i) ? (equipped(i) ? "EQUIPPED" : "EQUIP") : "BUY";
            item.addView(button(label, () -> {
                if (!owned(index)) {
                    if (coins() < COSTS[index]) {
                        new AlertDialog.Builder(this).setTitle("Not enough coins").setMessage("Ask more questions to earn coins.").setPositiveButton("OK", null).show();
                        return;
                    }
                    prefs.edit().putInt("coins", coins() - COSTS[index]).putBoolean("owned_" + index, true).apply();
                }
                for (int k = 0; k < ITEMS.length; k++) prefs.edit().putBoolean("equip_" + k, false).apply();
                prefs.edit().putBoolean("equip_" + index, true).apply();
                buildShop();
            }));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.topMargin = 10;
            content.addView(item, lp);
        }
        content.addView(button("BACK TO VERITY", this::buildHome));
    }

    private void buildCustomize() {
        startScreen("VERITY // CUSTOMIZE");
        content.addView(text("Equipped: " + equippedName(), 22, green, true));
        content.addView(text(avatarArt(), 25, Color.WHITE, true));
        content.addView(text("Visit the Cosmetic Shop to buy and equip more looks.", 17, Color.LTGRAY, false));
        content.addView(button("OPEN COSMETIC SHOP", this::buildShop));
        content.addView(button("BACK", this::buildHome));
    }

    private String equippedName() {
        for (int i = 0; i < ITEMS.length; i++) if (equipped(i)) return ITEMS[i];
        return "Default Smile";
    }

    @Override public void onBackPressed() {
        buildHome();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
