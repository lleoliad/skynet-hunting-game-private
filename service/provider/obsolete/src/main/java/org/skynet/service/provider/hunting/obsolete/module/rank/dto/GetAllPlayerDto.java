package org.skynet.service.provider.hunting.obsolete.module.rank.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllPlayerDto {

    //是否拉取所有数据
    private Boolean pullAllData;

}
