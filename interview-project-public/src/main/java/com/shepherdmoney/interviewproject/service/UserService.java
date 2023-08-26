package com.shepherdmoney.interviewproject.service;

import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;

public interface UserService {

    Integer createUser(CreateUserPayload payload);

    void deleteUser(int userId);
}
