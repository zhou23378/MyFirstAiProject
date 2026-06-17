package com.salon.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salon.common.exception.BusinessException;
import com.salon.common.exception.ErrorCode;
import com.salon.inventory.dto.PurchaseOrderItemDTO;
import com.salon.inventory.dto.PurchaseOrderReqDTO;
import com.salon.inventory.entity.PurchaseOrder;
import com.salon.inventory.entity.PurchaseOrderItem;
import com.salon.inventory.entity.Supplier;
import com.salon.inventory.mapper.PurchaseOrderItemMapper;
import com.salon.inventory.mapper.PurchaseOrderMapper;
import com.salon.inventory.mapper.SupplierMapper;
import com.salon.inventory.service.PurchaseOrderService;
import com.salon.inventory.vo.PurchaseOrderItemVO;
import com.salon.inventory.vo.PurchaseOrderPageVO;
import com.salon.inventory.vo.PurchaseOrderVO;
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
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    private final PurchaseOrderItemMapper itemMapper;
    private final SupplierMapper supplierMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrderVO create(PurchaseOrderReqDTO dto, String applicant) {
        // 生成订单编号
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = String.format("%05d", System.currentTimeMillis() % 100000);
        String orderNo = "PO-" + datePart + "-" + suffix;

        // 计算汇总
        int itemCount = dto.getItems().size();
        int totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderItemDTO item : dto.getItems()) {
            totalQty += item.getQty();
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(item.getQty())));
        }
        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);

        // 保存主表
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(orderNo);
        order.setSupplierId(dto.getSupplierId());
        order.setStatus(0);
        order.setTotalAmount(totalAmount);
        order.setItemCount(itemCount);
        order.setTotalQty(totalQty);
        order.setApplicant(applicant);
        order.setRemark(dto.getRemark());
        save(order);

        // 批量插入明细
        List<PurchaseOrderItem> entities = new ArrayList<>();
        for (PurchaseOrderItemDTO item : dto.getItems()) {
            PurchaseOrderItem ei = new PurchaseOrderItem();
            ei.setOrderId(order.getId());
            ei.setProductId(item.getProductId());
            ei.setProductName(item.getProductName());
            ei.setUnit(item.getUnit());
            ei.setQty(item.getQty());
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            ei.setPrice(price);
            ei.setTotalAmount(price.multiply(BigDecimal.valueOf(item.getQty())).setScale(2, RoundingMode.HALF_UP));
            ei.setReceivedQty(0);
            entities.add(ei);
        }
        batchInsertItems(entities);

        return toVO(order, entities);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrderVO update(Long id, PurchaseOrderReqDTO dto) {
        PurchaseOrder order = getById(id);
        if (order == null) throw new BusinessException(ErrorCode.PURCHASE_ORDER_NOT_FOUND);
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PURCHASE_ORDER_STATUS_INVALID, "只有草稿状态可编辑");
        }

        int itemCount = dto.getItems().size();
        int totalQty = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderItemDTO item : dto.getItems()) {
            totalQty += item.getQty();
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(item.getQty())));
        }
        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);

        order.setSupplierId(dto.getSupplierId());
        order.setTotalAmount(totalAmount);
        order.setItemCount(itemCount);
        order.setTotalQty(totalQty);
        order.setRemark(dto.getRemark());
        updateById(order);

        // 删除旧明细 + 插入新明细
        itemMapper.delete(new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id));
        List<PurchaseOrderItem> entities = new ArrayList<>();
        for (PurchaseOrderItemDTO item : dto.getItems()) {
            PurchaseOrderItem ei = new PurchaseOrderItem();
            ei.setOrderId(id);
            ei.setProductId(item.getProductId());
            ei.setProductName(item.getProductName());
            ei.setUnit(item.getUnit());
            ei.setQty(item.getQty());
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            ei.setPrice(price);
            ei.setTotalAmount(price.multiply(BigDecimal.valueOf(item.getQty())).setScale(2, RoundingMode.HALF_UP));
            ei.setReceivedQty(0);
            entities.add(ei);
        }
        batchInsertItems(entities);

        return toVO(order, entities);
    }

    @Override
    public PurchaseOrderVO getDetail(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) throw new BusinessException(ErrorCode.PURCHASE_ORDER_NOT_FOUND);
        List<PurchaseOrderItem> items = itemMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id));
        return toVO(order, items);
    }

    @Override
    public Page<PurchaseOrderPageVO> page(int page, int pageSize, Integer status, Long supplierId) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<PurchaseOrder>()
            .eq(status != null, PurchaseOrder::getStatus, status)
            .eq(supplierId != null, PurchaseOrder::getSupplierId, supplierId)
            .orderByDesc(PurchaseOrder::getCreateTime);
        Page<PurchaseOrder> p = page(new Page<>(page, pageSize), wrapper);

        List<PurchaseOrderPageVO> list = new ArrayList<>();
        for (PurchaseOrder order : p.getRecords()) {
            PurchaseOrderPageVO vo = new PurchaseOrderPageVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setStatus(order.getStatus());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setItemCount(order.getItemCount());
            vo.setTotalQty(order.getTotalQty());
            vo.setCreateTime(order.getCreateTime());
            if (order.getSupplierId() != null) {
                Supplier s = supplierMapper.selectById(order.getSupplierId());
                vo.setSupplierName(s != null ? s.getName() : null);
            }
            list.add(vo);
        }

        Page<PurchaseOrderPageVO> result = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        result.setRecords(list);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE purchase_order SET status = 1 WHERE id = ? AND status = 0", id);
        if (rows == 0) throw new BusinessException(ErrorCode.PURCHASE_ORDER_STATUS_INVALID, "只有草稿状态可提交");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String approver) {
        int rows = jdbcTemplate.update(
            "UPDATE purchase_order SET status = 2, approver = ?, approved_time = ? WHERE id = ? AND status = 1",
            approver, LocalDateTime.now(), id);
        if (rows == 0) throw new BusinessException(ErrorCode.PURCHASE_ORDER_STATUS_INVALID, "只有已提交状态可审核");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE purchase_order SET status = 3, received_time = ? WHERE id = ? AND status = 2",
            LocalDateTime.now(), id);
        if (rows == 0) throw new BusinessException(ErrorCode.PURCHASE_ORDER_STATUS_INVALID, "只有已审批状态可收货");

        // 逐条入库
        List<PurchaseOrderItem> items = itemMapper.selectList(
            new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id));
        // A4: jdbcTemplate wrote to purchase_order above, read back via jdbcTemplate bypassing MyBatis cache
        Map<String, Object> orderRow = jdbcTemplate.queryForMap(
            "SELECT order_no, supplier_id FROM purchase_order WHERE id = ?", id);
        String orderNo = (String) orderRow.get("order_no");
        Long supplierId = orderRow.get("supplier_id") != null ? ((Number) orderRow.get("supplier_id")).longValue() : null;
        for (PurchaseOrderItem item : items) {
            // 原子增加库存
            int stockRows = jdbcTemplate.update(
                "UPDATE product SET stock_qty = stock_qty + ? WHERE id = ?",
                item.getQty(), item.getProductId());
            if (stockRows == 0)
                throw new BusinessException(ErrorCode.INTERNAL_ERROR,
                    "采购入库失败：商品 " + item.getProductName() + " 不存在");
            // 写库存流水
            int recordRows = jdbcTemplate.update(
                "INSERT INTO stock_record (product_id, type, qty, price, total_amount, supplier_id, operator, remark, create_time) VALUES (?, 1, ?, ?, ?, ?, ?, ?, ?)",
                item.getProductId(), item.getQty(), item.getPrice(),
                item.getTotalAmount(), supplierId, "system",
                "采购入库: PO-" + orderNo, LocalDateTime.now());
            if (recordRows == 0)
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "采购入库失败：流水写入异常");
            // 更新已收数量
            int itemRows = jdbcTemplate.update(
                "UPDATE purchase_order_item SET received_qty = ? WHERE id = ?",
                item.getQty(), item.getId());
            if (itemRows == 0)
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "采购入库失败：明细更新异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        int rows = jdbcTemplate.update(
            "UPDATE purchase_order SET status = 4 WHERE id = ? AND status IN (0, 1)", id);
        if (rows == 0) throw new BusinessException(ErrorCode.PURCHASE_ORDER_STATUS_INVALID, "只有草稿或已提交状态可取消");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PurchaseOrder order = getById(id);
        if (order == null) throw new BusinessException(ErrorCode.PURCHASE_ORDER_NOT_FOUND);
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.PURCHASE_ORDER_STATUS_INVALID, "只有草稿状态可删除");
        }
        itemMapper.delete(new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id));
        removeById(id);
    }

    private void batchInsertItems(List<PurchaseOrderItem> items) {
        List<Object[]> batchArgs = new ArrayList<>();
        for (PurchaseOrderItem item : items) {
            batchArgs.add(new Object[]{item.getOrderId(), item.getProductId(), item.getProductName(),
                item.getUnit(), item.getQty(), item.getPrice(), item.getTotalAmount(),
                item.getReceivedQty() != null ? item.getReceivedQty() : 0, LocalDateTime.now()});
        }
        int[] results = jdbcTemplate.batchUpdate(
            "INSERT INTO purchase_order_item (order_id, product_id, product_name, unit, qty, price, total_amount, received_qty, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            batchArgs);
        int successCount = 0;
        for (int r : results) { if (r == 1 || r == java.sql.Statement.SUCCESS_NO_INFO) successCount++; }
        if (successCount != items.size())
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "采购明细批量写入失败");
    }

    private PurchaseOrderVO toVO(PurchaseOrder order, List<PurchaseOrderItem> items) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSupplierId(order.getSupplierId());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setItemCount(order.getItemCount());
        vo.setTotalQty(order.getTotalQty());
        vo.setApplicant(order.getApplicant());
        vo.setApprover(order.getApprover());
        vo.setApprovedTime(order.getApprovedTime());
        vo.setReceivedTime(order.getReceivedTime());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());

        if (order.getSupplierId() != null) {
            Supplier s = supplierMapper.selectById(order.getSupplierId());
            vo.setSupplierName(s != null ? s.getName() : null);
        }

        List<PurchaseOrderItemVO> itemVOs = new ArrayList<>();
        for (PurchaseOrderItem item : items) {
            PurchaseOrderItemVO iv = new PurchaseOrderItemVO();
            iv.setId(item.getId());
            iv.setProductId(item.getProductId());
            iv.setProductName(item.getProductName());
            iv.setUnit(item.getUnit());
            iv.setQty(item.getQty());
            iv.setPrice(item.getPrice());
            iv.setTotalAmount(item.getTotalAmount());
            iv.setReceivedQty(item.getReceivedQty());
            itemVOs.add(iv);
        }
        vo.setItems(itemVOs);
        return vo;
    }
}
