package dev.noash.hearmewatch.Models;

import java.util.ArrayList;

public class PreferenceList {

    public static final int PRE_LIST_SIZE = 6;
    private ArrayList<MyPreference> list = new ArrayList<>(PRE_LIST_SIZE);

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (int i=0;i<list.size();i++) {
            s.append(list.get(i).toString());
        }
        return s.toString();
    }
}
