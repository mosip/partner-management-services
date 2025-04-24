package io.mosip.pms.interceptor;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.config.CryptoFieldConfig;
import io.mosip.pms.tasklets.util.KeyManagerHelper;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class CryptoInterceptor implements Interceptor {

	private Logger log = PMSLogger.getLogger(CryptoInterceptor.class);
	
    @Autowired
    KeyManagerHelper keyManagerHelper;

    @Autowired
    CryptoFieldConfig cryptoFieldConfig;

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
                                Object[] previousState, String[] propertyNames, Type[] types) {
        return processEntity(entity, currentState, propertyNames, true);
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state,
                          String[] propertyNames, Type[] types) {
        return processEntity(entity, state, propertyNames, true);
    }

//    @Override
//    public boolean onLoad(Object entity, Object id, Object[] state,
//                          String[] propertyNames, Type[] types) {
//        return processEntity(entity, state, propertyNames, false);
//    }
    
    private boolean processEntity(Object entity, Object[] state, String[] propertyNames, boolean isEncrypt) {
        if (entity == null) return false;
        log.debug("Processing entity, {}", entity);        
        // Get the fields that need to be encrypted/decrypted for this entity
        List<String> fieldsToProcess = cryptoFieldConfig.getFieldsToEncrypt(entity.getClass());
        boolean changed = false;

        for (int i = 0; i < propertyNames.length; i++) {
            if (fieldsToProcess.contains(propertyNames[i]) && state[i] != null) {
                if (isEncrypt) {
                	log.debug("Encrypting field, {}", propertyNames[i]);
                    // Encrypt the field
                    state[i] = keyManagerHelper.encryptData(state[i].toString());
                    log.debug("Encrypted value, {}", state[i]);
                } else {
                	log.debug("Decrypting field, {}", propertyNames[i]);
                    // Decrypt the field
                    state[i] = keyManagerHelper.decryptData(state[i].toString());
                    log.debug("Decrypted value, {}", state[i]);
                }
                changed = true;
            }
        }

        return changed;
    }
}
