// package org.skynet.service.provider.hunting.obsolete.pojo.entity;
//
// import io.swagger.annotations.ApiModel;
// import io.swagger.annotations.ApiModelProperty;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.EqualsAndHashCode;
// import lombok.NoArgsConstructor;
//
// import java.io.Serializable;
//
//
// @Data
// @EqualsAndHashCode(callSuper = false)
// @ApiModel(value = "FreeChestData对象", description = "免费箱子数据")
// @AllArgsConstructor
// @NoArgsConstructor
// public class FreeChestData extends ChestData implements Serializable {
//
//     private static final long serialVersionUID = 1L;
//
//     @ApiModelProperty("剩余可用时间")
//     Long availableUnixTime;
//
//
//     public FreeChestData(String uid, Integer chestType, Integer level, Long createTime, Long availableUnixTime) {
//         super(uid, chestType, level, createTime);
//         this.availableUnixTime = availableUnixTime;
//     }
// }
