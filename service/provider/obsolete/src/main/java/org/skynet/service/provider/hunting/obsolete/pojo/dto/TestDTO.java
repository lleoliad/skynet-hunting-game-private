package org.skynet.service.provider.hunting.obsolete.pojo.dto;

import org.skynet.service.provider.hunting.obsolete.pojo.entity.RangeInt;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TestDTO extends BaseDTO {

    private String segmentCollectionRef;

    private Integer chapterId;

    private RangeInt rangeInt;

    private String matchUid;

    private Integer matchId;
}
