package com.ukastar.security.support;

import com.ukastar.security.model.CurrentUser;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 默认的系统级当前用户提供者，返回系统用户占位。
 */
@Component
public class SystemCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Mono<CurrentUser> currentUser() {
        return Mono.just(CurrentUser.system());
    }
}
