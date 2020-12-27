package com.a.robust.data;

import com.a.robust.patch.annotation.Modify;

public class M {
    private int apple = 3;

//    public String a(int appleNum) {
//        this.apple = appleNum;
//        StringBuilder sb = new StringBuilder();
//        sb.append("M aaa ,apple:");
//        sb.append(this.apple);
//        return sb.toString();
//    }

    @Modify
    public String a(int appleNum) {
        this.apple = appleNum + 1;
        StringBuilder sb = new StringBuilder();
        sb.append("M aaa fix,apple:");
        sb.append(this.apple);
        return sb.toString();
    }


    public static int b() {
        System.out.println("method b ");
        return 1;
    }
}
