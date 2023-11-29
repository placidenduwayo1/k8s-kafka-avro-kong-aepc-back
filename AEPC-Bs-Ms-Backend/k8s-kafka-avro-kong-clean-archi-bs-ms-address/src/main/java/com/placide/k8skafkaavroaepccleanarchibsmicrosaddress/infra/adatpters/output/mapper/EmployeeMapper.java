package com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.output.mapper;

import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.bean.Employee;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.model.EmployeeModel;
import org.springframework.beans.BeanUtils;

public class EmployeeMapper {
    private EmployeeMapper(){}
    public static Employee toBean(EmployeeModel model){
        Employee bean = new Employee();
        BeanUtils.copyProperties(model,bean);
        return bean;
    }
}
