package dev.noash.hearmewatch.Utilities;

import android.content.Context;
import android.widget.TextView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import dev.noash.hearmewatch.R;

public class StepItemView extends LinearLayout {

    private TextView stepNumber, stepTitle, stepDescription;

    public StepItemView(Context context) {
        super(context);
        init(context);
    }

    public StepItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StepItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.step_view, this, true);

        stepNumber = findViewById(R.id.TV_step_number);
        stepTitle = findViewById(R.id.TV_step_title);
        stepDescription = findViewById(R.id.TV_step_description);
    }

    public void setStep(int number, String title, String description, @DrawableRes int backgroundResId) {
        stepNumber.setText(String.valueOf(number));
        stepNumber.setBackground(ContextCompat.getDrawable(getContext(), backgroundResId));
        stepTitle.setText(title);
        stepDescription.setText(description);
    }
}
