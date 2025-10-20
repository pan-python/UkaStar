package com.ukastar.security.support;

import com.ukastar.security.model.CurrentUser;
import reactor.core.publisher.Mono;

/**
 * 提供当前用户上下文，后续可接入安全框架。现阶段提供系统默认用户。
 */
public interface CurrentUserProvider {

    Mono<CurrentUser> currentUser();
}
