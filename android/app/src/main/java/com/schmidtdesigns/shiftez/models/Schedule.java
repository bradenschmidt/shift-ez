package com.schmidtdesigns.shiftez.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Braden on 25/09/2014.
 */
public class Schedule {
	private static final String TAG = "Schedule";

	@Expose
	private String dep;
	@Expose
	private String image;
	@Expose
	private String store;
	@SerializedName("upload_dateTime")
	@Expose
	private String uploadDateTime;
	@SerializedName("user_id")
	@Expose
	private String userId;
	@SerializedName("user_name")
	@Expose
	private String userName;
	@Expose
	private Integer week;
	@Expose
	private Integer year;

	/**
	 *
	 * @return
	 * The dep
	 */
	public String getDep() {
		return dep;
	}

	/**
	 *
	 * @param dep
	 * The dep
	 */
	public void setDep(String dep) {
		this.dep = dep;
	}

	public Schedule withDep(String dep) {
		this.dep = dep;
		return this;
	}

	/**
	 *
	 * @return
	 * The image
	 */
	public String getImage() {
		return image;
	}

	/**
	 *
	 * @param image
	 * The image
	 */
	public void setImage(String image) {
		this.image = image;
	}

	public Schedule withImage(String image) {
		this.image = image;
		return this;
	}

	/**
	 *
	 * @return
	 * The store
	 */
	public String getStore() {
		return store;
	}

	/**
	 *
	 * @param store
	 * The store
	 */
	public void setStore(String store) {
		this.store = store;
	}

	public Schedule withStore(String store) {
		this.store = store;
		return this;
	}

	/**
	 *
	 * @return
	 * The uploadDateTime
	 */
	public String getUploadDateTime() {
		return uploadDateTime;
	}

	/**
	 *
	 * @param uploadDateTime
	 * The upload_dateTime
	 */
	public void setUploadDateTime(String uploadDateTime) {
		this.uploadDateTime = uploadDateTime;
	}

	public Schedule withUploadDateTime(String uploadDateTime) {
		this.uploadDateTime = uploadDateTime;
		return this;
	}

	/**
	 *
	 * @return
	 * The userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 *
	 * @param userId
	 * The user_id
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Schedule withUserId(String userId) {
		this.userId = userId;
		return this;
	}

	/**
	 *
	 * @return
	 * The userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 *
	 * @param userName
	 * The user_name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Schedule withUserName(String userName) {
		this.userName = userName;
		return this;
	}

	/**
	 *
	 * @return
	 * The week
	 */
	public Integer getWeek() {
		return week;
	}

	/**
	 *
	 * @param week
	 * The week
	 */
	public void setWeek(Integer week) {
		this.week = week;
	}

	public Schedule withWeek(Integer week) {
		this.week = week;
		return this;
	}

	/**
	 *
	 * @return
	 * The year
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 *
	 * @param year
	 * The year
	 */
	public void setYear(Integer year) {
		this.year = year;
	}

	public Schedule withYear(Integer year) {
		this.year = year;
		return this;
	}

	@Override
	public String toString() {
		return "Schedule{" +
				"dep='" + dep + '\'' +
				", image='" + image + '\'' +
				", store='" + store + '\'' +
				", uploadDateTime='" + uploadDateTime + '\'' +
				", userId='" + userId + '\'' +
				", userName='" + userName + '\'' +
				", week=" + week +
				", year=" + year +
				'}';
	}

	public static class List extends ArrayList<Schedule> {

		@Override
		public String toString() {
			StringBuilder res = new StringBuilder();

			res.append("Schedules:\n");

			int i = 0;
			for(Schedule s : this) {
				res.append(i + ": " + s.toString());

				i++;
			}

			return res.toString();
		}
	}
}
