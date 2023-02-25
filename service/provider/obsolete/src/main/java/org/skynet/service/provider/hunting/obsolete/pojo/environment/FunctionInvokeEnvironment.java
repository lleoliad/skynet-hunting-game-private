package org.skynet.service.provider.hunting.obsolete.pojo.environment;

import org.skynet.service.provider.hunting.obsolete.enums.ABTestGroup;
import org.skynet.service.provider.hunting.obsolete.enums.ClientGameVersion;
import org.skynet.service.provider.hunting.obsolete.enums.PlatformName;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserDataSendToClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunctionInvokeEnvironment {

    private String _clientGameVersionString = "";

    private ClientGameVersion _clientGameVersion = ClientGameVersion._1_0_1_;

    private UserData _userData = null;

    private String _userUid;

    private Integer _userDataUpdateCount;

    private Integer _requestId;

    private UserDataSendToClient _sendToClientUserData = null;

    private String _platformName = PlatformName.Android.getPlatform();

    private ABTestGroup _userABTestGroup = null;

    private Boolean _isFastForwarding = false;

    private Boolean _isAdmin = false;

    private Boolean _userFromUnityEditor = false;

    private Integer _serverTimeOffset = 0;

    private Boolean _isFunctionLocked = false;


}
