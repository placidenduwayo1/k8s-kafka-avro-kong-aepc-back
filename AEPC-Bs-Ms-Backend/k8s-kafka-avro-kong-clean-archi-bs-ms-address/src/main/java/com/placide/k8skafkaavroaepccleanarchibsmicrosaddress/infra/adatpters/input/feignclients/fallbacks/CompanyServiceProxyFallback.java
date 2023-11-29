package com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.fallbacks;

import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.exceptions.ExceptionMgs;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.model.CompanyModel;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.proxy.CompanyServiceProxy;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class CompanyServiceProxyFallback implements CompanyServiceProxy {
    @Override
    public CompanyModel getRemoteCompanyAtAddress(String addressId) {
        return CompanyModel.builder()
                .companyId(ExceptionMgs.ADDRESS_ASSIGNED_COMPANY_EXCEPTION.getMsg())
                .name(ExceptionMgs.ADDRESS_ASSIGNED_COMPANY_EXCEPTION.getMsg())
                .agency(ExceptionMgs.ADDRESS_ASSIGNED_COMPANY_EXCEPTION.getMsg())
                .type(ExceptionMgs.ADDRESS_ASSIGNED_COMPANY_EXCEPTION.getMsg())
                .connectedDate(ExceptionMgs.ADDRESS_ASSIGNED_COMPANY_EXCEPTION.getMsg())
                .build();
    }
}
