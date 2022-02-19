/*
 *   DialogSystem.java
 *
 *   Created by Artem Khlyupin on 28/01/2019.
 *   Copyright © 2019 Artem Khlyupin. All rights reserved.
 */

package com.hifitoy.dialogsystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Build;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;
import com.hifitoy.R;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyDeviceManager;
import com.hifitoy.hifitoydevice.PeripheralData;
import com.hifitoy.hifitoyobjects.Biquad;

public class DialogSystem {
    private static final String TAG = "HiFiToy";
    private static DialogSystem instance;

    private BaseProgressDialog progressDialog = null;
    private BaseDialog dialog = null;

    public static DialogSystem getInstance(){
        if (instance == null){
            instance = new DialogSystem();
        }
        return instance;
    }

    public DialogSystem(){

    }

    /*---------------------------------- Getter dialogs -------------------------------------*/
    public AlertDialog getAlertDialog(){
        return dialog;
    }

    public ProgressDialog getProgressDialog(){
        return progressDialog;
    }

    /*---------------------------------- Close dialogs -------------------------------------*/
    public void closeDialog(){
        if (dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }

    public void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /*---------------------------------- Utility. Get message from dialog -------------------------------------*/
    public String getDialogMessage(){
        if (dialog != null) {
            return dialog.getMessage();
        }
        return null;
    }

    /*---------------------------------- Show 1button dialog -------------------------------------*/
    public void showDialog(String title, String message, String button) {
        final Context context = ApplicationContext.getInstance().getContext();

        dialog = new BaseDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        DialogInterface.OnClickListener listener = null;
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, button, listener);
        dialog.show();
    }

    public void showDialog(String title, View messageView, String button) {
        final Context context = ApplicationContext.getInstance().getContext();

        dialog = new BaseDialog(context);
        dialog.setTitle(title);
        dialog.setView(messageView);
        DialogInterface.OnClickListener listener = null;
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, button, listener);
        dialog.show();
    }

    /*----------------------------- Show base dialog and text dialog -----------------------------*/
    public void showDialog(final OnClickDialog onClickDialog,
                           String title, String message,
                           String posButton, String negButton){
        final Context context = ApplicationContext.getInstance().getContext();

        BaseDialog d = new BaseDialog(context);
        d.setTitle(title);
        d.setMessage(message);
        d.setButton(DialogInterface.BUTTON_POSITIVE, posButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onClickDialog != null) onClickDialog.onPositiveClick();
            }
        });
        d.setButton(DialogInterface.BUTTON_NEGATIVE, negButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onClickDialog != null) onClickDialog.onNegativeClick();
            }
        });
        d.show();
    }

    public void showDialog(final OnClickDialog onClickDialog,
                           String title, View messageView,
                           String posButton, String negButton){
        final Context context = ApplicationContext.getInstance().getContext();

        BaseDialog d = new BaseDialog(context);
        d.setTitle(title);
        d.setView(messageView);
        d.setButton(DialogInterface.BUTTON_POSITIVE, posButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onClickDialog != null) onClickDialog.onPositiveClick();
            }
        });
        d.setButton(DialogInterface.BUTTON_NEGATIVE, negButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onClickDialog != null) onClickDialog.onNegativeClick();
            }
        });

        if (d.getWindow() != null) {
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        d.show();
    }

    public void showTextDialog(final OnClickTextDialog onClickDialog, String title, String defaultText,
                               String posButton, String negButton){
        showEditTextDialog(false, onClickDialog, title, defaultText, posButton, negButton);
    }

    public void showTextDialog(final OnClickTextDialog onClickDialog, String title,
                               String posButton, String negButton){
        showEditTextDialog(false, onClickDialog, title, posButton, negButton);
    }

    public void showNumberTextDialog(final OnClickTextDialog onClickDialog, String title,
                               String posButton, String negButton){
        showEditTextDialog(true, onClickDialog, title, posButton, negButton);
    }

    private void showEditTextDialog(boolean numberType,
                                    final OnClickTextDialog onClickDialog,
                                    String title,
                                    String defaultText,
                                    String posButton,
                                    String negButton){
        final Context context = ApplicationContext.getInstance().getContext();

        BaseDialog dialog = new BaseDialog(context);
        dialog.setTitle(title);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View inputView = inflater.inflate(R.layout.layout_input, null);
        final EditText input = inputView.findViewById(R.id.nameInput_outl);
        input.setText(defaultText, TextView.BufferType.SPANNABLE);
        input.requestFocus();

        if (numberType) input.setInputType(InputType.TYPE_CLASS_NUMBER);

        dialog.setView(inputView);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, posButton,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        if (onClickDialog != null){
                            onClickDialog.onPositiveClick(input.getText().toString());
                        }
                    }
                });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, negButton,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (onClickDialog != null){
                            onClickDialog.onNegativeClick(input.getText().toString());
                        }
                        dialog.cancel();
                    }
                });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.show();
    }

    private void showEditTextDialog(boolean numberType,
                                    final OnClickTextDialog onClickDialog,
                                    String title,
                                    String posButton,
                                    String negButton) {
        showEditTextDialog(numberType, onClickDialog, title, "", posButton, negButton);
    }

    /*-------------------------------- Show base progress dialog --------------------------------*/
    public void showProgressDialog(String title, int maxPackets){
        if (!HiFiToyControl.getInstance().isConnected()){
            return;
        }

        closeProgressDialog();
        progressDialog = new BaseProgressDialog(ApplicationContext.getInstance().getContext());

        progressDialog.setMax(maxPackets);

        //set the icon, title and progress style..
        progressDialog.setTitle(title);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.setCancelable(false);
        //initialize the dialog..
        progressDialog.setProgress(0);
        progressDialog.setSecondaryProgress(0);

        //show the dialog
        progressDialog.show();
    }


    public void updateProgressDialog(int value){
        if (progressDialog != null) {
            progressDialog.incrementProgressBy(value);
        }
    }

    /*---------------------------- Show pairing code dialog -----------------------------*/
    public void showPairingCodeDialog(){
        final Activity activity = (Activity)ApplicationContext.getInstance().getContext();

        showNumberTextDialog(new OnClickTextDialog() {
            @Override
            public void onPositiveClick(String text) {
                try {
                    int pair_code = Integer.parseInt(text);
                    Log.d(TAG, String.format("Pair code = %d", pair_code));

                    HiFiToyDevice d = HiFiToyControl.getInstance().getActiveDevice();
                    d.setPairingCode(pair_code);
                    HiFiToyDeviceManager.getInstance().store();

                    //send pairing code to dsp
                    HiFiToyControl.getInstance().startPairedProccess(pair_code);
                } catch (NumberFormatException e) {
                    Toast.makeText(activity, "The value of a pair code is not allowed.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNegativeClick(String text) {
                HiFiToyControl.getInstance().disconnect();
            }
        }, "Enter pairing code", "Send", "Cancel");
    }

    /*====================== OnClickDialog Interfaces ===============================*/
    public interface OnClickDialog {
        void onPositiveClick();
        void onNegativeClick();
    }

    public interface OnClickTextDialog {
        void onPositiveClick(String text);
        void onNegativeClick(String text);

    }

}
