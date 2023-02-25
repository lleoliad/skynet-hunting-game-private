package org.skynet.service.provider.hunting.obsolete.pojo.dto;


import io.swagger.annotations.ApiModel;
import lombok.*;

import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "AchievementDTO对象", description = "成就接收对象")
public class BattleScriptDTO {

    private String adminKey;

    private String gameVersion;

    private String userUid;

    private Integer chapterId;

    private Integer trophy;

    private Double winningProbability;

    private Integer mediaScore;

    private Integer chapterEnteredCount;

    private Integer winStreak = 0;

    private Integer loseStreak = 0;

    private Double cultivateScore;

    private Double firstPrecision;

    private Integer firstScore;

    private Double secondPrecision;

    private Integer secondScore;

    private Double thirdPrecision;

    private Integer thirdScore;

    private Double fourthPrecision;

    private Integer fourthScore;

    private Double fifthPrecision;

    private Integer fifthScore;

//    private Long animalRouteUid;

    private List<Integer> animalIds = new LinkedList<>();

    private List<Long> routeIds = new LinkedList<>();
}
