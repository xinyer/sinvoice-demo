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

    private int count = -1;
    private int desireSum = 0;
    private VoiceMsgListener listener;

    private Set<VoiceMsgUnit> msgUnitSet = new TreeSet<>();

    public void setListener(VoiceMsgListener listener) {
        this.listener = listener;
    }

    public void receiveMsg(String msg) {
        if (msg != null && !msg.equals("")) {
            String[] strs = msg.split("@");
            if (strs.length == 0) return;

            if (strs[0].equals("count")) {
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
}
