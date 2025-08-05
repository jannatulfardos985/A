package com.example.applizen.model;

public class data {
    private String title;
    private String description;
    private String skills;
    private String salary;
    private String id;    // this could be jobId already — but rename for clarity if needed
    private String date;
    private String uid;

    // 🔹 Add this line

    public data() {
        // Required empty constructor for Firebase
    }

    public data(String title, String description, String skills, String salary, String id, String date,String uid) {
        this.title = title;
        this.description = description;
        this.skills = skills;
        this.salary = salary;
        this.id = id;
        this.date = date;
        this.uid=uid;

    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



}