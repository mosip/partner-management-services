/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.pms.common.repository;

import org.springframework.data.repository.CrudRepository;

import io.mosip.pms.common.entity.ClientDetail;

import java.util.Optional;

public interface ClientDetailRepository extends CrudRepository<ClientDetail, String> {

    /**
     * case-sensitive query to fetch client with clientId and status
     * @param clientId
     * @param status
     * @return
     */
    Optional<ClientDetail> findByIdAndStatus(String clientId, String status);
    
    /**
     * 
     * @param clientName
     * @return
     */
    Optional<ClientDetail> findByName(String clientName);
}
