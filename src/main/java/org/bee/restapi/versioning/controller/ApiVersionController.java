package org.bee.restapi.versioning.controller;

import org.bee.restapi.versioning.domain.ApiVersion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/")
public class ApiVersionController {
    @GetMapping(path = "v1/api")
    public @ResponseBody ApiVersion apiversion1() {
        return new ApiVersion("v1.0", "apiversion");
    }

    @GetMapping(path = ("v2/api"))
    public @ResponseBody ApiVersion apiversion2() {
        return new ApiVersion("v2.0", "apiversion");
    }

    @GetMapping(path = "api", params = "v1")
    public @ResponseBody ApiVersion apiversion1param() {
        return new ApiVersion("v1.0", "apiversion");
    }


    @GetMapping(path = "api", params = "v2")
    public @ResponseBody ApiVersion apiversion2param() {
        return new ApiVersion("v2.0", "apiversion");
    }


    @GetMapping(path = "api", headers = "X-API-VERSION=1")
    public @ResponseBody ApiVersion apiversion1header() {
        return new ApiVersion("v1.0", "apiversion using headers =X-API-VERSION=1");
    }


    @GetMapping(path = "api", headers = "X-API-VERSION=2")
    public @ResponseBody ApiVersion apiversion2header() {
        return new ApiVersion("v2.0", "apiversion using headers =X-API-VERSION=2");
    }


    @GetMapping(path = "api", produces = "application/vnd.company.app-v1+json")
    public @ResponseBody ApiVersion apiversion1produce() {
        return new ApiVersion("v1.0", "apiversion using produce =\"+\"application//vnd.company.app-v1+json\"");
    }


    @GetMapping(path = "/api", produces = "application/vnd.company.app-v2+json")
    public @ResponseBody ApiVersion apiversion2produce() {
        return new ApiVersion("v2.0", "apiversion using produce =\"+\"application//vnd.company.app-v2+json\"");
    }
}
