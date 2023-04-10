package org.skynet.service.provider.hunting.login.service;

import org.skynet.commons.lang.common.Result;
import org.skynet.service.provider.hunting.login.data.LoginVO;
import org.skynet.service.provider.hunting.login.query.LoginQuery;

public interface LoginService {
    Object login(LoginQuery loginQuery);
}
