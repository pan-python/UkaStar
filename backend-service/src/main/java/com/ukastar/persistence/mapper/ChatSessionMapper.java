package com.ukastar.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ukastar.persistence.entity.ChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {}

