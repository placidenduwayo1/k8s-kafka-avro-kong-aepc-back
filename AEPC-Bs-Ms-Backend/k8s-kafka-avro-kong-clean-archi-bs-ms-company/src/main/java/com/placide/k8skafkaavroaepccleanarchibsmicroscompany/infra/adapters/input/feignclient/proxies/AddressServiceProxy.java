package com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.input.feignclient.proxies;


import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.exceptions.RemoteApiAddressNotLoadedException;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.input.feignclient.fallbacks.AddressServiceProxyFallback;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.input.feignclient.models.AddressModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "k8s-kafka-avro-kong-bs-ms-address", url = "http://k8s-kafka-avro-kong-bs-ms-address:8681",
        path = "/bs-ms-address-api", fallback = AddressServiceProxyFallback.class)
@Qualifier(value = "address-service-proxy")
public interface AddressServiceProxy {
    @GetMapping(value = "/addresses/id/{addressId}")
    AddressModel loadRemoteApiGetAddressById(@PathVariable(name = "addressId") String addressId) throws RemoteApiAddressNotLoadedException;
    @GetMapping(value = "/addresses/city/{city}")
    List<AddressModel> loadRemoteApiAddressesByCity(@PathVariable(name = "city") String city);
}