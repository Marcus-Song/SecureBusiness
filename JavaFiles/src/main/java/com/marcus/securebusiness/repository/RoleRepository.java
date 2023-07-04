package com.marcus.securebusiness.repository;


import com.marcus.securebusiness.model.Role;

import java.util.Collection;

public interface RoleRepository<T extends Role>{
    // Basic BRUD Operations
    T create(T data);
    Collection<T> list();
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    // More Complex Operations
    void addRoleToUser(Long userId, String roleName);
    Role getRoleByUserId(Long id);
    Role getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);

    Collection<T> getRoles();
}
