package io.p13i.glassnotes.utilities;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ListUtils {

    public static <T> List<T> join(Collection<T>... collections) {
        List<T> resultantList = new LinkedList<T>();

        for (Collection<T> collection : collections) {
            resultantList.addAll(collection);
        }

        return resultantList;
    }
}
