package com.example.sinvoicedemo.voice;

import java.util.Set;
import java.util.TreeSet;

public class VoiceMsg {
    /**
     * count@3
     * 0@xxx
     * 1@yyy
     * 2@zzz
     */

    private static final String SEPARATION = "&@&";
    private static final String COUNT_KEY = "c0un7";
    private static final int SPLIT_LEN = 10;

    private int count = -1;
    private int desireSum = 0;
    private VoiceMsgListener listener;

    private Set<VoiceMsgUnit> msgUnitSet = new TreeSet<>();

    public void setListener(VoiceMsgListener listener) {
        this.listener = listener;
    }

    public void receiveMsg(String msg) {
        if (msg != null && !msg.equals("")) {
            String[] strs = msg.split(SEPARATION);
            if (strs.length != 2) return;
            if (strs[0].equals(COUNT_KEY)) {
                this.count = Integer.parseInt(strs[1]);
            } else {
                int index = Integer.parseInt(strs[0]);
                String content = strs[1];
                msgUnitSet.add(new VoiceMsgUnit(index, content));
            }
        }

        if (checkReceiveSuccess()) {
            if (listener != null) {
                listener.onReceiveOver(getMsg());
            }
        }
    }

    public String getMsg() {
        StringBuilder sb = new StringBuilder();
        for (VoiceMsgUnit unit : msgUnitSet) {
            sb.append(unit.getContent());
        }
        return sb.toString();
    }

    private boolean checkReceiveSuccess() {
        if (count < 0) return false;
        if (desireSum == 0) getDesireSum();

        int tmpSum = 0;
        for (VoiceMsgUnit unit : msgUnitSet) {
            tmpSum += unit.getIndex();
        }
        return tmpSum == desireSum;
    }

    private void getDesireSum() {
        for (int i=0; i<count; i++) {
            desireSum += i;
        }
    }

    public static String[] splitVoiceMsg(String msg) {
        if (msg.length() <= SPLIT_LEN) return new String[] {msg};

        int len = msg.length() / SPLIT_LEN + 1;
        String [] result = new String[len + 1];
        for (int i=0; i<len; i++) {
            if (i == len - 1) result[i] = i + SEPARATION + msg.substring(i*SPLIT_LEN);
            else result[i] = i + SEPARATION + msg.substring(i*SPLIT_LEN, (i+1)*SPLIT_LEN);
        }
        result[len] = COUNT_KEY + SEPARATION + len;
        return result;
    }
}
