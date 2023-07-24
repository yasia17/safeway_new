package com.example.newas;


import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String FirstName;
    private String LastName;
    private Integer Age;
    private String Email;
    private String Password;

    public  User(){

    }

    public User(String firstName, String lastName, Integer age, String email, String password) {
        this.FirstName = firstName;
        this.LastName = lastName;
        this.Age = age;
        this.Email = email;
        this.Password = password;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public Integer getAge() {
        return Age;
    }

    public void setAge(Integer age) {
        Age = age;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    @Override
    public String toString() {
        return "com.example.myapplicationsasdas.User{" +
                "name='" + FirstName + '\'' +
                ", email='" + Email + '\'' +
                ", password='" + Password + '\'' +
                '}';
    }
}
