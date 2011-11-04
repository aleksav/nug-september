package com.opencredo.nugsample.service;

import com.google.common.collect.Iterables;
import com.opencredo.nugsample.domain.Movie;
import com.opencredo.nugsample.domain.Rating;
import com.opencredo.nugsample.domain.User;
import com.opencredo.nugsample.repository.SpringDataUserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.cypher.commands.Query;
import org.neo4j.cypher.javacompat.CypherParser;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.IndexHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Aleksa Vukotic
 */

@ContextConfiguration(locations = {"classpath*:/META-INF/spring/module-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
@Transactional
public class SimpleUserServiceTest {
    @Autowired
//    @Qualifier("simpleUserService")
    @Qualifier("springDataUserService")
    UserService userService;

    @Autowired
    SpringDataUserRepository springDataUserRepository;

    @Autowired
    GraphDatabaseService graphDatabaseService;

    @Test
    public void testSaveUser() {
        User user = new User("Aleksa", "V", 1979);

        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser.getId());

        String indexname = "user";//User.class.getSimpleName();
        IndexHits<Node> hits = this.graphDatabaseService.index().forNodes(indexname).get("firstName", "Aleksa");
        assertThat(hits.size(), equalTo(1));

    }

    @Test
    public void testUpdateUser() {
        User user = new User("Aleksa", "V", 1979);

        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser.getId());

        String indexname = "user";//User.class.getSimpleName();
        IndexHits<Node> hits = this.graphDatabaseService.index().forNodes(indexname).get("firstName", "Aleksa");
        assertThat(hits.size(), equalTo(1));

        savedUser.setFirstName("Andy");
        User updatedUser = this.userService.saveUser(savedUser);
        hits = this.graphDatabaseService.index().forNodes(indexname).get("firstName", "Andy");
        assertThat(hits.size(), equalTo(1));


    }

    @Test
    public void testBestFriend() {
        User user1 = new User("Aleksa", "V", 1979);
        user1.persist();

        User user2 = new User("John", "S", 1978);
        user2.setYearOfBirth(1978);
        user2.persist();

        user1.setBestFriend(user2);


        Node user1Node = this.graphDatabaseService.getNodeById(user1.getId());
        Relationship bestFriendRel = user1Node.getSingleRelationship(DynamicRelationshipType.withName("BEST_FRIENDS"), Direction.OUTGOING);
        assertNotNull(bestFriendRel);
        assertThat(bestFriendRel.getStartNode().getId(), equalTo(user1.getId()));
        assertThat(bestFriendRel.getEndNode().getId(), equalTo(user2.getId()));
        assertThat((String) bestFriendRel.getEndNode().getProperty("firstName"), equalTo("John"));

        assertThat(user1.getBestFriend().getFirstName(), equalTo("John"));
    }


    @Test
    public void testFriends() {
        User user1 = new User("Aleksa", "V", 1979);
        user1.persist();

        User user2 = new User("Kate", "S", 1982);
        user2.persist();

        User user3 = new User();
        user3.setFirstName("Jim");
        user3.setLastName("X");
        user3.setYearOfBirth(1971);
        user3.persist();

        user1.addFriend(user2);
        user1.addFriend(user3);


        Node user1Node = this.graphDatabaseService.getNodeById(user1.getId());
        Iterable<Relationship> friendRelationships = user1Node.getRelationships(DynamicRelationshipType.withName("IS_FRIEND_OF"), Direction.BOTH);
        assertNotNull(friendRelationships);
        assertThat(Iterables.size(friendRelationships), equalTo(2));


        assertThat(user1.getFriends().size(), equalTo(2));
    }

    @Test
    public void testMovies() {
        User user = new User("Aleksa", "V", 1979);
        user.persist();
        Movie movie = new Movie("Fargo", 1996);
        movie.persist();

        user.addRating(movie, 5);

        Node user1Node = this.graphDatabaseService.getNodeById(user.getId());
        Iterable<Relationship> ratingRelationships = user1Node.getRelationships(DynamicRelationshipType.withName("HAS_SEEN"), Direction.OUTGOING);
        assertNotNull(ratingRelationships);
        Iterator<Relationship> iterator = ratingRelationships.iterator();
        Relationship relationship = iterator.next();
        assertThat(relationship.getStartNode().getId(), equalTo(user.getId()));
        assertThat(relationship.getEndNode().getId(), equalTo(movie.getNodeId()));
        assertThat((Integer) relationship.getProperty("stars"), equalTo(5));
        assertThat(iterator.hasNext(), equalTo(false));

        assertThat(user.getRatings().size(), equalTo(1));
        Rating rating = user.getRatings().iterator().next();
        assertThat(rating.getStars(), equalTo(5));
        assertThat(rating.getMovie().getNodeId(), equalTo(movie.getNodeId()));
        assertThat(rating.getUser().getId(), equalTo(user.getId()));
    }

    @Test
    public void testFriendsOfFriends() {
        User user1 = new User("Aleksa", "V", 1979);
        user1.persist();

        User user2 = new User("John", "S", 1978);
        user2.persist();

        User user3 = new User("Kate", "S", 1984);
        user3.persist();


        User user4 = new User("Mr", "T", 1955);
        user4.persist();

        User user5 = new User("Mr", "Neo", 1979);
        user5.persist();

        user1.addFriend(user2);
        user1.addFriend(user3);

        user2.addFriend(user3);

        user3.addFriend(user4);


        Iterator<User> friendsIterator = user1.getFriendsOfFriends().iterator();
        assertThat(friendsIterator.next().getId(), equalTo(user4.getNodeId()));
        assertThat(friendsIterator.hasNext(), equalTo(false));


    }



    @Test
    public void testRecommendations() {
        User user1 = new User("Aleksa", "V", 1979);
        user1.persist();

        User user2 = new User("John", "S", 1978);
        user2.persist();

        user1.addFriend(user2);
        Movie movie1 = new Movie("Pulp Fiction", 1994);
        movie1.persist();

        Movie movie2 = new Movie("The Matrix", 1999);
        movie2.persist();

        Movie movie3 = new Movie("Fargo", 1996);
        movie3.persist();

        user1.addRating(movie2, 4);
        user1.addRating(movie2, 3);

        user2.addRating(movie2, 4);
        user2.addRating(movie3, 5);

        Iterator<Movie> recommendationsIterator = this.springDataUserRepository.findRecommendedMoviesForUser(user1).iterator();
        assertThat(recommendationsIterator.next().getNodeId(), equalTo(movie3.getNodeId()));
        assertThat(recommendationsIterator.hasNext(), equalTo(false));

    }
}
