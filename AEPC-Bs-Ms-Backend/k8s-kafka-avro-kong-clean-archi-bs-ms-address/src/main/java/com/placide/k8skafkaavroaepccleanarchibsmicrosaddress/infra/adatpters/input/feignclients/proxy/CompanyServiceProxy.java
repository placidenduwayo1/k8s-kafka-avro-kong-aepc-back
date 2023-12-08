package com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.proxy;

import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.fallbacks.CompanyServiceProxyFallback;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.models.CompanyModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "k8s-kafka-avro-kong-bs-ms-company", url = "http://k8s-kafka-avro-kong-bs-ms-company:8682",
        path = "/bs-ms-company-api", fallback = CompanyServiceProxyFallback.class)
@Qualifier(value = "companyserviceproxy")
public interface CompanyServiceProxy {
    @GetMapping(value = "/companies/addresses/id/{addressId}")
    CompanyModel getRemoteCompanyAtAddress(@PathVariable(name = "addressId") String addressId);
}
