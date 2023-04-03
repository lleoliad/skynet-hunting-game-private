package org.skynet.service.provider.hunting.game.service;

import org.skynet.commons.lang.common.Result;
import org.skynet.components.hunting.game.query.MatchConsumeBulletQuery;
import org.skynet.components.hunting.game.query.MatchCompleteQuery;

public interface MatchAssignmentService {

    Result<?> consumeBullet(MatchConsumeBulletQuery matchConsumeBulletQuery);
    Result<?> complete(MatchCompleteQuery matchCompleteQuery);
}
