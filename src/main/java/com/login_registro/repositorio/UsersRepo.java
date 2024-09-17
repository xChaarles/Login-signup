package com.login_registro.repositorio;

import com.login_registro.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepo extends JpaRepository<Users, Integer> {

    Optional<Users> findByEmail(String email);
}
