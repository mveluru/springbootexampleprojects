package org.bee.banking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bee.banking.domain.BankLocations;
import org.bee.banking.domain.BankOperationServices;
import org.bee.banking.domain.LocationType;
import org.bee.banking.messages.BankingMessages;
import org.bee.banking.repository.LocationBasedOperationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Looks up bank offices/ATMs by location, type and the operations they serve. */
@Service
@Slf4j
@RequiredArgsConstructor
public class LocationBasedOperationService {
    private final LocationBasedOperationRepository locationRepository;

    public Page<BankLocations> listLocations(LocationType type, String city, String state, String zip,
                                             BankOperationServices service, Pageable pageable) {
        log.debug(BankingMessages.LOG_LOCATION_SEARCH, type, city, state, zip, service, pageable.getPageNumber());
        return locationRepository.search(type, city, state, zip, service, pageable);
    }
}
