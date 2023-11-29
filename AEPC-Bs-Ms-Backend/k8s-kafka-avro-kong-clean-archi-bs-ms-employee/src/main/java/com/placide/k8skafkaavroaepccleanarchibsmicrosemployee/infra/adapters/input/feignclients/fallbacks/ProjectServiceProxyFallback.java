package com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.adapters.input.feignclients.fallbacks;

import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.exceptions.ExceptionsMsg;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.adapters.input.feignclients.models.ProjectModel;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.adapters.input.feignclients.proxies.ProjectServiceProxy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectServiceProxyFallback implements ProjectServiceProxy {
    @Override
    public List<ProjectModel> getRemoteProjectsAssignedToEmployee(String employeeId) {
        return List.of(ProjectModel.builder()
                .projectId(ExceptionsMsg.EMPLOYEE_ASSIGNED_REMOTE_PROJECT_API_EXCEPTION.getMessage())
                .name(ExceptionsMsg.EMPLOYEE_ASSIGNED_REMOTE_PROJECT_API_EXCEPTION.getMessage())
                .description(ExceptionsMsg.EMPLOYEE_ASSIGNED_REMOTE_PROJECT_API_EXCEPTION.getMessage())
                .priority(0)
                .state(ExceptionsMsg.EMPLOYEE_ASSIGNED_REMOTE_PROJECT_API_EXCEPTION.getMessage())
                .createdDate(ExceptionsMsg.EMPLOYEE_ASSIGNED_REMOTE_PROJECT_API_EXCEPTION.getMessage())
                .employeeId(ExceptionsMsg.EMPLOYEE_ASSIGNED_REMOTE_PROJECT_API_EXCEPTION.getMessage())
                .build());
    }
}
