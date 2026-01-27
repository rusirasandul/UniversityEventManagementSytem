package com.unievent.model;

public class Department {
    private int deptId;
    private String deptName;

    // Constructors
    public Department() {}

    public Department(String deptName) {
        this.deptName = deptName;
    }

    // Getters and Setters
    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}
