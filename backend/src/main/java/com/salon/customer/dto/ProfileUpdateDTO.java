package com.salon.customer.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProfileUpdateDTO {

    private String name;

    private LocalDate birthday;
}
