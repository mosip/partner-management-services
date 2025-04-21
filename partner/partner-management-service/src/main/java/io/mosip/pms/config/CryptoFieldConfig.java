package io.mosip.pms.config;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;

@Component
public class CryptoFieldConfig {

    private final Map<Class<?>, List<String>> entityFieldMap;

    public CryptoFieldConfig() {
        // Define which fields need encryption for which entities
        entityFieldMap = Map.of(
                //Partner.class, List.of("emailId", "contactNo", "address"),
                NotificationEntity.class, List.of("emailId")
        );
    }

    public List<String> getFieldsToEncrypt(Class<?> entityClass) {
        return entityFieldMap.getOrDefault(entityClass, List.of());
    }
}
