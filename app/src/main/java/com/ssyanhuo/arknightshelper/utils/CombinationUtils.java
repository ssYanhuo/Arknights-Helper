package com.ssyanhuo.arknightshelper.utils;

import java.util.ArrayList;
import java.util.Collections;

public class CombinationUtils {
    public static ArrayList<ArrayList<String>> combine (ArrayList<String> array){
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        int size = array.size();
        int nBit = (int) Math.pow(2, size) - 1;
        for (int i = 1; i <= nBit; i++){
            ArrayList<String> tmp = new ArrayList<>();
            for(int j = 0 ; j < size; j++){
                int bit = 1 << j;
                if ((bit & i) != 0){
                    tmp.add(array.get(j));
                }
            }
            Collections.sort(tmp);
            result.add(new ArrayList<String>(tmp));
        }
        return result;
    }
}
