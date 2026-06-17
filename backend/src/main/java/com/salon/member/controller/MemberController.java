package com.salon.member.controller;

import com.salon.common.annotation.AuditLog;
import com.salon.common.exception.ErrorCode;
import com.salon.common.result.PageResult;
import com.salon.common.result.Result;
import com.salon.member.dto.MemberQueryDTO;
import com.salon.member.dto.MemberReqDTO;
import com.salon.member.dto.RechargeDTO;
import com.salon.member.entity.Member;
import com.salon.member.mapper.RechargeRecordMapper;
import com.salon.member.service.MemberService;
import com.salon.member.vo.MemberVO;
import com.salon.member.vo.RechargeRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 会员管理 Controller
 * <p>
 * 提供会员的增删改查 REST API
 * </p>
 */
@Tag(name = "会员管理", description = "会员的增删改查接口")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final RechargeRecordMapper rechargeRecordMapper;

    @Value("${salon.churn-risk-days:60}")
    private int churnRiskDays;

    /**
     * 分页查询会员列表
     *
     * @param query 查询条件（姓名、手机号、分页参数）
     * @return 分页结果
     */
    @Operation(summary = "分页查询会员列表", description = "支持按姓名和手机号模糊查询")
    @GetMapping("/page")
    public Result<PageResult<MemberVO>> page(@Valid MemberQueryDTO query) {
        return Result.success(PageResult.of(memberService.pageQuery(query), MemberVO::from));
    }

    @GetMapping("/template")
    @Operation(summary = "下载会员导入模板")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("会员导入模板");
        Row header = sheet.createRow(0);
        String[] cols = {"姓名", "手机号", "性别", "等级", "积分", "余额", "生日", "标签", "最后消费", "备注", "注册时间"};
        for (int i = 0; i < cols.length; i++) {
            header.createCell(i).setCellValue(cols[i]);
        }
        // 示例行：演示正确格式
        Row example = sheet.createRow(1);
        example.createCell(0).setCellValue("张三");
        example.createCell(1).setCellValue("13800138000");
        example.createCell(2).setCellValue("男");
        example.createCell(3).setCellValue("1");
        example.createCell(4).setCellValue(0);
        example.createCell(5).setCellValue(0.00);
        example.createCell(6).setCellValue("1990-01-01");
        example.createCell(7).setCellValue("新客");
        example.createCell(8).setCellValue("");
        example.createCell(9).setCellValue("备注示例");
        example.createCell(10).setCellValue("");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("会员导入模板.xlsx", StandardCharsets.UTF_8));
        OutputStream os = response.getOutputStream();
        wb.write(os);
        wb.close();
        os.flush();
    }

    /**
     * 根据ID查询会员详情
     *
     * @param id 会员ID
     * @return 会员信息
     */
    @Operation(summary = "查询会员详情", description = "根据ID查询单个会员信息")
    @GetMapping("/{id}")
    public Result<MemberVO> getById(@Parameter(description = "会员ID") @PathVariable Long id) {
        Member member = memberService.getById(id);
        if (member == null) {
            return Result.notFound("会员不存在");
        }
        return Result.success(MemberVO.from(member));
    }

    /**
     * 新增会员
     *
     * @param req 会员信息
     * @return 新增后的会员
     */
    @Operation(summary = "新增会员")
    @PostMapping
    public Result<MemberVO> create(@Valid @RequestBody MemberReqDTO req) {
        return Result.success(MemberVO.from(memberService.createMember(req)));
    }

    /**
     * 修改会员
     *
     * @param id  会员ID
     * @param req 会员信息
     * @return 修改后的会员
     */
    @Operation(summary = "修改会员")
    @PutMapping("/{id}")
    public Result<MemberVO> update(
            @Parameter(description = "会员ID") @PathVariable Long id,
            @Valid @RequestBody MemberReqDTO req) {
        return Result.success(MemberVO.from(memberService.updateMember(id, req)));
    }

    /**
     * 会员充值
     *
     * @param id  会员ID
     * @param dto 充值信息
     * @return 充值后的会员
     */
    @PostMapping("/{id}/recharge")
    @Operation(summary = "会员充值")
    public Result<MemberVO> recharge(@PathVariable Long id, @Valid @RequestBody RechargeDTO dto) {
        return Result.success(MemberVO.from(memberService.recharge(id, dto)));
    }

    @Operation(summary = "删除会员")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "会员ID") @PathVariable Long id) {
        memberService.deleteMember(id);
        return Result.success();
    }

    @GetMapping("/{id}/recharge-records")
    @Operation(summary = "查询会员充值记录")
    public Result<List<RechargeRecordVO>> rechargeRecords(@PathVariable Long id) {
        return Result.success(
                rechargeRecordMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.salon.member.entity.RechargeRecord>()
                                .eq(com.salon.member.entity.RechargeRecord::getMemberId, id)
                                .orderByDesc(com.salon.member.entity.RechargeRecord::getCreateTime)
                ).stream().map(RechargeRecordVO::from).toList());
    }

    @GetMapping("/churn-risk")
    @Operation(summary = "流失预警", description = "查询超过N天未消费的会员")
    public Result<List<MemberVO>> churnRisk(@RequestParam(defaultValue = "0") int days) {
        int threshold = days > 0 ? days : churnRiskDays;
        return Result.success(
                memberService.getChurnRiskMembers(threshold).stream()
                        .map(MemberVO::from)
                        .toList()
        );
    }

    @AuditLog("导出会员Excel")
    @GetMapping("/export")
    @Operation(summary = "导出会员Excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        List<Member> members = memberService.list();
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("会员列表");
        Row header = sheet.createRow(0);
        String[] cols = {"姓名", "手机号", "性别", "等级", "积分", "余额", "生日", "标签", "最后消费", "备注", "注册时间"};
        for (int i = 0; i < cols.length; i++) {
            header.createCell(i).setCellValue(cols[i]);
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(m.getName() != null ? m.getName() : "");
            row.createCell(1).setCellValue(m.getPhone() != null ? m.getPhone() : "");
            row.createCell(2).setCellValue(m.getGender() == null ? "未知" : m.getGender() == 1 ? "男" : m.getGender() == 2 ? "女" : "未知");
            row.createCell(3).setCellValue(m.getLevel() != null ? m.getLevel().toString() : "");
            row.createCell(4).setCellValue(m.getPoints() != null ? m.getPoints() : 0);
            row.createCell(5).setCellValue(m.getBalance() != null ? m.getBalance().doubleValue() : 0);
            row.createCell(6).setCellValue(m.getBirthday() != null ? m.getBirthday().format(df) : "");
            row.createCell(7).setCellValue(m.getTags() != null ? m.getTags() : "");
            row.createCell(8).setCellValue(m.getLastConsumeDate() != null ? m.getLastConsumeDate().format(df) : "");
            row.createCell(9).setCellValue(m.getRemark() != null ? m.getRemark() : "");
            row.createCell(10).setCellValue(m.getCreateTime() != null ? m.getCreateTime().toString() : "");
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("会员列表.xlsx", StandardCharsets.UTF_8));
        OutputStream os = response.getOutputStream();
        wb.write(os);
        wb.close();
        os.flush();
    }

    @AuditLog("导入会员Excel")
    @PostMapping("/import")
    @Operation(summary = "导入会员Excel")
    @org.springframework.transaction.annotation.Transactional
    public Result<String> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        // 文件大小限制 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            return Result.error(ErrorCode.FILE_FORMAT_UNSUPPORTED);
        }

        int count = 0;
        List<String> rowErrors = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                try {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String name = getCellString(row.getCell(0));
                    String phone = getCellString(row.getCell(1));
                    if (name.isEmpty() && phone.isEmpty()) continue;

                    Member m = new Member();
                    m.setName(name);
                    m.setPhone(phone);
                    String gender = getCellString(row.getCell(2));
                    m.setGender("男".equals(gender) ? 1 : "女".equals(gender) ? 2 : 0);

                    // 积分与余额：兼容字符串和数字两种格式
                    m.setPoints((int) getCellNumeric(row.getCell(4)));
                    m.setBalance(BigDecimal.valueOf(getCellNumeric(row.getCell(5))));

                    String birthday = getCellString(row.getCell(6));
                    if (!birthday.isEmpty()) m.setBirthday(LocalDate.parse(birthday, df));
                    m.setTags(getCellString(row.getCell(7)));
                    m.setRemark(getCellString(row.getCell(9)));

                    memberService.save(m);
                    count++;
                } catch (Exception e) {
                    rowErrors.add("第" + (i + 1) + "行: " + e.getMessage());
                }
            }
        }

        StringBuilder msg = new StringBuilder("成功导入 " + count + " 条会员");
        if (!rowErrors.isEmpty()) {
            msg.append("，").append(rowErrors.size()).append(" 行失败: ").append(String.join("; ", rowErrors));
        }
        return Result.success(msg.toString());
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                // 整数不带小数点，小数保留原样
                double v = cell.getNumericCellValue();
                yield v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private double getCellNumeric(Cell cell) {
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                String s = cell.getStringCellValue().trim();
                yield s.isEmpty() ? 0 : Double.parseDouble(s);
            }
            default -> 0;
        };
    }
}
