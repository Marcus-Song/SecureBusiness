package com.marcus.securebusiness.util;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import static com.twilio.rest.api.v2010.account.Message.creator;

public class SmsUtils {
    protected static final String FROM_NUMBER = "+14178052404";
    protected static final String SID_KEY = "ACb155f8dabd102caba8398c5cd349e03f";
    protected static final String TOKEN_KEY = "----";

    public static void sendSMS(String to, String messageBody) {
        Twilio.init(SID_KEY, TOKEN_KEY);
        Message message = creator(new PhoneNumber("+1" + to), new PhoneNumber(FROM_NUMBER), messageBody).create();
        System.out.println(message);
    }












}
