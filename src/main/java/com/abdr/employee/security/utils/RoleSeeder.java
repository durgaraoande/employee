package com.abdr.employee.security.utils;

import com.abdr.employee.security.dao.RoleRepo;
import com.abdr.employee.security.entities.ERole;
import com.abdr.employee.security.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RoleSeeder {

    @Autowired
    private RoleRepo roleRepository;

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Seed the roles after the application is ready
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(ERole.ROLE_USER);
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName(ERole.ROLE_MANAGER).isEmpty()) {
            Role managerRole = new Role();
            managerRole.setName(ERole.ROLE_MANAGER);
            roleRepository.save(managerRole);
        }
    }
}
