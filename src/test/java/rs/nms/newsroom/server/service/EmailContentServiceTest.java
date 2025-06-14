package rs.nms.newsroom.server.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailContentServiceTest {

    @Mock
    private MessageSource messageSource;

    @Test
    void testBuildPasswordResetEmail() {
        when(messageSource.getMessage(eq("email.reset.body"),
                any(Object[].class), any(Locale.class)))
                .thenReturn("Dear Marko, click here: url");

        EmailContentService service = new EmailContentService(messageSource);
        String result = service.buildPasswordResetEmail("Marko", "url", Locale.forLanguageTag("en"));

        assertTrue(result.contains("click here"));
        assertTrue(result.contains("url"));
    }
}