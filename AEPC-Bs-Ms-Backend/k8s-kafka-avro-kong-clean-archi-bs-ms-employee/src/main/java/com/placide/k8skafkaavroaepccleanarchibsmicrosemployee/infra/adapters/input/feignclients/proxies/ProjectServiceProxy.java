package com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.adapters.input.feignclients.proxies;

import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.adapters.input.feignclients.fallbacks.ProjectServiceProxyFallback;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.adapters.input.feignclients.models.ProjectModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "k8s-kafka-avro-kong-bs-ms-project", url = "http://k8s-kafka-avro-kong-bs-ms-project:8684",
        path = "/bs-ms-project-api", fallback = ProjectServiceProxyFallback.class)
@Qualifier(value="project-service-proxy")
public interface ProjectServiceProxy {
    @GetMapping(value = "/projects/employees/id/{employeeId}")
    List<ProjectModel> getRemoteProjectsAssignedToEmployee(@PathVariable(name = "employeeId") String employeeId);
}
