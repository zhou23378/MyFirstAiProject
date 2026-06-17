package com.salon.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.inventory.dto.ReturnOrderItemDTO;
import com.salon.inventory.dto.ReturnOrderReqDTO;
import com.salon.inventory.entity.ReturnOrder;
import com.salon.inventory.entity.ReturnOrderItem;
import com.salon.inventory.entity.Supplier;
import com.salon.inventory.mapper.ReturnOrderItemMapper;
import com.salon.inventory.mapper.ReturnOrderMapper;
import com.salon.inventory.mapper.SupplierMapper;
import com.salon.inventory.service.ReturnOrderService;
import com.salon.inventory.vo.ReturnOrderItemVO;
import com.salon.inventory.vo.ReturnOrderPageVO;
import com.salon.inventory.vo.ReturnOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReturnOrderServiceImpl extends ServiceImpl<ReturnOrderMapper, ReturnOrder> implements ReturnOrderService {

    private final ReturnOrderItemMapper itemMapper;
    private final SupplierMapper supplierMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnOrderVO create(ReturnOrderReqDTO dto, String applicant) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = String.format("%05d", System.currentTimeMillis() % 100000);
        String orderNo = "RO-" + datePart + "-" + suffix;

        int itemCount = dto.getItems().size();
        int totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReturnOrderItemDTO item : dto.getItems()) {
            totalQty += item.getQty();
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(item.getQty())));
        }
        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);

        ReturnOrder order = new ReturnOrder();
        order.setOrderNo(orderNo);
        order.setSupplierId(dto.getSupplierId());
        order.setOriginalOrderId(dto.getOriginalOrderId());
        order.setStatus(0);
        order.setTotalAmount(totalAmount);
        order.setItemCount(itemCount);
        order.setTotalQty(totalQty);
        order.setReason(dto.getReason());
        order.setApplicant(applicant);
        order.setRemark(dto.getRemark());
        save(order);

        List<ReturnOrderItem> entities = new ArrayList<>();
        for (ReturnOrderItemDTO item : dto.getItems()) {
            ReturnOrderItem ei = new ReturnOrderItem();
            ei.setOrderId(order.getId());
            ei.setProductId(item.getProductId());
            ei.setProductName(item.getProductName());
            ei.setUnit(item.getUnit());
            ei.setQty(item.getQty());
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            ei.setPrice(price);
            ei.setTotalAmount(price.multiply(BigDecimal.valueOf(item.getQty())).setScale(2, RoundingMode.HALF_UP));
            entities.add(ei);
        }
        batchInsertItems(entities);

        return toVO(order, entities);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnOrderVO update(Long id, ReturnOrderReqDTO dto) {
        ReturnOrder order = getById(id);
        if (order == null) throw new BusinessException(ErrorCode.RETURN_ORDER_NOT_FOUND);
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.RETURN_ORDER_STATUS_INVALID, "只有草稿状态可编辑");
        }

        int itemCount = dto.getItems().size();
        int totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReturnOrderItemDTO item : dto.getItems()) {
            totalQty += item.getQty();
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(item.getQty())));
        }
        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);

        order.setSupplierId(dto.getSupplierId());
        order.setOriginalOrderId(dto.getOriginalOrderId());
        order.setTotalAmount(totalAmount);
        order.setItemCount(itemCount);
        order.setTotalQty(totalQty);
        order.setReason(dto.getReason());
        order.setRemark(dto.getRemark());
        updateById(order);

        itemMapper.delete(new LambdaQueryWrapper<ReturnOrderItem>().eq(ReturnOrderItem::getOrderId, id));
        List<ReturnOrderItem> entities = new ArrayList<>();
        for (ReturnOrderItemDTO item : dto.getItems()) {
            ReturnOrderItem ei = new ReturnOrderItem();
            ei.setOrderId(id);
            ei.setProductId(item.getProductId());
            ei.setProductName(item.getProductName());
            ei.setUnit(item.getUnit());
            ei.setQty(item.getQty());
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            ei.setPrice(price);
            ei.setTotalAmount(price.multiply(BigDecimal.valueOf(item.getQty())).setScale(2, RoundingMode.HALF_UP));
            entities.add(ei);
        }
        batchInsertItems(entities);

        return toVO(order, entities);
    }

    @Override
    public ReturnOrderVO getDetail(Long id) {
        ReturnOrder order = getById(id);
        if (order == null) throw new BusinessException(ErrorCode.RETURN_ORDER_NOT_FOUND);
        List<ReturnOrderItem> items = itemMapper.selectList(
            new LambdaQueryWrapper<ReturnOrderItem>().eq(ReturnOrderItem::getOrderId, id));
        return toVO(order, items);
    }

    @Override
    public Page<ReturnOrderPageVO> page(int page, int pageSize, Integer status, Long supplierId) {
        LambdaQueryWrapper<ReturnOrder> wrapper = new LambdaQueryWrapper<ReturnOrder>()
            .eq(status != null, ReturnOrder::getStatus, status)
            .eq(supplierId != null, ReturnOrder::getSupplierId, supplierId)
            .orderByDesc(ReturnOrder::getCreateTime);
        Page<ReturnOrder> p = page(new Page<>(page, pageSize), wrapper);

        List<ReturnOrderPageVO> list = new ArrayList<>();
        for (ReturnOrder order : p.getRecords()) {
            ReturnOrderPageVO vo = new ReturnOrderPageVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setStatus(order.getStatus());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setItemCount(order.getItemCount());
            vo.setTotalQty(order.getTotalQty());
            vo.setReason(order.getReason());
            vo.setCreateTime(order.getCreateTime());
            if (order.getSupplierId() != null) {
                Supplier s = supplierMapper.selectById(order.getSupplierId());
                vo.setSupplierName(s != null ? s.getName() : null);
            }
            list.add(vo);
        }

        Page<ReturnOrderPageVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(list);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE return_order SET status = 1 WHERE id = ? AND status = 0", id);
        if (rows == 0) throw new BusinessException(ErrorCode.RETURN_ORDER_STATUS_INVALID, "只有草稿状态可提交");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String approver) {
        int rows = jdbcTemplate.update(
            "UPDATE return_order SET status = 2, approver = ?, approved_time = ? WHERE id = ? AND status = 1",
            approver, LocalDateTime.now(), id);
        if (rows == 0) throw new BusinessException(ErrorCode.RETURN_ORDER_STATUS_INVALID, "只有已提交状态可审核");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String approver) {
        int rows = jdbcTemplate.update(
            "UPDATE return_order SET status = 4, approver = ? WHERE id = ? AND status = 1",
            approver, id);
        if (rows == 0) throw new BusinessException(ErrorCode.RETURN_ORDER_STATUS_INVALID, "只有已提交状态可驳回");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE return_order SET status = 3, completed_time = ? WHERE id = ? AND status = 2",
            LocalDateTime.now(), id);
        if (rows == 0) throw new BusinessException(ErrorCode.RETURN_ORDER_STATUS_INVALID, "只有已审批状态可完成退货");

        // 逐条出库
        List<ReturnOrderItem> items = itemMapper.selectList(
            new LambdaQueryWrapper<ReturnOrderItem>().eq(ReturnOrderItem::getOrderId, id));
        // A4: jdbcTemplate wrote to return_order above, read back via jdbcTemplate bypassing MyBatis cache
        Map<String, Object> orderRow = jdbcTemplate.queryForMap(
            "SELECT order_no, supplier_id FROM return_order WHERE id = ?", id);
        String orderNo = (String) orderRow.get("order_no");
        Long supplierId = orderRow.get("supplier_id") != null ? ((Number) orderRow.get("supplier_id")).longValue() : null;
        for (ReturnOrderItem item : items) {
            // 原子扣减库存（H1 下限守卫）
            int stockRows = jdbcTemplate.update(
                "UPDATE product SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?",
                item.getQty(), item.getProductId(), item.getQty());
            if (stockRows == 0) {
                throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT,
                    "商品 " + item.getProductName() + " 库存不足");
            }
            // 写库存流水
            int recordRows = jdbcTemplate.update(
                "INSERT INTO stock_record (product_id, type, qty, price, total_amount, supplier_id, operator, remark, create_time) VALUES (?, 3, ?, ?, ?, ?, ?, ?, ?)",
                item.getProductId(), item.getQty(), item.getPrice(),
                item.getTotalAmount(), supplierId, "system",
                "退货出库: " + orderNo, LocalDateTime.now());
            if (recordRows == 0)
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "退货出库失败：流水写入异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE return_order SET status = 4 WHERE id = ? AND status = 0", id);
        if (rows == 0) throw new BusinessException(ErrorCode.RETURN_ORDER_STATUS_INVALID, "只有草稿状态可取消");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ReturnOrder order = getById(id);
        if (order == null) throw new BusinessException(ErrorCode.RETURN_ORDER_NOT_FOUND);
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.RETURN_ORDER_STATUS_INVALID, "只有草稿状态可删除");
        }
        itemMapper.delete(new LambdaQueryWrapper<ReturnOrderItem>().eq(ReturnOrderItem::getOrderId, id));
        removeById(id);
    }

    private void batchInsertItems(List<ReturnOrderItem> items) {
        List<Object[]> batchArgs = new ArrayList<>();
        for (ReturnOrderItem item : items) {
            batchArgs.add(new Object[]{item.getOrderId(), item.getProductId(), item.getProductName(),
                item.getUnit(), item.getQty(), item.getPrice(), item.getTotalAmount(), LocalDateTime.now()});
        }
        int[] results = jdbcTemplate.batchUpdate(
            "INSERT INTO return_order_item (order_id, product_id, product_name, unit, qty, price, total_amount, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            batchArgs);
        int successCount = 0;
        for (int r : results) { if (r == 1 || r == java.sql.Statement.SUCCESS_NO_INFO) successCount++; }
        if (successCount != items.size())
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "退货明细批量写入失败");
    }

    private ReturnOrderVO toVO(ReturnOrder order, List<ReturnOrderItem> items) {
        ReturnOrderVO vo = new ReturnOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSupplierId(order.getSupplierId());
        vo.setOriginalOrderId(order.getOriginalOrderId());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setItemCount(order.getItemCount());
        vo.setTotalQty(order.getTotalQty());
        vo.setReason(order.getReason());
        vo.setApplicant(order.getApplicant());
        vo.setApprover(order.getApprover());
        vo.setApprovedTime(order.getApprovedTime());
        vo.setCompletedTime(order.getCompletedTime());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());

        if (order.getSupplierId() != null) {
            Supplier s = supplierMapper.selectById(order.getSupplierId());
            vo.setSupplierName(s != null ? s.getName() : null);
        }

        List<ReturnOrderItemVO> itemVOs = new ArrayList<>();
        for (ReturnOrderItem item : items) {
            ReturnOrderItemVO iv = new ReturnOrderItemVO();
            iv.setId(item.getId());
            iv.setProductId(item.getProductId());
            iv.setProductName(item.getProductName());
            iv.setUnit(item.getUnit());
            iv.setQty(item.getQty());
            iv.setPrice(item.getPrice());
            iv.setTotalAmount(item.getTotalAmount());
            itemVOs.add(iv);
        }
        vo.setItems(itemVOs);
        return vo;
    }
}
