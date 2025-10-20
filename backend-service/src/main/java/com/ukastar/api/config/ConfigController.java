package com.ukastar.api.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.persistence.entity.SystemConfigEntity;
import com.ukastar.persistence.mapper.SystemConfigMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/configs")
public class ConfigController {

    private final SystemConfigMapper mapper;

    public ConfigController(SystemConfigMapper mapper) { this.mapper = mapper; }

    @GetMapping("/growth")
    public Map<String, Object> growth(@RequestParam(required = false) Long tenantId) {
        Map<String, Object> resp = new HashMap<>();
        Map<String, Integer> thresholds = new HashMap<>();
        thresholds.put("bronze", 100);
        thresholds.put("silver", 300);
        thresholds.put("gold", 600);
        if (tenantId != null) {
            SystemConfigEntity row = mapper.selectOne(new QueryWrapper<SystemConfigEntity>()
                    .eq("category", "growth")
                    .eq("config_key", "milestone_thresholds")
                    .eq("tenant_id", tenantId));
            if (row == null) {
                row = mapper.selectOne(new QueryWrapper<SystemConfigEntity>()
                        .eq("category", "growth")
                        .eq("config_key", "milestone_thresholds")
                        .isNull("tenant_id"));
            }
            if (row != null && row.getConfigValue() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> val = new com.fasterxml.jackson.databind.ObjectMapper().readValue(row.getConfigValue(), Map.class);
                    for (var e : val.entrySet()) {
                        if (e.getValue() instanceof Number n) {
                            thresholds.put(e.getKey(), n.intValue());
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        resp.put("milestone_thresholds", thresholds);
        return resp;
    }
}

