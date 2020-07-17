
package com.trickdarinda.instaloader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EdgeMediaToCaption {

    @SerializedName("edges")
    @Expose
    private List<Edge> edges = null;

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

}
