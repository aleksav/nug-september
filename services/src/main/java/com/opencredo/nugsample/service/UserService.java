package com.opencredo.nugsample.service;

import com.opencredo.nugsample.domain.User;

import java.util.List;

/**
 * @author Aleksa Vukotic
 */
public interface UserService {

    User saveUser(User user);

    Iterable<User> getAllUsers();


}
