package com.unievent.model;

public class Student extends User {
    private String stdId;
    private int batchYear;
    private int deptId;

    // Constructors
    public Student() {}

    public Student(String email, String password, String fullName, String phone, 
                   String stdId, int batchYear, int deptId) {
        super(email, password, fullName, phone);
        this.stdId = stdId;
        this.batchYear = batchYear;
        this.deptId = deptId;
    }

    // Getters and Setters
    public String getStdId() {
        return stdId;
    }

    public void setStdId(String stdId) {
        this.stdId = stdId;
    }

    public int getBatchYear() {
        return batchYear;
    }

    public void setBatchYear(int batchYear) {
        this.batchYear = batchYear;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }
}
