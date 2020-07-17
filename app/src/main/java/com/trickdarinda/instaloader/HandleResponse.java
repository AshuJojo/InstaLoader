package com.trickdarinda.instaloader;

import com.trickdarinda.instaloader.model.PostsResponse;

public class HandleResponse {
    private PostsResponse response;

    public HandleResponse(PostsResponse response) {
        this.response = response;
    }

    /* method to return PostID from response */
    public String getSinglePostID() {
        return response.getGraphql().getShortcodeMedia().getId();
    }

    /* Method to return Username */
    public String getUsername() {
        return response.getGraphql().getShortcodeMedia().getOwner().getUsername();
    }

    public String getMediaCaption() {
        if (response.getGraphql().getShortcodeMedia().getEdgeMediaToCaption().getEdges().size() > 0) {
            return response.getGraphql().getShortcodeMedia().getEdgeMediaToCaption().getEdges().get(0).getNode().getText();
        }
        return null;
    }

    /*Method to return single image Display Url */
    public String getDisplayUrl() {
        if (response.getGraphql().getShortcodeMedia().getDisplayResources().get(0) != null)
            return response.getGraphql().getShortcodeMedia().getDisplayResources().get(0).getSrc();
        else
            return response.getGraphql().getShortcodeMedia().getDisplayUrl();
    }

    /*Method to return username profile pic url */
    public String getProfilePicUrl() {
        return response.getGraphql().getShortcodeMedia().getOwner().getProfilePicUrl();
    }

    /*Method to return download URL of a single image */
    public String getSingleImgUrl() {
        //Return the best download quality url
        if (response.getGraphql().getShortcodeMedia().getDisplayResources().get(2).getSrc() != null) {
            return response.getGraphql().getShortcodeMedia().getDisplayResources().get(2).getSrc();
        } else if (response.getGraphql().getShortcodeMedia().getDisplayResources().get(1).getSrc() != null) {
            return response.getGraphql().getShortcodeMedia().getDisplayResources().get(1).getSrc();
        } else if (response.getGraphql().getShortcodeMedia().getDisplayResources().get(0).getSrc() != null) {
            return response.getGraphql().getShortcodeMedia().getDisplayResources().get(0).getSrc();
        } else {
            return response.getGraphql().getShortcodeMedia().getDisplayUrl();
        }
    }

    /*Method to return single video display thumbnail */
    public String getSingleVidDisplayThumbnail() {
        return response.getGraphql().getShortcodeMedia().getDisplayUrl();
    }

    /*Method to check if url is a single video or image */
    public boolean getIsVideo() {
        return response.getGraphql().getShortcodeMedia().getIsVideo();
    }

    /*Method to return single video download url */
    public String getSingleVidUrl() {
        return response.getGraphql().getShortcodeMedia().getVideoUrl();
    }

    /* Return the number of edges in multiple Media Url */
    public int getNumOfEdges() {
        return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                .getEdges().size();
    }

    /* Method to return multiple post Id */
    public String multiplePostID(int i) {
        return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren().getEdges()
                .get(i).getNode().getId();
    }

    /*Method to return Multiple Media Image url */
    public String getMultipleMediaImgUrl(int i) {
        //Return the best image download url available
        if (response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                .getEdges().get(i).getNode().getDisplayResources().get(2).getSrc() != null) {
            return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                    .getEdges().get(i).getNode().getDisplayResources().get(2).getSrc();
        } else if (response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                .getEdges().get(i).getNode().getDisplayResources().get(1).getSrc() != null) {
            return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                    .getEdges().get(i).getNode().getDisplayResources().get(1).getSrc();
        } else if (response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                .getEdges().get(i).getNode().getDisplayResources().get(0).getSrc() != null) {
            return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                    .getEdges().get(i).getNode().getDisplayResources().get(0).getSrc();
        } else {
            return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                    .getEdges().get(i).getNode().getDisplayUrl();
        }
    }

    /*Method to return multiple media display url */
    public String getMultipleMediaDisplayUrl(int i) {
        if (response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren().getEdges().get(i)
                .getNode().getDisplayResources().get(0) != null) {
            return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren().getEdges().get(i)
                    .getNode().getDisplayResources().get(0).getSrc();
        }else{
            return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren().getEdges().get(i)
                    .getNode().getDisplayUrl();
        }

    }

    /* Method to return multiple media post Id */
    public String getMultipleMediaPostID(int i) {
        return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren().getEdges().get(i)
                .getNode().getId();
    }

    /* Method to return multiple Media Video Url */
    public String getMultipleMediaVidUrl(int i) {
        return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                .getEdges().get(i).getNode().getVideoUrl();
    }

    /*Method to check if the url contains a multiple media image or video */
    public boolean getMultipleMediaIsVid(int i) {
        return response.getGraphql().getShortcodeMedia().getEdgeSidecarToChildren()
                .getEdges().get(i).getNode().getIsVideo();
    }


}
