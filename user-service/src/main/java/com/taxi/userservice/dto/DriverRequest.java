package com.taxi.userservice.dto;

import lombok.Data;

@Data
public class DriverRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String licenseNumber;
}