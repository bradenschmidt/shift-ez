package com.schmidtdesigns.shiftez.models;

/**
 * Created by braden on 11/06/15.
 */
public class PostResult {

    /**
     * code : 0
     * desc : Upload Successful
     */
    private int code;
    private String desc;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String niceToString() {
        return desc + " (Code: " + code + ")";
    }

    @Override
    public String toString() {
        return "PostResult{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                '}';
    }
}
