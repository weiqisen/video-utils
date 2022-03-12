package com.up.wqs.service;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Part {

    private ArrayList<String> getAll(String s, List<Integer> iL, int m, ArrayList<String> ifs, ArrayList<String> ins) {
        if (m == 0) {
            ifs.add(s);
            return ifs;
        }
        List<Integer> iL2;
        for (int i = 0; i < ins.size(); i++) {
            iL2 = new ArrayList<>();
            iL2.addAll(iL);
            if (!iL.contains(i)) {
                String str = "";
                if (s.equals("")) {
                    str = s + ins.get(i);
                } else {
                    str = s + "@" + ins.get(i);

                }
                iL2.add(i);
                getAll(str, iL2, m - 1, ifs, ins);

            }
        }
        return ifs;
    }

    public static ArrayList<String> getList(ArrayList<String> ins, int groupNum) {
        List<Integer> iL = new ArrayList<>();
        ArrayList<String> ifs = new ArrayList<>();
        ArrayList<String> iffs = new Part().getAll("", iL, groupNum, ifs, ins);
        Collections.shuffle(iffs);
        return iffs;
    }

    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            strings.add(i + "");
        }
        ArrayList<String> list = getList(strings, 3);
        for (String s : list) {
            System.out.println(s);
        }
    }

}
