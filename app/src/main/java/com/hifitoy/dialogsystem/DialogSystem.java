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
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hifitoy.ApplicationContext;
import com.hifitoy.hifitoycontrol.HiFiToyControl;
import com.hifitoy.hifitoydevice.HiFiToyDevice;
import com.hifitoy.hifitoydevice.HiFiToyDeviceManager;

public class DialogSystem {

    private static final String TAG = "HiFiToy";
    private static DialogSystem instance;

    private int tempOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    private ProgressDialog progressDialog = null;
    private AlertDialog dialog = null;

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
            progressDialog.setProgress(0);
            progressDialog.dismiss();

            Activity activity = (Activity) ApplicationContext.getInstance().getContext();
            //activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            activity.setRequestedOrientation(tempOrientation);
        }
    }

    /*---------------------------------- Utility. Get message from dialog -------------------------------------*/
    public String getDialogMessage(){
        if (dialog != null){
            TextView message = dialog.findViewById(android.R.id.message);
            return message.getText().toString();
        }
        return null;
    }

    /*---------------------------------- Utility. Get message from dialog -------------------------------------*/
    public String getProgressDialogTitle(){
        if (progressDialog != null){
            Resources res = ApplicationContext.getInstance().getContext().getResources();
            TextView title = progressDialog.findViewById(res.getIdentifier("alertTitle", "id", "android"));

            if (title != null) {
                return title.getText().toString();
            }
        }
        return null;
    }

    /*---------------------------------- Show 1button dialog -------------------------------------*/
    public void showDialog(String title, String message, String button) {
        dialog = new AlertDialog.Builder(ApplicationContext.getInstance().getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(button, null)
                .show();
    }

    /*----------------------------- Show base dialog and text dialog -----------------------------*/
    public void showDialog(final OnClickDialog onClickDialog,
                           String title, String message,
                           String posButton, String negButton){
        final Context context = ApplicationContext.getInstance().getContext();

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(posButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (onClickDialog != null){
                                    onClickDialog.onPositiveClick();
                                }
                            }
                        })
                .setNegativeButton(negButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (onClickDialog != null){
                                    onClickDialog.onNegativeClick();
                                }
                            }
                        }).show();
    }

    public void showTextDialog(final OnClickTextDialog onClickDialog, String title,
                               String posButton, String negButton){
        final Context context = ApplicationContext.getInstance().getContext();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);

        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        alertDialog.setView(input);

        alertDialog.setPositiveButton(posButton,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        if (onClickDialog != null){
                            onClickDialog.onPositiveClick(input.getText().toString());
                        }
                    }
                });
        alertDialog.setNegativeButton(negButton,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (onClickDialog != null){
                            onClickDialog.onNegativeClick(input.getText().toString());
                        }
                        dialog.cancel();
                    }
                });


        Dialog dialog = alertDialog.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    /*-------------------------------- Show base progress dialog --------------------------------*/
    public void showProgressDialog(String title, int maxPackets){
        if (!HiFiToyControl.getInstance().isConnected()){
            return;
        }

        Context context = ApplicationContext.getInstance().getContext();
        //context.registerReceiver(mBleReceiver, makeGattUpdateIntentFilter());

        Activity activity = (Activity)context;
        tempOrientation = activity.getRequestedOrientation();
        activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        closeProgressDialog();
        progressDialog = new ProgressDialog(ApplicationContext.getInstance().getContext());

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


    /*-------------------------------- Show factory reset dialog ---------------------------------*/
    public void showFactoryResetDialog(){
        OnClickDialog dialogListener = new OnClickDialog() {
            public void onPositiveClick(){
                HiFiToyControl.getInstance().getActiveDevice().restoreFactorySettings();
                //showFactoryProgressDialog();
            }
            public void onNegativeClick(){
                //
            }
        };

        showDialog(dialogListener, "Warning", "Are you sure you want to reset to factory defaults?", "Ok", "Cancel");
    }

    public void showFirmwareFailDialog() {
        OnClickDialog dialogListener = new OnClickDialog() {
            public void onPositiveClick(){
                HiFiToyControl.getInstance().getActiveDevice().restoreFactorySettings();
            }
            public void onNegativeClick(){
                //
            }
        };

        showDialog(dialogListener,
                "Dsp Firmware fail",
                "Dsp Firmware is corrupted! 'Restore Factory Seetings' will solve problem, continue?",
                "Ok", "Cancel");
    }


    /*public void showFactoryProgressDialog(int remainPackets){
        showProgressDialog("Send factory settings...", remainPackets);
    }*/

    /*---------------------------- Show pairing code dialog -----------------------------*/
    public void showPairingCodeDialog(){
        final Activity activity = (Activity)ApplicationContext.getInstance().getContext();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("Enter pairing code");

        final EditText input = new EditText(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(4) });

        alertDialog.setView(input);

        alertDialog.setPositiveButton("Send",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        try {
                            int pair_code = Integer.parseInt(input.getText().toString());
                            Log.d(TAG, String.format("Pair code = %d", pair_code));

                            HiFiToyDevice d = HiFiToyControl.getInstance().getActiveDevice();
                            d.setPairingCode(pair_code);
                            HiFiToyDeviceManager.getInstance().store();

                            //send pairing code to dsp
                            HiFiToyControl.getInstance().startPairedProccess(pair_code);
                        } catch (NumberFormatException e) {
                            Toast.makeText(activity, "The value of a pair code is not allowed.",
                                    Toast.LENGTH_SHORT).show();
                        } finally {
                            activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        }
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HiFiToyControl.getInstance().disconnect();
                        activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        activity.finish();
                    }
                });


        activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        Dialog dialog = alertDialog.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    /*---------------------------- Show save preset progress dialog -----------------------------*/
    /*public void showSavePresetProgressDialog(){
        int maxPackets = DspControl.getInstance().getBleService().getPacketQueueSize();
        showProgressDialog("Send dsp parameters...", maxPackets);

    }*/

    /*-------------------------- Show import preset progress dialog -----------------------------*/
    /*public void showImportPresetProgressBar(int maxPackets){
        showProgressDialog("Import Preset...", maxPackets);
    }*/


    /*====================== OnClickDialog Interfaces ===============================*/
    public interface OnClickDialog {
        public void onPositiveClick();
        public void onNegativeClick();
    }

    public interface OnClickTextDialog {
        public void onPositiveClick(String text);
        public void onNegativeClick(String text);
    }

}
