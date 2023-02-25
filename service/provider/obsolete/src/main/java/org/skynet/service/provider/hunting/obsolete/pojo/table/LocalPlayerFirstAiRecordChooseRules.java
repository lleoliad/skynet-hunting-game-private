package org.skynet.service.provider.hunting.obsolete.pojo.table;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "LocalPlayerFirstAiRecordChooseRules对象", description = "AI操作选择规则集合")
public class LocalPlayerFirstAiRecordChooseRules implements Serializable {

    private static final long serialVersionUID = 1L;

    Map<String, LocalPlayerFirstAiRecordChooseRule> localPlayerFirstAiRecordChooseRules;
}
