package com.irl.survivalapp;

import android.telephony.SmsManager;

import java.util.ArrayList;

/** Class to send SMS */
public class SendSMS {

    private SmsManager smsManager = null;
    private ArrayList<String> fragmentList = null;
    private String serviceCenterAddress = null;

    /** Class constructor */
    public SendSMS() {
        smsManager = SmsManager.getDefault();
    }

    /**
     * Sens the SMS to the specified destination with the specified content
     * @param destination phoneNumber
     * @param smsText text content
     * @return success of the operation
     */
    public boolean sendSMS(String destination, String smsText) {
        if (smsManager == null)
            return false;

        fragmentList = smsManager.divideMessage(smsText);
        if(fragmentList.size() > 1) {
            smsManager.sendMultipartTextMessage(destination, serviceCenterAddress, fragmentList, null, null);
        } else {
            smsManager.sendTextMessage(destination, serviceCenterAddress, smsText, null, null);
        }
        return true;
    }
}
