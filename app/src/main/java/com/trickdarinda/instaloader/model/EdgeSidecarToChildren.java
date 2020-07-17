
package com.trickdarinda.instaloader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EdgeSidecarToChildren {

    @SerializedName("edges")
    @Expose
    private List<Edge_> edges = null;

    public List<Edge_> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge_> edges) {
        this.edges = edges;
    }

}
