package org.bee.banking.contoller;

import lombok.RequiredArgsConstructor;
import org.bee.banking.domain.BankLocations;
import org.bee.banking.domain.BankOperationServices;
import org.bee.banking.domain.LocationType;
import org.bee.banking.service.LocationBasedOperationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/locations")
@RequiredArgsConstructor
public class BankLocationController {
    private final LocationBasedOperationService locationService;

    /**
     * Search bank offices/ATMs. All filters optional. {@code type} matches by capability:
     * OFFICE and ATM each also return office+ATM (BOTH) branches (see
     * {@code LocationBasedOperationRepository#search}).
     * GET /v1/api/locations?type=ATM&state=TX&service=ATM_DEPOSIT&sort=city,asc
     */
    @GetMapping
    public ResponseEntity<Page<BankLocations>> listLocations(
            @RequestParam(required = false) LocationType type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false) BankOperationServices service,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(locationService.listLocations(type, city, state, zip, service, pageable));
    }
}
