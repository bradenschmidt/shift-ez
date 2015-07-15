package com.schmidtdesigns.shiftez.models;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Braden on 25/09/2014.
 */
public class Schedule {
	private static final String TAG = "Schedule";

	@SerializedName("user_name")
	@Expose
	private String userName;
	@SerializedName("store_name")
	@Expose
	private String storeName;
	@SerializedName("dep_name")
	@Expose
	private String depName;
	@SerializedName("image_url")
	@Expose
	private String image_url;
	@SerializedName("upload_dateTime")
	@Expose
	private String uploadDateTime;
	@SerializedName("upload_user_id")
	@Expose
	private String uploadUserId;
	@Expose
	private Integer week;
	@SerializedName("week_offset")
	@Expose
	private Integer weekOffset;
	@Expose
	private Integer year;

	// Serialize a single object.
	public static String serializeToJson(Schedule s) {
		Gson gson = new Gson();
		return gson.toJson(s);
	}

	//Using gson to deserialize to a single object.
	// Deserialize to single object.
	public static Schedule deserializeFromJson(String jsonString) {
		Gson gson = new Gson();
		return gson.fromJson(jsonString, Schedule.class);
	}

	public ArrayList<String> asStringList() {
		ArrayList<String> values = new ArrayList<>();
		values.add("Store: " + storeName);
		values.add("Department: " + depName);
		values.add("Year: " + year);
		values.add("Week: " + week);
		values.add("Uploaded By: " + userName);
		values.add("Upload Date: " + uploadDateTime);

		return values;
	}

	public int getWeekOffset() {
		return weekOffset;
	}

	public String getImageUrl() {
		return image_url;
	}

	public String getUploadUserId() {
		return uploadUserId;
	}

	public String getStoreName() {
		return storeName;
	}

	public String getDepName() {
		return depName;
	}

	public String getUserName() {
		return userName;
	}

	public Integer getYear() {
		return year;
	}

	public Integer getWeek() {
		return week;
	}

	public String getUploadDateTime() {
		return uploadDateTime;
	}

	@Override
	public String toString() {
		return "Schedule{" +
				"userName='" + userName + '\'' +
				", storeName='" + storeName + '\'' +
				", depName='" + depName + '\'' +
				", image_url='" + image_url + '\'' +
				", uploadDateTime='" + uploadDateTime + '\'' +
				", uploadUserId='" + uploadUserId + '\'' +
				", week=" + week +
				", weekOffset=" + weekOffset +
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

	public class Response {

		@SerializedName("schedules")
		private Schedule.List schedules;

		public Schedule.List getSchedules() {
			return schedules;
		}

		public void setSchedules(Schedule.List schedules) {
			this.schedules = schedules;
		}

		@Override
		public String toString() {
			return "ScheduleResponse{" +
					"schedules=" + schedules +
					'}';
		}
	}
}
