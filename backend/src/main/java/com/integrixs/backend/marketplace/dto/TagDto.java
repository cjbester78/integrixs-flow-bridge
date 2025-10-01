package com.integrixs.backend.marketplace.dto;

public class TagDto {
    private String name;
    private int count;

    // Default constructor
    public TagDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}