package org.bee.restapi.versioning.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiVersion {
    private String apiversion = "0.0";
    private String apiname;
}
