package com.salon.member.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.salon.member.dto.MemberQueryDTO;
import com.salon.member.dto.MemberReqDTO;
import com.salon.member.dto.RechargeDTO;
import com.salon.member.entity.Member;

/**
 * 会员 Service 接口
 * <p>
 * 定义会员管理的业务方法
 * </p>
 */
public interface MemberService extends IService<Member> {

    /**
     * 分页查询会员列表
     * <p>
     * 支持按姓名和手机号模糊查询
     * </p>
     *
     * @param query 查询条件（姓名、手机号、分页参数）
     * @return 分页结果
     */
    IPage<Member> pageQuery(MemberQueryDTO query);

    /**
     * 新增会员
     *
     * @param req 会员信息
     * @return 新增后的会员
     */
    Member createMember(MemberReqDTO req);

    /**
     * 修改会员
     *
     * @param id  会员ID
     * @param req 会员信息
     * @return 修改后的会员
     */
    Member updateMember(Long id, MemberReqDTO req);

    /**
     * 删除会员
     *
     * @param id 会员ID
     */
    void deleteMember(Long id);

    /**
     * 增加会员积分并自动升级等级
     * <p>
     * 在消费完成后调用，根据会员当前积分自动匹配对应的等级
     * </p>
     *
     * @param memberId 会员ID
     * @param points   新增积分
     */
    void addPointsAndUpgrade(Long memberId, int points);

    /** 会员充值 */
    Member recharge(Long memberId, RechargeDTO dto);

    /** 流失预警：查询超过 N 天未消费的会员 */
    java.util.List<Member> getChurnRiskMembers(int days);
}


