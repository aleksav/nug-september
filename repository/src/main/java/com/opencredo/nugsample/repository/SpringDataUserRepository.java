package com.opencredo.nugsample.repository;

import com.opencredo.nugsample.domain.Movie;
import com.opencredo.nugsample.domain.User;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.AbstractGraphRepository;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author Aleksa Vukotic
 */
public interface SpringDataUserRepository extends GraphRepository<User>{
}
//    @Query(value = "start n=(%start) match (n)-[:IS_FRIEND_OF]-(m)-[HAS_SEEN]-(movie),p = shortestPath(n-[*..2]->movie)  where (LENGTH(p)>1) return distinct movie",
//                    params = {"isFriend", "IS_FRIEND_OF", "hasSeen", "HAS_SEEN"}, type = QueryType.Cypher)
//    Iterable<Movie> findRecommendedMoviesForUser(@Param("start")User user);
