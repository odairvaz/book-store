package com.example.bookstore.spring;

import com.example.bookstore.persistense.model.Privilege;
import com.example.bookstore.persistense.model.Role;
import com.example.bookstore.persistense.model.User;
import com.example.bookstore.service.IPrivilegeService;
import com.example.bookstore.service.IRoleService;
import com.example.bookstore.service.IUserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.bookstore.spring.UserRolesAndPrivileges.*;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private final IUserService userService;
    private final IPrivilegeService privilegeService;
    private final IRoleService roleService;
    private final ApplicationContext context;

    public SetupDataLoader(IUserService userService, IRoleService roleService, IPrivilegeService privilegeService, ApplicationContext context) {
        this.userService = userService;
        this.roleService = roleService;
        this.privilegeService = privilegeService;
        this.context = context;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (isAlreadySetup()) {
            return;
        }
        context.getBean(SetupDataLoader.class).setupInitialData();
    }

    private boolean isAlreadySetup() {
        User testUser = userService.findUserByEmail("test@test.com");
        Optional<Role> adminRole = roleService.findRoleByName(ROLE_ADMIN);
        return testUser != null && adminRole.isPresent();
    }

    @Transactional
    public void setupInitialData() {
        Privilege readPrivilege = createPrivilege(READ_PRIVILEGE);
        Privilege writePrivilege = createPrivilege(WRITE_PRIVILEGE);

        Role adminRole = createRole(ROLE_ADMIN, Arrays.asList(readPrivilege, writePrivilege));
        createRole(ROLE_USER, Collections.singletonList(readPrivilege));

        userService.createUser("Test", "Test", "test@test.com", "test", Collections.singletonList(adminRole));
    }

    private Privilege createPrivilege(String name) {
        return privilegeService.createPrivilegeIfNotFound(name);
    }

    private Role createRole(String name, List<Privilege> privileges) {
        return roleService.createRoleIfNotFound(name, privileges);
    }

}
