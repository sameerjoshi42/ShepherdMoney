package com.shepherdmoney.interviewproject.service;

import com.shepherdmoney.interviewproject.exception.InterviewProjectException;
import com.shepherdmoney.interviewproject.mapper.UserMapper;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Integer createUser(CreateUserPayload payload) {
        User user = this.userMapper.convertToUser(payload);
        this.userRepository.save(user);
        return user.getId();
    }

    @Override
    public void deleteUser(int userId) {

        Optional<User> optionalUser = this.userRepository.findById(userId);
        if(optionalUser.isEmpty()){
            throw new InterviewProjectException("user not found");
        }

        User user = optionalUser.get();
        this.userRepository.delete(user);
    }
}
