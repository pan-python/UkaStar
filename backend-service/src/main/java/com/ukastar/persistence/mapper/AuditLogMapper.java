package com.ukastar.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ukastar.persistence.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntity> {}

