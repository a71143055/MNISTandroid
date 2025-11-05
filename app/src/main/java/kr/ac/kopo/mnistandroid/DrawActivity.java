package kr.ac.kopo.mnistandroid;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.divyanshu.draw.widget.DrawView;

import java.io.IOException;
import java.util.Locale;

public class DrawActivity extends AppCompatActivity {

    private Classifier cls;
    private static final String TAG = "DrawActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        DrawView drawView = findViewById(R.id.drawView);
        TextView resultView = findViewById(R.id.resultView);
        Button classifyBtn = findViewById(R.id.classifyBtn);
        Button clearBtn = findViewById(R.id.clearBtn);

        if (drawView != null) {
            drawView.setStrokeWidth(100.0f);
            drawView.setBackgroundColor(Color.BLACK);
            drawView.setColor(Color.WHITE);
        }

        cls = new Classifier(this);
        try {
            cls.init();
            Log.d(TAG, "Classifier initialized successfully");
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to initialize Classifier", ioe);
            cls = null;
        }

        if (classifyBtn != null && resultView != null && drawView != null) {
            classifyBtn.setOnClickListener(v -> {
                if (cls == null) {
                    resultView.setText("Classifier 초기화 실패");
                    Log.e(TAG, "Classifier is null");
                    return;
                }

                Bitmap image = drawView.getBitmap();

                try {
                    Pair<Integer, Float> res = cls.classify(image);
                    String outStr = String.format(Locale.ENGLISH, "%d, %.0f%%", res.first, res.second * 100.0f);
                    resultView.setText(outStr);
                    Log.d(TAG, "Classification result: " + outStr);
                } catch (Exception e) {
                    resultView.setText("분류 중 오류 발생");
                    Log.e(TAG, "Error during classification", e);
                }
            });
        }

        if (clearBtn != null && drawView != null) {
            clearBtn.setOnClickListener(v -> drawView.clearCanvas());
        }
    }

    @Override
    protected void onDestroy() {
        if (cls != null) {
            cls.finish();
        }
        super.onDestroy();
    }
}
