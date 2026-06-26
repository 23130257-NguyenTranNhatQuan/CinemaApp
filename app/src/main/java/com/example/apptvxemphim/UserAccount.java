package com.example.apptvxemphim;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class UserAccount {
    @DocumentId
    private String id;
    private String email;
    @PropertyName("full_name")
    private String fullName;
    private String phone;
    private String role;
    private String user;

    public UserAccount() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @PropertyName("full_name")
    public String getFullName() { return fullName; }
    @PropertyName("full_name")
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
}