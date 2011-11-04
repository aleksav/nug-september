package com.opencredo.nugsample.service;
import com.opencredo.nugsample.domain.User;
import com.opencredo.nugsample.repository.SpringDataUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aleksa Vukotic
 */
@Service
@Transactional
public class SpringDataUserService implements UserService{
    private final SpringDataUserRepository userRepository;

    @Autowired
    public SpringDataUserService(SpringDataUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User saveUser(User user) {
        this.userRepository.
        return this.userRepository.save(user);
//        return user.persist();
    }

    @Override
    public Iterable<User> getAllUsers() {
        return this.userRepository.findAll();
    }
}
