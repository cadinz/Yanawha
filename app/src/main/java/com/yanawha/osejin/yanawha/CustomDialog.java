package com.yanawha.osejin.yanawha;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

class CustomDialog {

    private TextView tv_no;
    private TextView tv_yes;
    private Dialog dialog;
    private MyDialogListener dialogListener;

    CustomDialog(final Activity activity){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.newcustom_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public void setDialogListener(MyDialogListener dialogListener){
        this.dialogListener = dialogListener;
    }

    public void setTvSelectedPlace(String selectedPlace){
        TextView tv_selected_place = dialog.findViewById(R.id.tv_selected_place);
        tv_selected_place.setText(selectedPlace);
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