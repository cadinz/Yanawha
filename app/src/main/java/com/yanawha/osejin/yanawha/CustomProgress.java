package com.yanawha.osejin.yanawha;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Window;
import android.widget.TextView;

public class CustomProgress extends Dialog
{

    public CustomProgress(@NonNull Context context)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // No Title
        setContentView(R.layout.custom_progress);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setCancelable(false);
    }
}