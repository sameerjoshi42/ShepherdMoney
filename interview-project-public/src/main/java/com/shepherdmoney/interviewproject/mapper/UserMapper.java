package com.shepherdmoney.interviewproject.mapper;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private ModelMapper modelMapper;

    @Autowired
    public UserMapper(ModelMapper modelMapper){
        this.modelMapper = modelMapper;
    }

    public User convertToUser(CreateUserPayload createUserPayload){
        return this.modelMapper.map(createUserPayload,User.class);

    }
}
