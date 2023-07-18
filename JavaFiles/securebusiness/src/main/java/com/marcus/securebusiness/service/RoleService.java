package com.marcus.securebusiness.service;

import com.marcus.securebusiness.model.Role;

import java.util.Collection;

public interface RoleService {
    Role getRoleByUserId(Long id);
    Collection<Role> getRoles();
}
