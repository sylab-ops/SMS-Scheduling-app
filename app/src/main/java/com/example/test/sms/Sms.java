package com.example.test.sms;

public class Sms {
    public String name = "";
    public String number = "";
    public String messageDate = "";
    public String messageTime = "";
    public String message = "";
    public String messageStatus = "";

    Sms(String n, String ln, String md, String mt, String m, String ms) {
        name = n;
        number = ln;
        messageDate = md;
        messageTime = mt;
        message = m;
        messageStatus = ms;
    }
}
