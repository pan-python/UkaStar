package com.ukastar.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ukastar.persistence.entity.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageEntity> {}

