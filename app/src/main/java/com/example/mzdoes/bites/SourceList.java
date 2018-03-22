package com.example.mzdoes.bites;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeucudatcapua2 on 3/22/18.
 */

public class SourceList {
    private String status;
    private List<NewsSource> sourceList;

    public SourceList() {
        status = "ok";
        sourceList = new ArrayList<>();
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public List<NewsSource> getSourceList() {
        return sourceList;
    }
    public void setSourceList(List<NewsSource> sourceList) {
        this.sourceList = sourceList;
    }
}
