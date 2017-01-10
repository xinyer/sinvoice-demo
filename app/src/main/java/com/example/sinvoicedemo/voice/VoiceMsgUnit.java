package com.example.sinvoicedemo.voice;

public class VoiceMsgUnit implements Comparable {

    private int index;
    private String content;

    public VoiceMsgUnit(int index, String content) {
        this.index = index;
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VoiceMsgUnit) {
            VoiceMsgUnit unit = (VoiceMsgUnit) obj;
            if (unit.index == this.index) return true;
        }

        return false;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof VoiceMsgUnit) {
            VoiceMsgUnit unit = (VoiceMsgUnit) o;
            return this.index - unit.index;
        }
        return -1;
    }
}
