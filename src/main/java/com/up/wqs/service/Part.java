package com.up.wqs.service;


import java.util.ArrayList;
import java.util.List;

public class Part {

        private static int m = 3;

        private ArrayList<String> getAll(String s, List<Integer> iL, int m,ArrayList<String> ifs,ArrayList<String> ins) {
            if(m == 0) {
                ifs.add(s);
                return ifs;
            }
            List<Integer> iL2;
            for(int i = 0; i < ins.size(); i++) {
                iL2 = new ArrayList<>();
                iL2.addAll(iL);
                if(!iL.contains(i)) {
                    String str = "";
                    if (s.equals("") ){
                        str = s + ins.get(i);
                    } else {
                        str = s + "@" + ins.get(i);

                    }
                    iL2.add(i);
                    getAll(str, iL2, m-1, ifs,ins);

                }
            }
            return ifs;
        }

    public static ArrayList<String> getList (ArrayList<String> ins)
    {
        List<Integer> iL = new ArrayList<>();
        ArrayList<String> ifs = new ArrayList<>();
        ArrayList<String> iffs = new Part().getAll("", iL, m, ifs, ins);
        return iffs;
    }


}
