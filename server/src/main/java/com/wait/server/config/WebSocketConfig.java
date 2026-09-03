package com.wait.server.config;

import com.wait.server.dto.MessageVO;
import com.wait.server.entity.UserEntity;
import com.wait.server.security.AuthContext;
import com.wait.server.service.MessageService;
import com.wait.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtDecoder jwtDecoder;
    private final UserService userService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://127.0.0.1:5173")
                .addInterceptors(new TokenHandshakeInterceptor())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端订阅前缀
        registry.enableSimpleBroker("/topic", "/queue");
        // 客户端发送前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 用户专属通道前缀
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new JwtChannelInterceptor(jwtDecoder, userService));
    }

    /** 从 query 参数 token 鉴权（前端 sockJS 无法塞 header） */
    static class TokenHandshakeInterceptor implements HandshakeInterceptor {
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                String token = servletRequest.getServletRequest().getParameter("token");
                if (token != null && !token.isBlank()) {
                    attributes.put("token", token);
                }
            }
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
        }
    }

    /** STOMP CONNECT 帧里再次校验 token，并把 user 绑定到 Principal */
    static class JwtChannelInterceptor implements ChannelInterceptor {
        private final JwtDecoder jwtDecoder;
        private final UserService userService;

        JwtChannelInterceptor(JwtDecoder jwtDecoder, UserService userService) {
            this.jwtDecoder = jwtDecoder;
            this.userService = userService;
        }

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                String token = accessor.getFirstNativeHeader("Authorization");
                if (token == null || !token.startsWith("Bearer ")) {
                    throw new IllegalArgumentException("missing Authorization");
                }
                token = token.substring(7);
                Jwt jwt = jwtDecoder.decode(token);
                String casdoorId = jwt.getClaimAsString("name");
                if (casdoorId == null || casdoorId.isBlank()) {
                    casdoorId = jwt.getSubject();
                }
                UserEntity user = userService.findByCasdoorId(casdoorId);
                if (user == null) {
                    throw new IllegalArgumentException("user not bound");
                }
                accessor.setUser(new StompPrincipal(user.getId().toString()));
            }
            return message;
        }
    }

    record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
