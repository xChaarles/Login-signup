package com.login_registro.service;

import com.login_registro.dto.ReqRes;
import com.login_registro.entity.Users;
import com.login_registro.repositorio.UsersRepo;
import io.jsonwebtoken.Jwts;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;
import java.util.List;

@Service
public class UsersManagementService {

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Metodo para registrar un usuario
    public ReqRes register(ReqRes registrationRequest){
        ReqRes resp = new ReqRes();

        try {
            Users users = new Users();
            users.setEmail(registrationRequest.getEmail());
            users.setRole(registrationRequest.getRole());
            users.setNombre(registrationRequest.getNombre());
            users.setApellido(registrationRequest.getApellido());
            users.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            Users UsersResult = usersRepo.save(users);

            if (UsersResult.getId()>0){
                resp.setUsers((UsersResult));
                resp.setMessage("Nuevo usuario creado");
                resp.setStatusCode(200);
            }
        }catch (Exception e){
            resp.setStatusCode(500);
            resp.setError(e.getMessage());
        }
        return resp;
    }

    //Metodo para inicio de sesion
    public ReqRes login(ReqRes logonRequest){
        ReqRes response = new ReqRes();
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(logonRequest.getEmail(),
                            logonRequest.getPassword()));
            var user = usersRepo.findByEmail(logonRequest.getEmail()).orElseThrow();
            var jwt = jwtUtils.generateKeyToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Inicio de sesion existoso");

        } catch (RuntimeException e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public ReqRes refreshToken(ReqRes refreshTokenrequest){
        ReqRes response = new ReqRes();
        try{
            String Email = jwtUtils.extractUsername(refreshTokenrequest.getToken());
            Users users = usersRepo.findByEmail(Email).orElseThrow();
            if (jwtUtils.isTokenValid(refreshTokenrequest.getToken(), users)) {
                var jwt = jwtUtils.generateKeyToken(users);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenrequest.getToken());
                response.setExpirationTime("24Hr");
                response.setMessage("Token actualizado");
            }
            response.setStatusCode(200);
            return response;

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    public ReqRes getAllUsers() {
        ReqRes reqRes = new ReqRes();

        try {
            List<Users> result = usersRepo.findAll();
            if (!result.isEmpty()) {
                reqRes.setUsersList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Exitosa");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No se encontraron usuarios");
            }
            return reqRes;
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Se produjo un error: " + e.getMessage());
            return reqRes;
        }
    }


    public ReqRes getUsersById(Integer id) {
        ReqRes reqRes = new ReqRes();
        try {
            Users usersById = usersRepo.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            reqRes.setUsers(usersById);
            reqRes.setStatusCode(200);
            reqRes.setMessage("Usuarios con id '" + id + "' encontrado exitosamente");
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Se produjo un error: " + e.getMessage());
        }
        return reqRes;
    }


    public ReqRes deleteUser(Integer userId) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<Users> userOptional = usersRepo.findById(userId);
            if (userOptional.isPresent()) {
                usersRepo.deleteById(userId);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Usuario eliminado exitosamente");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("Usuario no encontrado para eliminación");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Se produjo un error al eliminar el usuario: " + e.getMessage());
        }
        return reqRes;
    }

    public ReqRes updateUser(Integer userId, Users updatedUser) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<Users> userOptional = usersRepo.findById(userId);
            if (userOptional.isPresent()) {
                Users existingUser = userOptional.get();
                existingUser.setEmail(updatedUser.getEmail());
                existingUser.setNombre(updatedUser.getNombre());
                existingUser.setApellido(updatedUser.getApellido());
                existingUser.setRole(updatedUser.getRole());

                // Check if password is present in the request
                if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                    // Encode the password and update it
                    existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                }

                Users savedUser = usersRepo.save(existingUser);
                reqRes.setUsers(savedUser);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Usuario actualizado con éxito");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("Usuario no encontrado para actualización");
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Ocurrió un error al actualizar el usuario: " + e.getMessage());
        }
        return reqRes;
    }


    public ReqRes getMyInfo(String email){
        ReqRes reqRes = new ReqRes();
        try {
            Optional<Users> userOptional = usersRepo.findByEmail(email);
            if (userOptional.isPresent()) {
                reqRes.setUsers(userOptional.get());
                reqRes.setStatusCode(200);
                reqRes.setMessage("exitoso");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("Usuario no encontrado para actualización");
            }

        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Se produjo un error al obtener la información del usuario: " + e.getMessage());
        }
        return reqRes;

    }
}