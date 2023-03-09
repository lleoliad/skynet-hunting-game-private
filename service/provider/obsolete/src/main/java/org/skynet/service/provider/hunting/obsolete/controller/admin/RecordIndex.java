// package org.skynet.service.provider.hunting.obsolete.controller.admin;
//
// import com.baomidou.mybatisplus.annotation.IdType;
// import com.baomidou.mybatisplus.annotation.TableField;
// import com.baomidou.mybatisplus.annotation.TableId;
// import com.baomidou.mybatisplus.annotation.TableName;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;
//
// import java.io.Serializable;
//
// @Data
// @AllArgsConstructor
// @NoArgsConstructor
// @TableName("record_index")
// public class RecordIndex implements Serializable {
//
//     private static final long serialVersionUID = 1L;
//
//     @TableId(value = "id", type = IdType.AUTO)
//     private Long id;
//
//     private String uid;
//
//     @TableField("`index`")
//     private String index;
//
//     private String playerUid;
//
//     private String gameVersion;
//
//     private Long recordVersion;
//
//     private String animalRouteUid;
//
//     private Long animalId;
//
//     private Long bulletId;
//
//     private Integer gunId;
//
//     private Integer gunLevel;
//
//     private Integer windId;
//
//     private Boolean isAnimalKilled;
//
//     private Integer finalScore;
//
//     private Double averageShowPrecision;
//
//     private Integer source;
//
//     private Long uploadTime;
//
//
// }
