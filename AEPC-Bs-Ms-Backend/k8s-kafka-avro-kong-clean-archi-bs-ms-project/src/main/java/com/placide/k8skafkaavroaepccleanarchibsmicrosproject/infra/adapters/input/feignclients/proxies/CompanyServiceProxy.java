package com.placide.k8skafkaavroaepccleanarchibsmicrosproject.infra.adapters.input.feignclients.proxies;

import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.infra.adapters.input.feignclients.fallback.CompanyServiceFallback;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.infra.adapters.input.feignclients.models.CompanyModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "k8s-kafka-avro-kong-bs-ms-company",
        url = "http://k8s-kafka-avro-kong-bs-ms-company:8682",
        path = "/bs-ms-company-api",
        fallback = CompanyServiceFallback.class)
@Qualifier(value = "company-service-proxy")
public interface CompanyServiceProxy {
    @GetMapping(value = "/companies/id/{companyId}")
    CompanyModel loadRemoteCompanyApiById(@PathVariable(name = "companyId") String companyId);
    @GetMapping(value = "/companies/name/{companyName}")
    List<CompanyModel> loadRemoteCompanyApiByName(@PathVariable(name = "companyName") String companyName);
    @GetMapping(value = "/companies/agency/{agency}")
    List<CompanyModel> loadRemoteCompanyApiByAgency(@PathVariable(name = "agency") String agency);
}
