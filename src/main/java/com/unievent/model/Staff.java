package com.unievent.model;

public class Staff extends User {
    private String staffId;
    private int deptId;
    private String position;

    // Constructors
    public Staff() {}

    public Staff(String email, String password, String fullName, String phone,
                 String staffId, int deptId, String position) {
        super(email, password, fullName, phone);
        this.staffId = staffId;
        this.deptId = deptId;
        this.position = position;
    }

    // Getters and Setters
    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(int deptId) {
        this.deptId = deptId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
