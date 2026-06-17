package com.salon.customer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.consumption.entity.ConsumptionOrder;
import com.salon.consumption.entity.ConsumptionOrderItem;
import com.salon.consumption.entity.ServiceCard;
import com.salon.consumption.mapper.ConsumptionOrderItemMapper;
import com.salon.consumption.mapper.ConsumptionOrderMapper;
import com.salon.consumption.mapper.ServiceCardMapper;
import com.salon.consumption.service.ServiceCardService;
import com.salon.consumption.vo.ConsumptionOrderItemVO;
import com.salon.appointment.entity.Appointment;
import com.salon.appointment.mapper.AppointmentMapper;
import com.salon.appointment.service.AppointmentService;
import com.salon.appointment.vo.AppointmentVO;
import com.salon.consumption.vo.ConsumptionOrderVO;
import com.salon.consumption.vo.ServiceCardVO;
import com.salon.coupon.entity.Coupon;
import com.salon.customer.dto.BookingSubmitReqDTO;
import com.salon.customer.dto.CustomerPayReqDTO;
import com.salon.coupon.mapper.CouponMapper;
import com.salon.coupon.vo.CouponVO;
import com.salon.customer.dto.ProfileUpdateDTO;
import com.salon.customer.dto.RechargeReqDTO;
import com.salon.customer.dto.ServiceCardPurchaseReqDTO;
import com.salon.customer.service.CustomerAuthService;
import com.salon.customer.vo.BalanceRecordVO;
import com.salon.customer.vo.ServiceProgressVO;
import com.salon.member.dto.RechargeDTO;
import com.salon.member.entity.Member;
import com.salon.member.entity.RechargeRecord;
import com.salon.member.mapper.MemberMapper;
import com.salon.member.mapper.RechargeRecordMapper;
import com.salon.member.service.MemberService;
import com.salon.member.vo.MemberVO;
import com.salon.points.dto.PointsExchangeRecordReqDTO;
import com.salon.points.dto.PointsExchangeReqDTO;
import com.salon.points.service.PointsExchangeRecordService;
import com.salon.points.service.PointsProductService;
import com.salon.points.vo.PointsProductPageVO;
import com.salon.points.vo.PointsProductVO;
import com.salon.points.vo.PointsExchangeRecordVO;
import com.salon.groupbuy.dto.GroupBuyOrderReqDTO;
import com.salon.groupbuy.service.GroupBuyOrderService;
import com.salon.groupbuy.service.GroupBuyTemplateService;
import com.salon.groupbuy.vo.GroupBuyOrderPageVO;
import com.salon.groupbuy.vo.GroupBuyOrderVO;
import com.salon.groupbuy.vo.GroupBuyTemplatePageVO;
import com.salon.groupbuy.vo.GroupBuyTemplateVO;
import com.salon.rating.dto.ServiceRatingReqDTO;
import com.salon.rating.entity.ServiceRating;
import com.salon.rating.mapper.ServiceRatingMapper;
import com.salon.rating.vo.ServiceRatingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "顾客自助门户", description = "顾客端个人中心接口")
@RestController
@RequestMapping("/api/customer/portal")
@RequiredArgsConstructor
@Slf4j
public class CustomerPortalController {

    private final CustomerAuthService authService;
    private final MemberMapper memberMapper;
    private final ConsumptionOrderMapper orderMapper;
    private final ConsumptionOrderItemMapper orderItemMapper;
    private final CouponMapper couponMapper;
    private final MemberService memberService;
    private final ServiceCardService serviceCardService;
    private final ServiceCardMapper serviceCardMapper;
    private final RechargeRecordMapper rechargeRecordMapper;
    private final JdbcTemplate jdbcTemplate;
    private final PointsProductService pointsProductService;
    private final PointsExchangeRecordService exchangeRecordService;
    private final GroupBuyTemplateService groupBuyTemplateService;
    private final GroupBuyOrderService groupBuyOrderService;
    private final ServiceRatingMapper serviceRatingMapper;

    @Deprecated(since = "微服务迁移后移除")
    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public Result<MemberVO> profile(@RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        Member member = memberMapper.selectById(memberId);
        return Result.success(MemberVO.from(member));
    }

