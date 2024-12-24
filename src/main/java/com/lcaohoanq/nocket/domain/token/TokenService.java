package com.lcaohoanq.nocket.domain.token;

import com.lcaohoanq.nocket.component.JwtTokenUtils;
import com.lcaohoanq.nocket.domain.jwt.JWTConfig;
import com.lcaohoanq.nocket.domain.user.UserPort;
import com.lcaohoanq.nocket.exception.ExpiredTokenException;
import com.lcaohoanq.nocket.exception.TokenNotFoundException;
import com.lcaohoanq.nocket.base.exception.DataNotFoundException;
import com.lcaohoanq.nocket.domain.user.User;
import com.lcaohoanq.nocket.domain.user.UserService;
import com.lcaohoanq.nocket.mapper.UserMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService implements ITokenService {

    private static final int MAX_TOKENS = 3;
    private final UserService userService;
    private final JWTConfig jwtConfig;
    private final TokenRepository tokenRepository;
    private final JwtTokenUtils jwtTokenUtil;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public Token refreshToken(String refreshToken, User user) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        if (existingToken == null) {
            throw new DataNotFoundException("Refresh token does not exist");
        }
        if (existingToken.getRefreshExpirationDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(existingToken);
            throw new ExpiredTokenException("Refresh token is expired");
        }
        String token = jwtTokenUtil.generateToken(user);
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(jwtConfig.getExpiration());
        existingToken.setExpirationDate(expirationDateTime);
        existingToken.setToken(token);
        existingToken.setRefreshToken(UUID.randomUUID().toString());
        existingToken.setRefreshExpirationDate(
            LocalDateTime.now().plusSeconds(jwtConfig.getExpirationRefreshToken()));
        return existingToken;
    }

    //do revoke token
    @Override
    public void deleteToken(String token, User user) throws DataNotFoundException {
        Token existingToken = tokenRepository.findByToken(token);
        if (Boolean.TRUE.equals(existingToken.getRevoked())) {
            throw new TokenNotFoundException("Token has been revoked");
        }
        //check if token is attaching with user
        if (!Objects.equals(existingToken.getUser().getId(), user.getId())) {
            throw new TokenNotFoundException("Token does not exist");
        }
        existingToken.setRevoked(true);
        tokenRepository.save(existingToken);
    }

    @Override
    public Token findUserByToken(String token) throws DataNotFoundException {
        return tokenRepository.findByToken(token);
    }

    @Transactional
    @Override
    public Token addToken(UUID userId, String token, boolean isMobileDevice) {
        UserPort.UserResponse existingUser = userService.findUserById(userId);
        List<Token> userTokens = tokenRepository.findByUserId(existingUser.getId());
        int tokenCount = userTokens.size();
        // Số lượng token vượt quá giới hạn, xóa một token cũ
        if (tokenCount >= MAX_TOKENS) {
            //kiểm tra xem trong danh sách userTokens có tồn tại ít nhất
            //một token không phải là thiết bị di động (non-mobile)
            boolean hasNonMobileToken = !userTokens.stream().allMatch(Token::isMobile);
            Token tokenToDelete;
            if (hasNonMobileToken) {
                tokenToDelete = userTokens.stream()
                    .filter(userToken -> !userToken.isMobile())
                    .findFirst()
                    .orElse(userTokens.get(0));
            } else {
                //tất cả các token đều là thiết bị di động,
                //chúng ta sẽ xóa token đầu tiên trong danh sách
                tokenToDelete = userTokens.get(0);
            }
            tokenRepository.delete(tokenToDelete);
        }
        long expirationInSeconds = jwtConfig.getExpiration();
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expirationInSeconds);
        // Tạo mới một token cho người dùng

        Token newToken = new Token();
        newToken.setUser(userMapper.toUser(existingUser));
        newToken.setToken(token);
        newToken.setRevoked(false);
        newToken.setExpired(false);
        newToken.setTokenType("Bearer");
        newToken.setExpirationDate(expirationDateTime);
        newToken.setMobile(isMobileDevice);

        newToken.setRefreshToken(UUID.randomUUID().toString());
        newToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(
            jwtConfig.getExpirationRefreshToken()));
        tokenRepository.save(newToken);
        return newToken;
    }

}
