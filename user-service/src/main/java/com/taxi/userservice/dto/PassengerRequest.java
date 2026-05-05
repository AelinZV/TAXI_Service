package com.taxi.userservice.dto;

import lombok.Data;

@Data
public class PassengerRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
}