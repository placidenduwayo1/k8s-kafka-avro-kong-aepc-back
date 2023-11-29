package com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.input.feignclient.fallbacks;

import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.exceptions.ExceptionMsg;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.input.feignclient.models.ProjectModel;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.input.feignclient.proxies.ProjectServiceProxy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectServiceProxyFallback implements ProjectServiceProxy {
    @Override
    public List<ProjectModel> loadRemoteProjectsOfCompany(String companyId) {
        return List.of(ProjectModel.builder()
                .projectId(ExceptionMsg.COMPANY_ASSIGNED_PROJECT_EXCEPTION.getMessage())
                .name(ExceptionMsg.COMPANY_ASSIGNED_PROJECT_EXCEPTION.getMessage())
                .description(ExceptionMsg.COMPANY_ASSIGNED_PROJECT_EXCEPTION.getMessage())
                .state(ExceptionMsg.COMPANY_ASSIGNED_PROJECT_EXCEPTION.getMessage())
                .createdDate(ExceptionMsg.COMPANY_ASSIGNED_PROJECT_EXCEPTION.getMessage())
                .companyId(ExceptionMsg.COMPANY_ASSIGNED_PROJECT_EXCEPTION.getMessage())
                .build());
    }
}
