package com.ukastar.service.auth;

import com.ukastar.domain.account.Account;
import com.ukastar.security.jwt.TokenPair;

/**
 * 包含账号信息与对应的令牌对。
 */
public record AuthSession(Account account, TokenPair tokenPair) {
}
