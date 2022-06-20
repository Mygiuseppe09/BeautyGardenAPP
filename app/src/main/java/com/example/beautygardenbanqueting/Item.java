package com.example.beautygardenbanqueting;

import android.widget.RatingBar;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Item implements Comparable<Item> {
    // dichiaro gli attributi (che sono le chiavi in firebase)
    private String name, slogan, image, description;
    private Integer capacity, price;
    private HashMap<String, Float> reviews;

    public Item() {}

    public Item(String name, String slogan, String image, String description, Integer capacity, Integer price) {
        this.name = name;
        this.slogan = slogan;
        this.image = image;
        this.description = description;
        this.capacity = capacity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public HashMap<String, Float> getReviews() {
        return reviews;
    }

    public void setReviews(HashMap<String, Float> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public String toString() {
        return "Item {" +
                "name='" + name + '\'' +
                ", slogan='" + slogan + '\'' +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", capacity=" + capacity +
                ", price=" + price +
                ", reviews=" + reviews +
                '}';
    }

    @Override
    public int compareTo(Item o) {
        // restituisce 0 se uguli, 1 se l'oggetto "this" è più grande di quello passato a parametro, -1 se il contrario
        if (this.getName().equals(o.getName()) && this.getSlogan().equals(o.getSlogan()))
            return 0;
        else return 1;
    }
}
