package com.finance.tracker.service;


import com.finance.tracker.dto.UpdateUserRequestDTO;
import com.finance.tracker.dto.UserResponseDTO;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDTO getUserProfile(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    @Override
    public UserResponseDTO updateUserProfile(User user, UpdateUserRequestDTO dto) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        userRepository.save(user);

        return getUserProfile(user);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }
}
