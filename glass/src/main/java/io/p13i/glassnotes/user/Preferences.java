package io.p13i.glassnotes.user;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class Preferences implements Serializable {
    @SerializedName("savePeriodMs")
    public int mSavePeriodMs;

    @SerializedName("preferredDataStoreName")
    public String mDataStoreName;

    @SerializedName("githubOwnerAndRepo")
    public String mOwnerAndRepo;

    @SerializedName("githubAccessToken")
    public String mGitHubAccessToken;

    public String getOwner() {
        return mOwnerAndRepo.split("/")[0];
    }

    public String getRepo() {
        return mOwnerAndRepo.split("/")[1];
    }

    public String getGitHubOAuthToken() {
        return mGitHubAccessToken;
    }
}
