package com.up.wqs.Service;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Part {

        private static int total;
        private static int m = 3;

        private ArrayList<String> getAll(String s, List<Integer> iL, int m,ArrayList<String> ifs,ArrayList<String> ins) {
            if(m == 0) {
                //System.out.println(s);
                ifs.add(s);
                total++;
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

//    public static void main(String[] args) {
//
//        ArrayList<String> ins = new ArrayList<>();
//        ins.add("a");
//        ins.add("b");
//        ins.add("c");
//        ins.add("d");
//        getList(ins);
//
//    }

    public static ArrayList<String> getList (ArrayList<String> ins)
    {
        List<Integer> iL = new ArrayList<>();
        ArrayList<String> ifs = new ArrayList<>();
        ArrayList<String> iffs = new Part().getAll("", iL, m, ifs, ins);
        //System.out.println(iffs);
        //System.out.println(iffs.size());
        HashSet set = new HashSet(iffs);
        if (set.size() == iffs.size()){
            System.out.println("===============没有重复元素");
        }
        return iffs;
    }


}
