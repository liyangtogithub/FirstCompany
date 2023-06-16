package com.desay.iwan2.common.api.http.entity.request;

/**
 * Created by 方奕峰 on 14-5-30.
 */
public class CommitSocialShare {
    private String username;
    private String gevent;
    private String socialID;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

	public String getGevent()
	{
		return gevent;
	}

	public void setGevent(String gevent)
	{
		this.gevent = gevent;
	}

	public String getSocialID()
	{
		return socialID;
	}

	public void setSocialID(String socialID)
	{
		this.socialID = socialID;
	}

}
