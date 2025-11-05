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

        if (classifyBtn != null && resultView != null && drawView != null) {
            classifyBtn.setOnClickListener(v -> {
                Bitmap image = drawView.getBitmap();
                if (cls != null) {
                    Pair<Integer, Float> res = cls.classify(image);
                    String outStr = String.format(Locale.ENGLISH, "%d, %.0f%%", res.first, res.second * 100.0f);
                    resultView.setText(outStr);
                }
            });
        }

        if (clearBtn != null && drawView != null) {
            clearBtn.setOnClickListener(v -> drawView.clearCanvas());
        }

        cls = new Classifier(this);
        try {
            cls.init();
        } catch (IOException ioe) {
            Log.e("DigitClassifier", "Failed to initialize Classifier", ioe);
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
