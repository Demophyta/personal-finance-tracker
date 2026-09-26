package com.finance.tracker.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequestDTO {
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
