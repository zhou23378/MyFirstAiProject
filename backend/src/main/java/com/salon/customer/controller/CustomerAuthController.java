package com.salon.customer.controller;

import com.salon.common.result.Result;
import com.salon.customer.dto.CustomerLoginReqDTO;
import com.salon.customer.dto.CustomerLoginRespDTO;
import com.salon.customer.service.CustomerAuthService;
import com.salon.member.entity.Member;
import com.salon.member.mapper.MemberMapper;
import com.salon.member.vo.MemberVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.salon.customer.dto.SendCodeReqDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "顾客认证", description = "手机号验证码登录/退出")
@RestController
@RequestMapping("/api/customer/auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthService authService;
    private final MemberMapper memberMapper;

    @Operation(summary = "发送验证码")
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeReqDTO body) {
        authService.sendCode(body.getPhone());
        return Result.success();
    }

    @Operation(summary = "顾客登录")
    @PostMapping("/login")
    public Result<CustomerLoginRespDTO> login(@Valid @RequestBody CustomerLoginReqDTO req) {
        return Result.success(authService.login(req));
    }

    @Operation(summary = "获取当前登录信息")
    @GetMapping("/me")
    public Result<MemberVO> me(@RequestHeader("X-Customer-Token") String token) {
        Long memberId = authService.getMemberIdByToken(token);
        if (memberId == null) {
            return Result.unauthorized();
        }
        Member member = memberMapper.selectById(memberId);
        return Result.success(MemberVO.from(member));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("X-Customer-Token") String token) {
        authService.logout(token);
        return Result.success();
    }
}
