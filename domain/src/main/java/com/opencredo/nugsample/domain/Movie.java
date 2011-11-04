package com.opencredo.nugsample.domain;


import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * @author Aleksa Vukotic
 */
@NodeEntity
public class Movie {
    private String title;
    private int year;

    public Movie() {
    }

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
