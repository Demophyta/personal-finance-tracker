package com.finance.tracker.service;

import com.finance.tracker.dto.UpdateUserRequestDTO;
import com.finance.tracker.dto.UserResponseDTO;
import com.finance.tracker.model.User;

public interface UserService {
    UserResponseDTO getUserProfile(User user);
    UserResponseDTO updateUserProfile(User user, UpdateUserRequestDTO dto);
    void deleteUser(User user);
}
