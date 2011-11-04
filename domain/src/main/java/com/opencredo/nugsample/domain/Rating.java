package com.opencredo.nugsample.domain;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

/**
 * @author Aleksa Vukotic
 */
@RelationshipEntity
public class Rating {

    private int stars;

    @StartNode
    private User user;
    @EndNode
    private Movie movie;

    public Rating(int stars, User user, Movie movie) {
        this.stars = stars;
        this.user = user;
        this.movie = movie;
    }

    public Rating() {
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public User getUser() {
        return user;
    }

    public Movie getMovie() {
        return movie;
    }
}
