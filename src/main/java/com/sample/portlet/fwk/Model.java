package com.sample.portlet.fwk;

import com.sample.portlet.fwk.F.Maybe;
import java.util.HashMap;

public class Model extends HashMap<String, Object> {

    public static final String ERROR = "error";

    public Maybe<Object> forKey(String key) {
        return new Maybe<Object>(super.get(key));
    }
}
