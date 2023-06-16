package com.desay.iwan2.common.api.http.entity.response;

import java.math.BigDecimal;

/**
 * 根据日期返回gps经纬度
 * @author hxy
 * */
public class Location {

    private BigDecimal longitude;
    private BigDecimal latitude;
	private long distance;
	private float speed;
	public BigDecimal getLongitude() {
		return longitude;
	}
	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}
	public BigDecimal getLatitude() {
		return latitude;
	}
	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}
	public long getDistance() {
		return distance;
	}
	public void setDistance(long distance) {
		this.distance = distance;
	}
	public float getSpeed() {
		return speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
    
	
}