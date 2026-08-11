package com.aditi.attendance.user.service;

import com.aditi.attendance.entity.Employee;
import com.aditi.attendance.entity.Role;
import com.aditi.attendance.entity.User;
import com.aditi.attendance.employee.exception.EmployeeNotFoundException;
import com.aditi.attendance.employee.repository.EmployeeRepository;
import com.aditi.attendance.role.exception.RoleNotFoundException;
import com.aditi.attendance.role.repository.RoleRepository;
import com.aditi.attendance.common.util.EmployeeCodeGenerator;
import com.aditi.attendance.user.dto.AccountLookupResponse;
import com.aditi.attendance.user.dto.AuthStatusResponse;
import com.aditi.attendance.user.dto.LoginRequest;
import java.time.LocalDate;
import com.aditi.attendance.user.dto.SetupPasswordRequest;
import com.aditi.attendance.user.dto.SignupRequest;
import com.aditi.attendance.user.dto.UserRequest;
import com.aditi.attendance.user.dto.UserResponse;
import com.aditi.attendance.user.exception.InvalidCredentialsException;
import com.aditi.attendance.user.exception.UnauthorizedEmailException;
import com.aditi.attendance.user.exception.UserAlreadyRegisteredException;
import com.aditi.attendance.user.exception.UserNotFoundException;
import com.aditi.attendance.user.exception.UserNotYetSetupException;
import com.aditi.attendance.user.exception.UsernameAlreadyExistsException;
import com.aditi.attendance.user.mapper.UserMapper;
import com.aditi.attendance.user.repository.UserCriteriaRepository;
import com.aditi.attendance.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserCriteriaRepository userCriteriaRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserRequest request) {

        validateDuplicateUsername(request.getUsername());

        Employee employee = findEmployeeById(request.getEmployeeId());

        Role role = findRoleById(request.getRoleId());

        User user = UserMapper.toEntity(request, employee, role);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {

        return userRepository.findByActiveTrue()
                .stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {

        User user = findUserById(id);

        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {

        User user = findUserById(id);

        if (!user.getUsername().equalsIgnoreCase(request.getUsername())) {
            validateDuplicateUsername(request.getUsername());
        }

        Employee employee = findEmployeeById(request.getEmployeeId());

        Role role = findRoleById(request.getRoleId());

        user.setUsername(request.getUsername());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setActive(request.getActive());
        user.setEmployee(employee);
        user.setRole(role);

        User updatedUser = userRepository.save(user);

        return UserMapper.toResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {

        User user = findUserById(id);

        user.setActive(false);

        userRepository.save(user);
    }

    public List<UserResponse> searchUser(String keyword) {

        return userCriteriaRepository.searchUser(keyword)
                .stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmployeeEmail(request.getUsername()))
                .orElseThrow(() ->
                        new UnauthorizedEmailException(
                                "Unauthorized email."
                        ));

        if (!user.getActive()) {
            throw new InvalidCredentialsException(
                    "User account is inactive."
            );
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new UserNotYetSetupException(
                    "Password has not been set. Please complete employee setup."
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new InvalidCredentialsException(
                    "Invalid username or password."
            );
        }

        return UserMapper.toResponse(user);
    }

    public AuthStatusResponse getAuthStatus() {
        return AuthStatusResponse.builder()
                .signupAllowed(userRepository.count() == 0)
                .build();
    }

    public AccountLookupResponse lookupAccount(String email) {
        return userRepository.findByUsername(email)
                .or(() -> userRepository.findByEmployeeEmail(email))
                .map(user -> AccountLookupResponse.builder()
                        .found(true)
                        .needsPasswordSetup(user.getPassword() == null || user.getPassword().isBlank())
                        .email(email)
                        .build())
                .orElseGet(() -> AccountLookupResponse.builder()
                        .found(false)
                        .needsPasswordSetup(false)
                        .email(email)
                        .build());
    }

    @Transactional
    public UserResponse signupAdmin(SignupRequest request) {

        if (userRepository.count() > 0) {
            throw new UserAlreadyRegisteredException(
                    "Admin already exists. Please login with your email and password."
            );
        }

        if (userRepository.findByUsername(request.getEmail()).isPresent()) {
            throw new UserAlreadyRegisteredException(
                    "An account with this email already exists."
            );
        }

        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .roleName("ADMIN")
                                .description("Administrator role")
                                .build()
                ));

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    String[] parts = request.getName().trim().split(" ", 2);
                    String firstName = parts.length > 0 ? parts[0] : request.getName();
                    String lastName = parts.length > 1 ? parts[1] : "";

                    return employeeRepository.save(Employee.builder()
                            .employeeCode(EmployeeCodeGenerator.generate())
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(request.getEmail())
                            .phoneNumber(request.getPhoneNumber())
                            .department("Administration")
                            .designation("ADMIN")
                            .dateOfJoining(LocalDate.now())
                            .active(true)
                            .deleted(false)
                            .build());
                });

        if (userRepository.findByEmployeeId(employee.getId()).isPresent()) {
            throw new UserAlreadyRegisteredException(
                    "A user account already exists for this employee."
            );
        }

        UserRequest userRequest = UserRequest.builder()
                .username(request.getEmail())
                .password(request.getPassword())
                .roleId(adminRole.getId())
                .employeeId(employee.getId())
                .active(true)
                .build();

        return createUser(userRequest);
    }

    @Transactional
    public UserResponse setupEmployeePassword(SetupPasswordRequest request) {

        User user = userRepository.findByEmployeeEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        "User account not found for this email."
                ));

        if (!user.getActive()) {
            throw new InvalidCredentialsException("User account is inactive.");
        }

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            throw new InvalidCredentialsException("Password has already been set for this user.");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User updatedUser = userRepository.save(user);
        return UserMapper.toResponse(updatedUser);
    }

    private User findUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with ID " + id + " not found."
                        ));
    }

    private Employee findEmployeeById(Long employeeId) {

        return employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee with ID " + employeeId + " not found."
                        ));
    }

    private Role findRoleById(Long roleId) {

        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "Role with ID " + roleId + " not found."
                        ));
    }

    private void validateDuplicateUsername(String username) {

        if (userRepository.existsByUsername(username)) {

            throw new UsernameAlreadyExistsException(
                    "Username '" + username + "' already exists."
            );
        }
    }
}