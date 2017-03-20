package com.t25.hbv601g.timerunner.services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.t25.hbv601g.timerunner.ClockActivity;
import com.t25.hbv601g.timerunner.R;
import com.t25.hbv601g.timerunner.communications.ClockCallback;
import com.t25.hbv601g.timerunner.communications.NetworkManager;
import com.t25.hbv601g.timerunner.entities.Entry;
import com.t25.hbv601g.timerunner.repositories.UserLocalStorage;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by dingo on 17.3.2017.
 */

public class ClockService {

    private NetworkManager mNetworkManager;
    private final Context mContext;
    private UserLocalStorage mLocalStorage;
    private Entry mCurrentEntry; //ekki í uml

    public ClockService(Context context) {
        mNetworkManager = NetworkManager.getInstance(context);
        mLocalStorage = UserLocalStorage.getInstance(context);
        mContext = context;
    }

    public void notifyIfClockedOut(final NotificationCompat.Builder clockNotification, final int uniqueNotificationId){
        String token = mLocalStorage.getToken();
        mNetworkManager.getOpenClockEntry(token, new ClockCallback() {
            @Override
            public void onSuccess(Entry entry) {
                mCurrentEntry = entry;
                if(entry==null){
                    // Todo something if user is not clocked in
                    // Build the notification
                    clockNotification.setSmallIcon(R.drawable.running_man);
                    clockNotification.setTicker("Want to clock in?");
                    clockNotification.setWhen(System.currentTimeMillis());
                    clockNotification.setContentTitle("Clock in to TimerRunner?");
                    clockNotification.setContentText("Tap to clock-in.");
                    clockNotification.setVibrate(new long[] {2000, 500});
                    clockNotification.setLights(Color.RED, 500, 500);

                    Intent clockInIntent = new Intent(mContext, ClockActivity.class);
                    // Give Android OS access to our app's newly created intent.
                    PendingIntent pendingClockIntent = PendingIntent.getActivity(mContext, 0, clockInIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    clockNotification.setContentIntent(pendingClockIntent);

                    // Issue the notification
                    NotificationManager nm =  (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(uniqueNotificationId, clockNotification.build());
                } else {
                    // Todo Nothing if he is not clocked in
                }
            }

            @Override
            public void onFailure(String error) {
                // TODO perhaps nothing?
                //Toast.makeText(mContext,
                //        mContext.getString(R.string.server_error), Toast.LENGTH_LONG).show();
            }
        });
    }


    public void setClockedButtonText(final Button button){
        String token = mLocalStorage.getToken();
        mNetworkManager.getOpenClockEntry(token, new ClockCallback() {
            @Override
            public void onSuccess(Entry entry) {
                mCurrentEntry = entry;
                if(entry==null){
                    button.setText("Clock in");
                } else {
                    button.setText("Clock out");
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(mContext,
                        mContext.getString(R.string.server_error), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void clock(final Button button){
        String token = mLocalStorage.getToken();
        mNetworkManager.clockInOut(token, mCurrentEntry, new ClockCallback(){

            @SuppressLint("StringFormatInvalid")
            @Override
            public void onSuccess(Entry entry) {
                if(entry.getOutTime() != null){
                    Toast.makeText(mContext,
                            mContext.getString(R.string.clock_out_toast), Toast.LENGTH_LONG).show();
                    button.setText(mContext.getString(R.string.clock_in_btn_text));
                } else {
                    Toast.makeText(mContext,
                            mContext.getString(R.string.clock_in_toast), Toast.LENGTH_LONG).show();
                    button.setText(mContext.getString(R.string.clock_out_btn_text));
                }
                mCurrentEntry = entry;

            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(mContext,
                        mContext.getString(R.string.server_error), Toast.LENGTH_LONG).show();
            }
        });
    }

    public Entry getCurrentEntry(){
        return mCurrentEntry;
    }

}
