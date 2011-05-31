package com.sample.portlet.fwk;

import com.sample.portlet.fwk.F.Maybe;
import com.sample.portlet.fwk.F.Option;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.portlet.PortletRequest;

public class RequestParams {

    private PortletRequest getRequest() {
        return PortletController.currentRequest.get();
    }

    public Option<String> parameter(String name) {
        return new Maybe<String>(getRequest().getParameter(name));
    }
    
    public Option<List<String>> parameterValues(String name) {
        String[] values = getRequest().getParameterValues(name);
        if (values == null) {
            return new Maybe<List<String>>(new ArrayList<String>());
        }
        return new Maybe<List<String>>(Arrays.asList(values));
    }
    
    public Map<String, String[]> parameters() {
        return getRequest().getParameterMap();
    }
    
    public List<String> parametersNames() {
        return Collections.list(getRequest().getParameterNames());
    }
}
