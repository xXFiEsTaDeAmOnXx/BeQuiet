package com.example.bequiet.view.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bequiet.R;

import java.util.Objects;

/**
 * Provide a reference to the type of views that you are using
 * (custom ViewHolder)
 */
public class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView textViewRuleTitle;
    private final TextView textViewStart;
    private final TextView textViewEnd;

    private final RadioButton radioButtonSilence;
    private final RadioButton radioButtonVibrate;
    private final RadioButton radioButtonNoise;
    private final FrameLayout fragmentRule;
    private final View view;
    private final Context context;

    public ViewHolder(View view, Context context) {
        super(view);
        // Define click listener for the ViewHolder's View
        this.view = view;
        this.context = context;

        fragmentRule = (FrameLayout) view.findViewById(R.id.fragmentRule);
        fragmentRule.setId(View.generateViewId());
        textViewRuleTitle = (TextView) view.findViewById(R.id.textViewRuleTitle);
        textViewStart = (TextView) view.findViewById(R.id.textViewStartTime);
        textViewEnd = (TextView) view.findViewById(R.id.textViewEndTime);
        radioButtonSilence = (RadioButton) view.findViewById(R.id.radioButtonSilence);
        radioButtonVibrate = (RadioButton) view.findViewById(R.id.radioButtonVibrate);
        radioButtonNoise = (RadioButton) view.findViewById(R.id.radioButtonFullVolume);
    }

    public Context getContext() {
        return context;
    }

    public RadioButton getRadioButtonSilence() {
        return radioButtonSilence;
    }

    public RadioButton getRadioButtonVibrate() {
        return radioButtonVibrate;
    }

    public RadioButton getRadioButtonNoise() {
        return radioButtonNoise;
    }

    public TextView getTextViewRuleTitle() {
        return textViewRuleTitle;
    }

    public TextView getTextViewStart() {
        return textViewStart;
    }

    public TextView getTextViewEnd() {
        return textViewEnd;
    }

    public View getView() {
        return view;
    }

    public void setFrag(Fragment fragment) {
        View v = fragment.onCreateView(LayoutInflater.from(context), fragmentRule, null);
        fragment.onViewCreated(Objects.requireNonNull(v),  null);
        fragmentRule.addView(v);
    }
}