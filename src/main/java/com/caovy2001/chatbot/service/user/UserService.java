package com.caovy2001.chatbot.service.user;

import com.caovy2001.chatbot.constant.ExceptionConstant;
import com.caovy2001.chatbot.entity.UserEntity;
import com.caovy2001.chatbot.repository.UserRepository;
import com.caovy2001.chatbot.service.BaseService;
import com.caovy2001.chatbot.service.user.command.CommandUserLogin;
import com.caovy2001.chatbot.service.user.command.CommandUserSignUp;
import com.caovy2001.chatbot.service.user.response.ResponseUserLogin;
import com.caovy2001.chatbot.service.user.response.ResponseUserSignUp;
import com.caovy2001.chatbot.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService  extends BaseService implements IUserService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    public ResponseUserLogin login(CommandUserLogin command) {
        UserEntity userEntity = userRepository.findByUsernameAndPassword(command.getUsername(), command.getPassword()).orElse(null);
        if (userEntity == null) {
            return returnException(ExceptionConstant.login_fail, ResponseUserLogin.class, null);
        }

        return ResponseUserLogin.builder()
                .username(userEntity.getUsername())
                .token(userEntity.getToken())
                .build();
    }

    @Override
    public UserEntity getById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public ResponseUserSignUp signUp(CommandUserSignUp commandUserSignUp) throws Exception {
        if (commandUserSignUp == null ||
        StringUtils.isAnyBlank(commandUserSignUp.getUsername(), commandUserSignUp.getPassword(), commandUserSignUp.getFullname())) {
            return this.returnException(ExceptionConstant.missing_param, ResponseUserSignUp.class, null);
        }

        // Check xem username này đã tồn tại hay chưa
        if (userRepository.countByUsername(commandUserSignUp.getUsername()) > 0) {
            return returnException(ExceptionConstant.username_exists, ResponseUserSignUp.class, null);
        }

        // Thực hiện đăng ký
        UserEntity userEntity = UserEntity.builder()
                .username(commandUserSignUp.getUsername())
                .password(commandUserSignUp.getPassword())
                .fullname(commandUserSignUp.getFullname())
                .token(String.valueOf(System.currentTimeMillis()))
                .secretKey(UUID.randomUUID().toString().toUpperCase())
                .build();

        // Khởi tạo token
        userEntity.setToken(JWTUtil.generateToken(userEntity));

        // Lưu user
        UserEntity savedUserEntity = userRepository.insert(userEntity);

        return ResponseUserSignUp.builder()
                .username(savedUserEntity.getUsername())
                .build();
    }
}