    @Operation(summary = "修改个人资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestHeader("X-Customer-Token") String token,
                                      @Valid @RequestBody ProfileUpdateDTO body) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            return Result.notFound("会员不存在");
        }
        if (body.getName() != null) {
            member.setName(body.getName());
        }
        if (body.getBirthday() != null) {
            member.setBirthday(body.getBirthday());
        }
        memberMapper.updateById(member);
        return Result.success();
    }

    @Operation(summary = "顾客在线预约")
    @PostMapping("/appointments")
    public Result<java.util.Map<String, Object>> createAppointment(
            @RequestHeader("X-Customer-Token") String token,
            @Valid @RequestBody BookingSubmitReqDTO req) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        // 查询服务项目获取时长和价格
        java.util.List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name, price, duration FROM service_item WHERE id = ? AND status = 1",
                req.getServiceItemId());
        if (rows.isEmpty()) {
            return Result.badRequest("服务项目不存在或已下架");
        }
        java.util.Map<String, Object> svc = rows.get(0);
        int duration = ((Number) svc.get("duration")).intValue();
        java.time.LocalTime endTime = req.getStartTime().plusMinutes(duration);

        // 如果指定了技师，检查时间冲突
        if (req.getEmployeeId() != null) {
            appointmentService.checkTimeConflict(req.getEmployeeId(), req.getDate(),
                    req.getStartTime(), endTime, null);
        }

        Appointment entity = new Appointment();
        entity.setMemberId(memberId);
        entity.setServiceItemId(req.getServiceItemId());
        entity.setEmployeeId(req.getEmployeeId());
        entity.setAppointmentDate(req.getDate());
        entity.setStartTime(req.getStartTime());
        entity.setEndTime(endTime);
        entity.setDuration(duration);
        entity.setStatus(1);

        appointmentService.save(entity);

        return Result.success(java.util.Map.of("id", entity.getId(), "message", "预约成功"));
    }

    @Operation(summary = "查询可用预约时段")
    @GetMapping("/slots")
    public Result<List<Map<String, Object>>> availableSlots(
            @RequestParam String date,
            @RequestParam Long serviceItemId) {
        List<Map<String, Object>> svcRows = jdbcTemplate.queryForList(
                "SELECT duration FROM service_item WHERE id = ? AND status = 1", serviceItemId);
        if (svcRows.isEmpty()) {
            return Result.badRequest("服务项目不存在或已下架");
        }
        int duration = ((Number) svcRows.get(0).get("duration")).intValue();

        LocalDate appointmentDate = LocalDate.parse(date);
        List<Map<String, Object>> appointments = jdbcTemplate.queryForList(
                "SELECT start_time, end_time FROM appointment " +
                "WHERE appointment_date = ? AND status IN (1, 2)", appointmentDate);

        List<Map<String, Object>> slots = new ArrayList<>();
        LocalTime slotStart = LocalTime.of(9, 0);
        LocalTime dayEnd = LocalTime.of(17, 30);

        while (!slotStart.isAfter(dayEnd)) {
            LocalTime slotEnd = slotStart.plusMinutes(duration);
            boolean available = true;
            for (Map<String, Object> appt : appointments) {
                LocalTime apptStart = (LocalTime) appt.get("start_time");
                LocalTime apptEnd = (LocalTime) appt.get("end_time");
                if (slotStart.isBefore(apptEnd) && slotEnd.isAfter(apptStart)) {
                    available = false;
                    break;
                }
            }
            slots.add(Map.of("time", slotStart.toString(), "available", available));
            slotStart = slotStart.plusMinutes(30);
        }
        return Result.success(slots);
    }

    @Operation(summary = "我的预约列表")
    @GetMapping("/my-appointments")
    public Result<List<AppointmentVO>> myAppointments(@RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        List<Appointment> list = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getMemberId, memberId)
                        .orderByDesc(Appointment::getAppointmentDate)
                        .orderByDesc(Appointment::getStartTime));
        return Result.success(list.stream().map(AppointmentVO::from).toList());
    }

    @Operation(summary = "取消预约")
    @PostMapping("/appointments/{id}/cancel")
    public Result<Void> cancelAppointment(@PathVariable Long id,
                                          @RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        int rows = jdbcTemplate.update(
            "UPDATE appointment SET status = 4 WHERE id = ? AND member_id = ? AND status = 1",
            id, memberId);
        if (rows == 0) {
            return Result.badRequest("仅可取消已预约状态的预约");
        }
        return Result.success();
    }

    @Operation(summary = "我的消费记录")
    @GetMapping("/orders")
    public Result<PageResult<ConsumptionOrderVO>> orders(
            @RequestHeader("X-Customer-Token") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "date") String sortBy) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        LambdaQueryWrapper<ConsumptionOrder> qw = new LambdaQueryWrapper<ConsumptionOrder>()
                .eq(ConsumptionOrder::getMemberId, memberId);
        // 日期范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            qw.ge(ConsumptionOrder::getCreateTime, java.time.LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            qw.le(ConsumptionOrder::getCreateTime, java.time.LocalDateTime.parse(endDate + "T23:59:59"));
        }
        // 排序
        if ("amount".equals(sortBy)) {
            qw.orderByDesc(ConsumptionOrder::getTotalAmount);
        } else {
            qw.orderByDesc(ConsumptionOrder::getCreateTime);
        }
        Page<ConsumptionOrder> p = new Page<>(page, size);
        Page<ConsumptionOrder> result = orderMapper.selectPage(p, qw);
        return Result.success(PageResult.of(result, ConsumptionOrderVO::from));
    }

    @Operation(summary = "消费详情")
    @GetMapping("/orders/{id}")
    public Result<ConsumptionOrderVO> orderDetail(@PathVariable Long id,
                                                 @RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        ConsumptionOrder order = orderMapper.selectById(id);
        if (order == null || !order.getMemberId().equals(memberId)) {
            return Result.badRequest("订单不存在");
        }
        ConsumptionOrderVO vo = ConsumptionOrderVO.from(order);
        List<ConsumptionOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<ConsumptionOrderItem>().eq(ConsumptionOrderItem::getOrderId, id));
        vo.setItems(items.stream().map(ConsumptionOrderItemVO::from).toList());
        return Result.success(vo);
    }

    @Operation(summary = "我的优惠券")
    @GetMapping("/coupons")
    public Result<List<CouponVO>> coupons(@RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        List<Coupon> list = couponMapper.selectList(
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getMemberId, memberId)
                        .orderByDesc(Coupon::getReceiveTime));
        return Result.success(list.stream().map(CouponVO::from).toList());
    }

    @Operation(summary = "顾客充值")
    @PostMapping("/recharge")
    public Result<MemberVO> recharge(@RequestHeader("X-Customer-Token") String token,
                                     @Valid @RequestBody RechargeReqDTO req) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        RechargeDTO dto = new RechargeDTO();
        dto.setAmount(req.getAmount());
        dto.setPayMethod(req.getPayMethod());
        Member member = memberService.recharge(memberId, dto);
        return Result.success(MemberVO.from(member));
    }

    @Operation(summary = "顾客余额支付")
    @PostMapping("/pay")
    @Transactional(rollbackFor = Exception.class)
    public Result<java.util.Map<String, Object>> pay(
            @RequestHeader("X-Customer-Token") String token,
            @Valid @RequestBody CustomerPayReqDTO req) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        if (!"BALANCE".equals(req.getPayMethod())) {
            return Result.badRequest("暂仅支持余额支付，微信/支付宝支付即将上线");
        }
        BigDecimal amount = req.getAmount().setScale(2, RoundingMode.HALF_UP);
        int recordRows = jdbcTemplate.update(
                "INSERT INTO recharge_record (member_id, amount, pay_method, remark, create_time) VALUES (?, ?, ?, ?, NOW())",
                memberId, amount.negate(), 8, "余额支付");
        if (recordRows == 0) {
            throw new BusinessException(ErrorCode.BALANCE_RECORD_WRITE_FAILED);
        }
        int rows = jdbcTemplate.update(
                "UPDATE member SET balance = balance - ? WHERE id = ? AND balance >= ?",
                amount, memberId, amount);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.MEMBER_BALANCE_INSUFFICIENT, "余额不足");
        }

        BigDecimal balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM member WHERE id = ?", BigDecimal.class, memberId);
        log.info("顾客余额支付成功: memberId={}, amount={}, balance={}", memberId, amount, balance);
        return Result.success(java.util.Map.of(
                "balance", balance,
                "message", "支付成功"));
    }

    @Operation(summary = "我的次卡")
    @GetMapping("/service-cards")
    public Result<List<ServiceCardVO>> serviceCards(@RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        List<ServiceCard> list = serviceCardMapper.selectList(
                new LambdaQueryWrapper<ServiceCard>()
                        .eq(ServiceCard::getMemberId, memberId)
                        .orderByDesc(ServiceCard::getCreateTime));
        return Result.success(list.stream().map(ServiceCardVO::from).toList());
    }

    @Operation(summary = "购买次卡")
    @PostMapping("/service-cards/purchase")
    public Result<ServiceCardVO> purchaseServiceCard(@RequestHeader("X-Customer-Token") String token,
                                                      @Valid @RequestBody ServiceCardPurchaseReqDTO req) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        // 从服务项目表获取单价，计算次卡总价
        BigDecimal unitPrice = jdbcTemplate.queryForObject(
                "SELECT price FROM service_item WHERE id = ?", BigDecimal.class, req.getServiceItemId());
        if (unitPrice == null) {
            return Result.badRequest("服务项目不存在");
        }
        ServiceCard card = new ServiceCard();
        card.setMemberId(memberId);
        card.setServiceItemId(req.getServiceItemId());
        card.setTotalCount(req.getTotalCount());
        card.setPrice(unitPrice.multiply(BigDecimal.valueOf(req.getTotalCount())));
        ServiceCard result = serviceCardService.purchase(card);
        return Result.success(ServiceCardVO.from(result));
    }

    @Operation(summary = "余额变动记录")
    @GetMapping("/balance-records")
    public Result<List<BalanceRecordVO>> balanceRecords(@RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        List<RechargeRecord> records = rechargeRecordMapper.selectList(
                new LambdaQueryWrapper<RechargeRecord>()
                        .eq(RechargeRecord::getMemberId, memberId)
                        .orderByDesc(RechargeRecord::getCreateTime));
        return Result.success(records.stream().map(BalanceRecordVO::from).toList());
    }

    @Operation(summary = "服务进度查询")
    @GetMapping("/progress")
    public Result<ServiceProgressVO> progress(@RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }

        // 1. 检查是否有进行中的服务计时
        List<Map<String, Object>> timers = jdbcTemplate.queryForList(
                "SELECT t.id, t.planned_duration, t.status, t.started_at, t.paused_at, " +
                "t.service_item_name, t.employee_id " +
                "FROM service_timer t WHERE t.member_id = ? AND t.status IN (1, 2) " +
                "ORDER BY t.started_at DESC LIMIT 1", memberId);

        if (!timers.isEmpty()) {
            Map<String, Object> timer = timers.get(0);
            int timerStatus = ((Number) timer.get("status")).intValue();
            int plannedDuration = ((Number) timer.get("planned_duration")).intValue();
            String serviceName = (String) timer.get("service_item_name");
            Long employeeId = (Long) timer.get("employee_id");
            java.time.LocalDateTime startedAt = (java.time.LocalDateTime) timer.get("started_at");

            // 获取技师姓名
            String techName = null;
            if (employeeId != null) {
                techName = jdbcTemplate.queryForObject(
                        "SELECT name FROM employee WHERE id = ?", String.class, employeeId);
            }

            boolean paused = timerStatus == 2;
            long elapsedSeconds = java.time.Duration.between(startedAt, java.time.LocalDateTime.now()).getSeconds();
            long remainingSeconds = Math.max(0, (long) plannedDuration * 60 - elapsedSeconds);

            return Result.success(ServiceProgressVO.inService(
                    techName != null ? techName : "未知技师",
                    serviceName != null ? serviceName : "未知服务",
                    elapsedSeconds, remainingSeconds, plannedDuration, paused));
        }

        // 2. 检查是否在排队中
        List<Map<String, Object>> queueEntries = jdbcTemplate.queryForList(
                "SELECT q.queue_number FROM service_queue q " +
                "WHERE q.member_id = ? AND q.status = 1 " +
                "ORDER BY q.queue_number ASC LIMIT 1", memberId);

        if (!queueEntries.isEmpty()) {
            int myNumber = ((Number) queueEntries.get(0).get("queue_number")).intValue();
            // 计算前面等待的人数
            Integer aheadCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM service_queue WHERE status = 1 AND queue_number < ? " +
                    "AND DATE(create_time) = CURDATE()", Integer.class, myNumber);
            return Result.success(ServiceProgressVO.waiting(myNumber, aheadCount != null ? aheadCount : 0));
        }

        // 3. 空闲
        return Result.success(ServiceProgressVO.idle());
    }

    // ──────────── 积分商城 ────────────

    @Operation(summary = "积分商城商品列表")
    @GetMapping("/points/products")
    public Result<List<PointsProductPageVO>> pointsProducts() {
        Page<PointsProductPageVO> p = pointsProductService.page(1, 100, null, 1);
        return Result.success(p.getRecords().stream()
            .filter(vo -> Integer.valueOf(0).compareTo(vo.getStockQty()) < 0)
            .toList());
    }

    @Operation(summary = "积分商城商品详情")
    @GetMapping("/points/products/{id}")
    public Result<PointsProductVO> pointsProductDetail(@PathVariable Long id) {
        return Result.success(pointsProductService.getById(id));
    }

    @Operation(summary = "积分兑换")
    @PostMapping("/points/exchange")
    public Result<PointsExchangeRecordVO> exchange(@RequestHeader("X-Customer-Token") String token,
                                                    @Valid @RequestBody PointsExchangeReqDTO req) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        PointsExchangeRecordReqDTO dto = new PointsExchangeRecordReqDTO();
        dto.setMemberId(memberId);
        dto.setProductId(req.getProductId());
        dto.setQuantity(req.getQuantity());
        return Result.success(exchangeRecordService.exchange(dto));
    }

    // ──────────── 拼团活动 ────────────

    @Operation(summary = "拼团活动列表")
    @GetMapping("/group-buy/templates")
    public Result<List<GroupBuyTemplatePageVO>> groupBuyTemplates() {
        Page<GroupBuyTemplatePageVO> p = groupBuyTemplateService.page(1, 100, null, 1);
        return Result.success(p.getRecords());
    }

    @Operation(summary = "拼团活动详情")
    @GetMapping("/group-buy/templates/{id}")
    public Result<GroupBuyTemplateVO> groupBuyTemplateDetail(@PathVariable Long id) {
        return Result.success(groupBuyTemplateService.getById(id));
    }

    @Operation(summary = "可参团的团单列表")
    @GetMapping("/group-buy/orders")
    public Result<List<GroupBuyOrderPageVO>> groupBuyOrders() {
        Page<GroupBuyOrderPageVO> p = groupBuyOrderService.page(1, 100, null, 1, null, null);
        // Only show orders that are still open (status=1) — filter in frontend or here
        return Result.success(p.getRecords().stream()
            .filter(vo -> Integer.valueOf(1).equals(vo.getStatus()))
            .toList());
    }

    @Operation(summary = "团单详情")
    @GetMapping("/group-buy/orders/{id}")
    public Result<GroupBuyOrderVO> groupBuyOrderDetail(@PathVariable Long id) {
        return Result.success(groupBuyOrderService.getDetail(id));
    }

    @Operation(summary = "开团")
    @PostMapping("/group-buy/orders")
    public Result<GroupBuyOrderVO> createGroupBuy(@RequestHeader("X-Customer-Token") String token,
                                                   @Valid @RequestBody GroupBuyOrderReqDTO req) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        return Result.success(groupBuyOrderService.create(memberId, req));
    }

    @Operation(summary = "参团")
    @PostMapping("/group-buy/orders/{id}/join")
    public Result<GroupBuyOrderVO> joinGroupBuy(@PathVariable Long id,
                                                 @RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        return Result.success(groupBuyOrderService.join(id, memberId));
    }

    // ──────────── 服务评价 ────────────

    @Operation(summary = "提交服务评价")
    @PostMapping("/orders/{id}/rate")
    public Result<ServiceRatingVO> rate(@PathVariable Long id,
                                        @RequestHeader("X-Customer-Token") String token,
                                        @Valid @RequestBody ServiceRatingReqDTO req) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        // 校验订单属于当前会员
        ConsumptionOrder order = orderMapper.selectById(id);
        if (order == null || !order.getMemberId().equals(memberId)) {
            return Result.badRequest("订单不存在");
        }
        // 检查是否已评价
        ServiceRating existing = serviceRatingMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ServiceRating>()
                .eq(ServiceRating::getOrderId, id));
        if (existing != null) {
            return Result.badRequest("该订单已评价");
        }
        ServiceRating rating = new ServiceRating();
        rating.setOrderId(id);
        rating.setMemberId(memberId);
        rating.setRating(req.getRating());
        rating.setTags(req.getTags());
        rating.setComment(req.getComment());
        serviceRatingMapper.insert(rating);
        return Result.success(ServiceRatingVO.from(rating));
    }

    @Operation(summary = "查询订单评价")
    @GetMapping("/orders/{id}/rate")
    public Result<ServiceRatingVO> getRate(@PathVariable Long id,
                                            @RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        ServiceRating rating = serviceRatingMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ServiceRating>()
                .eq(ServiceRating::getOrderId, id));
        return Result.success(ServiceRatingVO.from(rating));
    }

    @Operation(summary = "我的拼团")
    @GetMapping("/group-buy/my-orders")
    public Result<PageResult<GroupBuyOrderPageVO>> myGroupBuyOrders(
            @RequestHeader("X-Customer-Token") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        Page<GroupBuyOrderPageVO> p = groupBuyOrderService.myOrders(memberId, page, size);
        return Result.success(PageResult.of(p));
    }
}
