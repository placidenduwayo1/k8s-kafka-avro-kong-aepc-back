package com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.fallbacks;

import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.exceptions.ExceptionMgs;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.model.EmployeeModel;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.proxy.EmployeeServiceProxy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeServiceProxyFallback implements EmployeeServiceProxy {
    @Override
    public List<EmployeeModel> getRemoteEmployeesLivingAtAddress(String addressId) {
        return List.of(EmployeeModel.builder()
                .employeeId(ExceptionMgs.ADDRESS_ASSIGNED_EMPLOYEE_EXCEPTION.getMsg())
                        .firstname(ExceptionMgs.ADDRESS_ASSIGNED_EMPLOYEE_EXCEPTION.getMsg())
                        .lastname(ExceptionMgs.ADDRESS_ASSIGNED_EMPLOYEE_EXCEPTION.getMsg())
                        .email(ExceptionMgs.ADDRESS_ASSIGNED_EMPLOYEE_EXCEPTION.getMsg())
                        .hireDate(ExceptionMgs.ADDRESS_ASSIGNED_EMPLOYEE_EXCEPTION.getMsg())
                        .state(ExceptionMgs.ADDRESS_ASSIGNED_EMPLOYEE_EXCEPTION.getMsg())
                        .type(ExceptionMgs.ADDRESS_ASSIGNED_EMPLOYEE_EXCEPTION.getMsg())
                .build());
    }
}
