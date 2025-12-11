package com.icio.sportakuz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegister {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}