package com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.input.feignclient.proxies;

import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.exceptions.CompanyNotFoundException;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.input.feignclient.fallbacks.ProjectServiceProxyFallback;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.input.feignclient.models.ProjectModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "k8s-kafka-avro-kong-bs-ms-project", url = "http://k8s-kafka-avro-kong-bs-ms-project:8684",
        path = "/bs-ms-project-api", fallback = ProjectServiceProxyFallback.class)
@Qualifier(value="project-service-proxy")
public interface ProjectServiceProxy {
    @GetMapping(value = "/projects/companies/id/{companyId}")
    List<ProjectModel> loadRemoteProjectsOfCompany(@PathVariable(name = "companyId") String companyId) throws CompanyNotFoundException;
}
