package com.analytics.web;

import java.util.List;
import java.util.Map;

/**
 * Created by Александр on 09.03.2015.
 */
public class DataValue {
    private final Integer id;
    private final Map<Integer, List<String>> values;

    public DataValue(Integer randomId, Map<Integer, List<String>> values) {

        this.id = randomId;
        this.values = values;
    }

    public Integer getId() {
        return id;
    }


    public String getValues(Integer key, int index) {
        if (values != null && index < values.get(key).size())
            return values.get(key).get(index);
        else return null;
    }

    public Map<Integer, List<String>> getValues() {
        return values;
    }
}
