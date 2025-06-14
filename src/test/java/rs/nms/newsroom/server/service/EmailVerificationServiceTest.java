package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rs.nms.newsroom.server.domain.EmailVerificationToken;
import rs.nms.newsroom.server.domain.User;
import rs.nms.newsroom.server.dto.EmailVerificationDTOs;
import rs.nms.newsroom.server.exception.ResourceNotFoundException;
import rs.nms.newsroom.server.exception.UnauthorizedException;
import rs.nms.newsroom.server.repository.EmailVerificationTokenRepository;
import rs.nms.newsroom.server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private MailService mailService;

    @InjectMocks
    private EmailVerificationService service;

    private User user;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "sslEnabled", false);
        ReflectionTestUtils.setField(service, "serverPort", 8080);
        ReflectionTestUtils.setField(service, "contextPath", "/");
        user = new User();
        user.setId(1L);
        user.setFullName("Test Testic");
        user.setEmail("test@mail.com");
        user.setEmailVerified(false);
    }

    @Test
    void testResendVerificationEmail_success() {
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        // No previous token
        doNothing().when(tokenRepository).deleteByUserId(1L);
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(inv -> {
            EmailVerificationToken t = inv.getArgument(0);
            t.setId(5L);
            return t;
        });

        doNothing().when(mailService).sendPlainText(anyString(), anyString(), anyString());

        service.resendVerificationEmail("test@mail.com");

        verify(tokenRepository).deleteByUserId(1L);
        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(mailService).sendPlainText(eq("test@mail.com"), eq("Verify Your Email"), contains("/verify-email?token="));
    }

    @Test
    void testResendVerificationEmail_alreadyVerified() {
        user.setEmailVerified(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        assertThrows(IllegalStateException.class, () -> service.resendVerificationEmail("test@mail.com"));
    }

    @Test
    void testResendVerificationEmail_userNotFound() {
        when(userRepository.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.resendVerificationEmail("unknown@mail.com"));
    }

    @Test
    void testVerifyToken_success() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setId(1L);
        token.setToken("abc123");
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(2));

        when(tokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));
        when(userRepository.save(any(User.class))).thenReturn(user);

        service.verifyToken("abc123");

        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void testVerifyToken_tokenNotFound() {
        when(tokenRepository.findByToken("notfound")).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> service.verifyToken("notfound"));
    }

    @Test
    void testVerifyToken_expired() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("expired");
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(token));
        assertThrows(UnauthorizedException.class, () -> service.verifyToken("expired"));
    }
}