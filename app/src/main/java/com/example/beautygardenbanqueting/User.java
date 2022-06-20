package com.example.beautygardenbanqueting;

import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private String name, surname, email;
    private boolean isSuperuser;
    private HashMap<String, Item> wishlist;


    public User() {

    }

    public User(String name, String surname, String email, boolean isSuperuser) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.isSuperuser = isSuperuser;

    }

    public HashMap<String, Item> getWishlist() {
        return wishlist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isSuperuser() {
        return isSuperuser;
    }

    public void setSuperuser(boolean superuser) {
        isSuperuser = superuser;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", isSuperuser=" + isSuperuser +
                '}';
    }
}
