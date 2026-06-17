package com.salon.member.vo;

import com.salon.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员视图对象
 * <p>
 * 会员信息的安全子集，不暴露 password、openid 等敏感字段
 * </p>
 */
@Data
@Schema(description = "会员视图")
public class MemberVO {

    /**
     * 从 Entity 转换为 VO（手机号自动脱敏）
     */
    public static MemberVO from(Member member) {
        if (member == null) return null;
        MemberVO vo = new MemberVO();
        BeanUtils.copyProperties(member, vo);
        // PII 脱敏
        if (vo.getPhone() != null) {
            vo.setPhone(vo.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        return vo;
    }

    @Schema(description = "会员ID")
    private Long id;

    @Schema(description = "会员姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别：0=未知 1=男 2=女")
    private Integer gender;

    @Schema(description = "会员等级")
    private Integer level;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "余额")
    private BigDecimal balance;

    @Schema(description = "会员标签")
    private String tags;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "生日类型：1=公历 2=农历")
    private Integer birthdayType;

    @Schema(description = "最后消费日期")
    private LocalDate lastConsumeDate;

    @Schema(description = "最后到店日期")
    private LocalDate lastVisitDate;

    @Schema(description = "累计消费金额")
    private BigDecimal totalSpent;

    @Schema(description = "到店次数")
    private Integer visitCount;

    @Schema(description = "RFM分层")
    private String rfmSegment;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
