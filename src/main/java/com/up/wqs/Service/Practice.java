package com.up.wqs.Service;

/**
 * @author weiqisen
 * @date 2022/2/25 13:01
 **/
import java.util.LinkedList;

public class Practice
{
    public static void recursionSub ( LinkedList<String[]> list, int count, String[] array, int ind, int start,
                                      int... indexs )
    {
        start++;
        if (start > count - 1)
        {
            return;
        }
        if (start == 0)
        {
            indexs = new int[array.length];
        }
        for ( indexs[start] = ind; indexs[start] < array.length; indexs[start]++ )
        {
            recursionSub (list, count, array, indexs[start] + 1, start, indexs);
            if (start == count - 1)
            {
                String[] temp = new String[count];
                for ( int i = count - 1; i >= 0; i-- )
                {
                    temp[start - i] = array[indexs[start - i]];
                }
                list.add (temp);
            }
        }
    }

    public static LinkedList<String[]> getList (String[] videos)
    {
        LinkedList<String[]> list = new LinkedList<>();
        recursionSub (list, 3, videos, 0, -1);
        return list;
    }


}