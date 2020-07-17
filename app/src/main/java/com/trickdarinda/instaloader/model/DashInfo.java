
package com.trickdarinda.instaloader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DashInfo {

    @SerializedName("is_dash_eligible")
    @Expose
    private Boolean isDashEligible;
    @SerializedName("video_dash_manifest")
    @Expose
    private String videoDashManifest;
    @SerializedName("number_of_qualities")
    @Expose
    private Integer numberOfQualities;

    public Boolean getIsDashEligible() {
        return isDashEligible;
    }

    public void setIsDashEligible(Boolean isDashEligible) {
        this.isDashEligible = isDashEligible;
    }

    public String getVideoDashManifest() {
        return videoDashManifest;
    }

    public void setVideoDashManifest(String videoDashManifest) {
        this.videoDashManifest = videoDashManifest;
    }

    public Integer getNumberOfQualities() {
        return numberOfQualities;
    }

    public void setNumberOfQualities(Integer numberOfQualities) {
        this.numberOfQualities = numberOfQualities;
    }

}
