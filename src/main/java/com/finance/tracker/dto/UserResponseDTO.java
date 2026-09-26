package com.finance.tracker.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
