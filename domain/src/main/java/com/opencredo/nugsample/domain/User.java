package com.opencredo.nugsample.domain;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.GraphProperty;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;
import org.springframework.data.neo4j.core.Direction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.Set;


/**
 * @author Aleksa Vukotic
 */
@Entity
@Table(name = "T_1")
@NodeEntity(partial = true)
public class User {
    @GraphId
    private Long id;

    @Id
    private Long dbID;

    @GraphProperty
    private String firstName;
    @Size(min = 5)
    @Indexed(indexName = "user")
    private String lastName;
    @Column
    private int yearOfBirth;


    @RelatedTo(type = "BEST_FRIENDS")
    private User bestFriend;

    @RelatedTo(type = "IS_FRIEND_OF")
    private Set<User> friends;

    @RelatedToVia(type = "HAS_SEEN", direction = Direction.OUTGOING, elementClass = Rating.class)
    private Set<Rating> ratings;

    @Query(value = "start n=(%start) match (n)-[:%relType]-(m)-[:%relType]-(friend),p = shortestPath(n-[*..2]->friend)  where (LENGTH(p)>1) return distinct friend",
                    params = {"relType", "IS_FRIEND_OF"},
                    type = QueryType.Cypher)
    private Iterable<User> friendsOfFriends;

    @Query(value = "start n=(%start) match (n)-[:%isFriend]-(m)-[%hasSeen]-(movie),p = shortestPath(n-[*..2]->movie)  where (LENGTH(p)>1) return distinct movie",
                    params = {"isFriend", "IS_FRIEND_OF", "hasSeen", "HAS_SEEN"})
    private Iterable<Movie> recommendations;

    //getters and setters
    public User() {
    }

    public User(String firstName, String lastName, int yearOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.yearOfBirth = yearOfBirth;
    }

    public User getBestFriend() {
        return bestFriend;
    }

    public void setBestFriend(User bestFriend){
//        this.relateTo(bestFriend, "BEST_FRIENDS" );
        this.bestFriend = bestFriend;
    }

    public void addFriend(User friend){
//        this.relateTo(friend, "IS_FRIEND_OF" );
        this.friends.add(friend);
    }

    public Set<Rating> getRatings() {
        return ratings;
    }

    public Set<User> getFriends() {
        return friends;
    }

    public void addRating(Movie movie, int starsAwarded){
//        Rating rating = new Rating();
//        rating.setUser(this);
//        rating.setMovie(movie);
//        rating.setStars(starsAwarded);
//        this.ratings.add(rating);
        Rating rating = this.relateTo(movie, Rating.class, "HAS_SEEN");
        rating.setStars(starsAwarded);
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(int yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Iterable<User> getFriendsOfFriends() {
        return friendsOfFriends;
    }

    public Iterable<User> getFriendsOfFriends2(){
        TraversalDescription traversalDescription = Traversal.description().relationships(DynamicRelationshipType.withName("IS_FRIEND_OF"), org.neo4j.graphdb.Direction.BOTH).evaluator(Evaluators.atDepth(2)).uniqueness(Uniqueness.NODE_PATH)
                .evaluator(new Evaluator() {
                    @Override
                    public Evaluation evaluate(Path path) {
                        PathFinder<Path> directFriends = GraphAlgoFactory.pathsWithLength(new RelationshipExpander() {
                            @Override
                            public Iterable<Relationship> expand(Node node) {
                                return node.getRelationships(DynamicRelationshipType.withName("IS_FRIEND_OF"), org.neo4j.graphdb.Direction.BOTH);
                            }

                            @Override
                            public RelationshipExpander reversed() {
                                return this;
                            }
                        }, 1);
                        Path singlePath = directFriends.findSinglePath(path.startNode(), path.endNode());
                        if (singlePath != null) {
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                        return Evaluation.INCLUDE_AND_CONTINUE;
                    }
                });
        return findAllByTraversal(User.class, traversalDescription);
    }

    public Iterable<Movie> getRecommendations() {
        return recommendations;
    }

    public static final User fromNode(Node node){
        if(node == null){
            return null;
        }
        User user = new User();
        user.setId(node.getId());
        user.setFirstName((String)node.getProperty("firstName"));
        user.setLastName((String) node.getProperty("lastName"));
        user.setYearOfBirth((Integer) node.getProperty("yearOfBirth"));
        return user;
    }




}
