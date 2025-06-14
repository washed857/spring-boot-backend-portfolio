package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.nms.newsroom.server.domain.Permission;
import rs.nms.newsroom.server.domain.Role;
import rs.nms.newsroom.server.domain.RoleLog.OperationType;
import rs.nms.newsroom.server.dto.RoleDTOs;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.repository.PermissionRepository;
import rs.nms.newsroom.server.repository.RoleRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RoleLogService roleLogService;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        testPermission = new Permission();
        testPermission.setId(101L);
        testPermission.setName("READ");

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("TestRole");
        testRole.setPermissions(Set.of(testPermission));
    }

    @Test
    void testCreateRole_success() {
        RoleDTOs.RoleCreateRequest req = new RoleDTOs.RoleCreateRequest();
        req.setName("TestRole");
        req.setPermissionIds(Set.of(101L));

        when(permissionRepository.findAllById(Set.of(101L))).thenReturn(List.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        RoleDTOs.RoleResponse resp = roleService.create(req);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getName()).isEqualTo("TestRole");
        assertThat(resp.getPermissions()).contains("READ");
        verify(roleLogService).logChange(any(Role.class), eq(OperationType.CREATE), isNull());
    }

    @Test
    void testGetAllRoles_success() {
        when(roleRepository.findAll()).thenReturn(List.of(testRole));
        List<RoleDTOs.RoleResponse> roles = roleService.getAll();
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getName()).isEqualTo("TestRole");
        assertThat(roles.get(0).getPermissions()).contains("READ");
    }

    @Test
    void testGetById_success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        RoleDTOs.RoleResponse resp = roleService.getById(1L);
        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getName()).isEqualTo("TestRole");
        assertThat(resp.getPermissions()).contains("READ");
    }

    @Test
    void testGetById_notFound() {
        when(roleRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.getById(2L));
    }

    @Test
    void testUpdateRole_success() {
        RoleDTOs.RoleUpdateRequest req = new RoleDTOs.RoleUpdateRequest();
        req.setName("UpdatedRole");
        req.setPermissionIds(Set.of(101L));

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findAllById(Set.of(101L))).thenReturn(List.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoleDTOs.RoleResponse resp = roleService.update(1L, req);

        assertThat(resp.getName()).isEqualTo("UpdatedRole");
        assertThat(resp.getPermissions()).contains("READ");
        verify(roleLogService).logChange(any(Role.class), eq(OperationType.UPDATE), any(Role.class));
    }

    @Test
    void testUpdateRole_notFound() {
        RoleDTOs.RoleUpdateRequest req = new RoleDTOs.RoleUpdateRequest();
        when(roleRepository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.update(123L, req));
    }

    @Test
    void testDeleteRole_success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        doNothing().when(roleRepository).delete(any(Role.class));
        doNothing().when(roleLogService).logChange(any(Role.class), eq(OperationType.DELETE), any(Role.class));

        roleService.delete(1L);

        verify(roleLogService).logChange(any(Role.class), eq(OperationType.DELETE), any(Role.class));
        verify(roleRepository).delete(testRole);
    }

    @Test
    void testDeleteRole_notFound() {
        when(roleRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.delete(5L));
    }
}
