package com.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResultList implements Serializable {
    private List<ResultBean> results;

    public ResultList() {
        results = new ArrayList<>();
    }

    public void addResult(ResultBean result) {
        results.add(result);
    }

    public List<ResultBean> getResults() {
        return results;
    }
}