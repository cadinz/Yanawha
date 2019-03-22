package com.yanawha.osejin.yanawha;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

class CustomDialog {

    private TextView tv_no;
    private TextView tv_yes;
    private Dialog dialog;
    private MyDialogListener dialogListener;

    CustomDialog(@NotNull final Activity activity){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public void setDialogListener(MyDialogListener dialogListener){
        this.dialogListener = dialogListener;
    }

    public void setDialogTitle(String title){
        TextView tv_title = dialog.findViewById(R.id.tv_title);
        tv_title.setText(title);
    }

    public void setDialogContent(String content){
        TextView tv_selected_place = dialog.findViewById(R.id.tv_content);
        tv_selected_place.setText(content);
    }

    public void setDialogYes(String content){
        TextView tv_yes = dialog.findViewById(R.id.tv_yes);
        tv_yes.setText(content);
    }

    public void setDialogNo(String content){
        TextView tv_no = dialog.findViewById(R.id.tv_no);
        tv_no.setText(content);
    }

    public void showDialog() {


        tv_no = dialog.findViewById(R.id.tv_no);
        tv_yes = dialog.findViewById(R.id.tv_yes);

        tv_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onNegativeClicked();
                dialog.dismiss();
            }
        });

        tv_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onPositiveClicked();
                dialog.cancel();
            }
        });

        dialog.show();
    }
}