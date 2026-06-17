package com.salon.customer.service;

import com.salon.customer.dto.CustomerLoginReqDTO;
import com.salon.customer.dto.CustomerLoginRespDTO;

public interface CustomerAuthService {
    void sendCode(String phone);
    CustomerLoginRespDTO login(CustomerLoginReqDTO req);
    void logout(String token);
    Long getMemberIdByToken(String token);
}
