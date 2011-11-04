package com.opencredo.nugsample.service;

import com.opencredo.nugsample.domain.User;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Aleksa Vukotic
 */
@Service
@Transactional
public class SimpleUserService implements UserService {
    private final GraphDatabaseService graphDatabaseService;

    @Autowired
    public SimpleUserService(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }


    public User saveUser(User user) {
//        Transaction transaction = this.graphDatabaseService.beginTx();
//        try {
        Node node = null;
        Index<Node> userIndex = this.graphDatabaseService.index().forNodes("user");
        if(user.getId() == null){
            node = this.graphDatabaseService.createNode();
        }else{
            node = this.graphDatabaseService.getNodeById(user.getId());
            userIndex.remove(node, "firstName");
            userIndex.remove(node, "lastName");
            userIndex.remove(node, "type");

        }
        node.setProperty("type", User.class.getName());
        node.setProperty("firstName", user.getFirstName());
        node.setProperty("lastName", user.getLastName());
        node.setProperty("yearOfBirth", user.getYearOfBirth());


        userIndex.add(node, "firstName", user.getFirstName());
        userIndex.add(node, "lastName", user.getLastName());
        userIndex.add(node, "type", User.class.getName());


        user.setId(node.getId());

//            transaction.success();
        return user;
//        } catch (Exception e) {
//            transaction.failure();
//            throw new RuntimeException("Save failed");
//        } finally {
//            transaction.finish();
//        }
    }

    public List<User> getAllUsers() {
        IndexHits<Node> hits = this.graphDatabaseService.index().forNodes("user").query("type", User.class.getName());
        List<User> users = new ArrayList<User>();
        for (Node node : hits) {
            users.add(User.fromNode(node));
        }
        return users;
    }
}
