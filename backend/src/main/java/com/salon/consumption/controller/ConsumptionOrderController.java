package com.salon.consumption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.annotation.AuditLog;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.consumption.dto.CreateOrderDTO;
import com.salon.consumption.dto.ResumeOrderReqDTO;
import com.salon.consumption.dto.SuspendOrderReqDTO;
import com.salon.consumption.entity.ConsumptionOrder;
import com.salon.consumption.entity.ConsumptionOrderItem;
import com.salon.consumption.mapper.ConsumptionOrderItemMapper;
import com.salon.consumption.service.ConsumptionOrderService;
import com.salon.consumption.vo.ConsumptionOrderItemVO;
import com.salon.consumption.vo.ConsumptionOrderPageVO;
import com.salon.consumption.vo.ConsumptionOrderVO;
import com.salon.consumption.vo.SuspendedOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 消费订单 Controller
 * <p>
 * 提供消费订单的创建、查询 REST API
 * </p>
 */
@Tag(name = "消费订单管理", description = "消费订单的创建和查询接口")
@RestController
@RequestMapping("/api/consumption")
@RequiredArgsConstructor
public class ConsumptionOrderController {

    private final ConsumptionOrderService consumptionOrderService;
    private final ConsumptionOrderItemMapper orderItemMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 分页查询消费订单
     *
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Operation(summary = "分页查询消费订单（含会员名/技师名/项目列表）")
    @GetMapping("/page")
    public Result<PageResult<ConsumptionOrderPageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String memberName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer payMethod) {

        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder();

        if (startDate != null && !startDate.isEmpty()) {
            where.append(" AND o.create_time >= ?");
            params.add(startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            where.append(" AND o.create_time <= ?");
            params.add(endDate + " 23:59:59");
        }
        if (memberName != null && !memberName.isEmpty()) {
            where.append(" AND m.name LIKE ?");
            params.add("%" + memberName + "%");
        }
        if (status != null) {
            where.append(" AND o.status = ?");
            params.add(status);
        }
        if (payMethod != null) {
            where.append(" AND o.pay_method = ?");
            params.add(payMethod);
        }

        String baseFrom = "FROM consumption_order o " +
                "LEFT JOIN member m ON o.member_id = m.id " +
                "LEFT JOIN employee e ON o.employee_id = e.id " +
                "WHERE 1=1" + where;

        String countSql = "SELECT COUNT(*)" + baseFrom;
        Long totalObj = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
        long total = totalObj != null ? totalObj : 0;

        int offset = (page - 1) * pageSize;
        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(offset);
        dataParams.add(pageSize);

        String dataSql = "SELECT o.id, o.member_id AS member_id, m.name AS member_name, " +
                "o.employee_id AS employee_id, e.name AS employee_name, " +
                "o.total_amount AS total_amount, o.pay_amount AS pay_amount, " +
                "o.balance_used AS balance_used, o.points_earned AS points_earned, " +
                "o.status, o.pay_remark AS pay_remark, o.coupon_discount AS coupon_discount, " +
                "o.commission_amount AS commission_amount, o.pay_method AS pay_method, " +
                "(SELECT GROUP_CONCAT(item_name SEPARATOR '、') " +
                " FROM consumption_order_item WHERE order_id = o.id) AS items, " +
                "o.create_time AS create_time, o.update_time AS update_time " +
                baseFrom +
                " ORDER BY o.create_time DESC LIMIT ?, ?";
        List<ConsumptionOrderPageVO> list = jdbcTemplate.query(dataSql,
                new BeanPropertyRowMapper<>(ConsumptionOrderPageVO.class), dataParams.toArray());

        PageResult<ConsumptionOrderPageVO> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setPages((long) Math.ceil(total * 1.0 / pageSize));
        return Result.success(result);
    }

    /**
     * 按会员ID分页查询消费记录
     *
     * @param memberId 会员ID
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @Operation(summary = "按会员ID查询消费记录", description = "分页查询指定会员的消费订单")
    @GetMapping("/member/{memberId}/page")
    public Result<PageResult<ConsumptionOrderVO>> pageByMember(
            @Parameter(description = "会员ID") @PathVariable Long memberId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<ConsumptionOrder> p = consumptionOrderService.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<ConsumptionOrder>()
                        .eq(ConsumptionOrder::getMemberId, memberId)
                        .orderByDesc(ConsumptionOrder::getCreateTime));
        return Result.success(PageResult.of(p, ConsumptionOrderVO::from));
    }

    /**
     * 查询订单明细
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    @Operation(summary = "查询订单明细", description = "根据订单ID查询所有服务项目明细")
    @GetMapping("/{orderId}/items")
    public Result<List<ConsumptionOrderItemVO>> getOrderItems(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        List<ConsumptionOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<ConsumptionOrderItem>()
                        .eq(ConsumptionOrderItem::getOrderId, orderId));
        return Result.success(items.stream().map(ConsumptionOrderItemVO::from).toList());
    }

    /**
     * 创建消费订单
     *
     * @param dto 创建订单请求（会员ID、支付方式、订单明细等）
     * @return 创建的订单
     */
    @AuditLog("创建消费订单 会员ID=#{dto.memberId}")
    @Operation(summary = "创建消费订单", description = "创建订单并更新会员余额和积分")
    @PostMapping
    public Result<ConsumptionOrderVO> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        return Result.success(ConsumptionOrderVO.from(consumptionOrderService.createOrder(dto)));
    }

    /**
     * 根据ID查询订单详情
     *
     * @param id 订单ID
     * @return 订单信息
     */
    @Operation(summary = "查询订单详情", description = "根据ID查询订单信息")
    @GetMapping("/{id}")
    public Result<ConsumptionOrderVO> getOrder(@PathVariable Long id) {
        ConsumptionOrder order = consumptionOrderService.getById(id);
        if (order == null) {
            return Result.notFound("订单不存在");
        }
        return Result.success(ConsumptionOrderVO.from(order));
    }

    @AuditLog("退款 订单ID=#{id}")
    @Operation(summary = "退款", description = "对订单进行退款，退还余额和积分")
    @PutMapping("/{id}/refund")
    public Result<ConsumptionOrderVO> refund(@PathVariable Long id) {
        return Result.success(ConsumptionOrderVO.from(consumptionOrderService.refundOrder(id)));
    }

    @AuditLog("挂单 会员ID=#{dto.memberId}")
    @Operation(summary = "挂单", description = "保存订单为草稿状态，不处理支付")
    @PostMapping("/suspend")
    public Result<ConsumptionOrderVO> suspendOrder(@Valid @RequestBody SuspendOrderReqDTO dto) {
        return Result.success(ConsumptionOrderVO.from(consumptionOrderService.suspendOrder(dto)));
    }

    @Operation(summary = "查询挂起订单", description = "查询所有状态为挂单的订单列表")
    @GetMapping("/suspended")
    public Result<List<SuspendedOrderVO>> listSuspended() {
        return Result.success(consumptionOrderService.listSuspended());
    }

    @AuditLog("取单结算 订单ID=#{id}")
    @Operation(summary = "取单结算", description = "加载挂单并完成支付")
    @PutMapping("/{id}/resume")
    public Result<ConsumptionOrderVO> resumeOrder(@PathVariable Long id, @Valid @RequestBody ResumeOrderReqDTO dto) {
        return Result.success(ConsumptionOrderVO.from(consumptionOrderService.resumeOrder(id, dto)));
    }
}



