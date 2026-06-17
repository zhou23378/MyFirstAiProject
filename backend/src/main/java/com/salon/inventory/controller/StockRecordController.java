package com.salon.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.common.exception.ErrorCode;
import com.salon.common.util.JwtUtil;
import com.salon.inventory.dto.StockRecordReqDTO;
import com.salon.inventory.service.StockRecordService;
import com.salon.inventory.vo.StockRecordPageVO;
import com.salon.inventory.vo.StockRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "库存流水", description = "入库/出库操作接口")
@RestController
@RequestMapping("/api/stock-record")
@RequiredArgsConstructor
public class StockRecordController {

    private final StockRecordService stockRecordService;
    private final JwtUtil jwtUtil;

    @GetMapping("/page")
    @Operation(summary = "分页查询库存流水（含商品名/供应商名）")
    public Result<PageResult<StockRecordPageVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer type) {
        Page<StockRecordPageVO> p = stockRecordService.pageWithNames(page, pageSize, type);
        return Result.success(PageResult.of(p));
    }

    @PostMapping
    @Operation(summary = "入库/出库")
    public Result<StockRecordVO> create(@Valid @RequestBody StockRecordReqDTO dto, HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return Result.error(ErrorCode.UNAUTHORIZED);
        }
        String token = auth.substring(7);
        String operator = jwtUtil.getUsername(token);
        return Result.success(StockRecordVO.from(stockRecordService.createRecord(dto, operator)));
    }
}
