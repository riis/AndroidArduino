package com.riis.androidarduino.bloodpressure;

import java.util.Random;

public class UserInfo {
	private int userID;
	private String firstName;
	private String lastName;
	private int age;
	private int weight;
	private int height;
	
	public UserInfo(String firstName, String lastName, int age, int weight, int height) {
		Random rand = new Random();
		userID = rand.nextInt();
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.weight = weight;
		this.height = height;
	}
	
	public UserInfo(int userID, String firstName, String lastName, int age, int weight, int height) {
		this.userID = userID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.weight = weight;
		this.height = height;
	}

	public int getUserID() {
		return userID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
