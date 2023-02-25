package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;

public interface UserDataVOService {
    void insertUser(UserData newUserData);

    void updateUserData(UserData userData);

    void deleteUserData(String userUid);
}
