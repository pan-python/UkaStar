package com.ukastar.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ukastar.persistence.entity.AccountEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<AccountEntity> {
}

