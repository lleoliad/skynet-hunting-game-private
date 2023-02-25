package org.skynet.service.provider.hunting.obsolete.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.skynet.service.provider.hunting.obsolete.common.util.TimeUtils;
import org.skynet.service.provider.hunting.obsolete.dao.mapper.UserDataVOMapper;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.UserData;
import org.skynet.service.provider.hunting.obsolete.pojo.vo.UserDataVO;
import org.skynet.service.provider.hunting.obsolete.service.UserDataVOService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@Transactional
public class UserDataVOServiceImpl implements UserDataVOService {

    @Resource
    private UserDataVOMapper userDataVOMapper;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private UserDataVOService userDataVOService;


    @Override
    public void insertUser(UserData newUserData) {
        UserDataVO userDataVO = voConvert(newUserData);
        userDataVOMapper.insert(userDataVO);
    }

    @Override
    public void updateUserData(UserData userData) {
        try {
            UserDataVO userDataVO = voConvert(userData);
            if (userDataVO.getUserId() != null) {
                QueryWrapper<UserDataVO> wrapper = new QueryWrapper<>();
                wrapper.eq("user_id", userDataVO.getUserId());
                UserDataVO dataVO = userDataVOMapper.selectOne(wrapper);
                if (dataVO != null) {
                    userDataVOMapper.update(userDataVO, wrapper);
                } else {
                    userDataVOMapper.insert(userDataVO);
                }
            } else {
                userDataVOMapper.insert(userDataVO);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public UserDataVO voConvert(UserData userData) {
        try {
            UserDataVO record = new UserDataVO();
            if (userData.getId() != null) {
                record.setId(userData.getId());
            }
            if (userData.getUserId() != null) {
                record.setUserId(userData.getUserId());
            }
            record.setUuid(userData.getUuid());
            record.setName(userData.getName());
            //账户状态
            boolean isBlocking = false;
            Long unixTimeNow = TimeUtils.getUnixTimeNow();
            //查看是否被封禁
            if (userData.getServerOnly().getEndBlockTime() >= unixTimeNow) {
                isBlocking = true;
            }
            if (isBlocking) {
                record.setAccountStatus((byte) 1);
                //封禁时间
                record.getBlockTime()[0] = userData.getServerOnly().getStartBlockTime();
                record.setBlockStartTime(new Date(record.getBlockTime()[0]));
                record.getBlockTime()[1] = userData.getServerOnly().getEndBlockTime();
                record.setBlockEndTime(new Date(record.getBlockTime()[1]));
            } else {
                record.setAccountStatus((byte) 0);
                record.setBlockStartTime(null);
                record.setBlockEndTime(null);
            }

            record.setSignUpTime(userData.getSignUpTime());
            record.setCoin(userData.getCoin());
            record.setDiamond(userData.getDiamond());
            record.setTotalEarnedCoin(userData.getHistory().getTotalEarnedCoin());
            record.setTotalEarnedDiamond(userData.getHistory().getTotalEarnedDiamond());
            record.setTrophy(userData.getTrophy());
            record.setHighestTrophyCount(userData.getHistory().getHighestTrophyCount());
            record.setAccumulateMoneyPaid(userData.getHistory().getAccumulateMoneyPaid());

            //总共的游戏对局

            Map<Integer, Integer> chapterCompletedCountMap = userData.getChapterCompletedCountMap();
            AtomicInteger sum = new AtomicInteger();
            AtomicInteger winCount = new AtomicInteger();
            double win = 0d;
            if (chapterCompletedCountMap.size() != 0) {
                chapterCompletedCountMap.forEach((k, v) -> sum.addAndGet(v));

                //总胜率
                Map<Integer, Integer> chapterWinCountMap = userData.getChapterWinCountMap();

                chapterWinCountMap.forEach((k, v) -> winCount.addAndGet(v));
                win = winCount.intValue() * 1.0 / sum.intValue();

            }
            record.setTotalGames(sum.intValue());
            record.setWinningProbability(win);
            //胜利局数
            record.setWinGames(winCount.intValue());
            record.setMatchAverageHitPrecision(userData.getHistory().getMatchAverageHitPrecision());

            //目前只有单个服务器、后期更新记得修改
            record.setServerInfoNum("1001");
            //章节的数量就是已经解锁的最高章节
            record.setHighestUnlockChapterId(userData.getUnlockedChapterIds().size());
            return record;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Override
    public void deleteUserData(String userUid) {
        QueryWrapper<UserDataVO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", userUid);
        userDataVOMapper.delete(queryWrapper);
    }
}
