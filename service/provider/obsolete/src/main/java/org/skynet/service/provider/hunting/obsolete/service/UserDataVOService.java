package org.skynet.service.provider.hunting.obsolete.service;

import org.skynet.commons.hunting.user.dao.entity.UserData;

public interface UserDataVOService {
    void insertUser(UserData newUserData);

    void updateUserData(UserData userData);

    void deleteUserData(String userUid);
}
