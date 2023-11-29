package com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.usecase;

import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.avrobeans.ProjectAvro;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.beans.company.Company;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.beans.employee.Employee;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.beans.project.Project;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.exceptions.*;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.ports.input.InputProjectService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.ports.input.InputRemoteApiCompanyService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.ports.input.InputRemoteApiEmployeeService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.ports.output.OutputKafkaProducerProjectService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.ports.output.OutputProjectService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.ports.output.OutputRemoteApiCompanyService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.domain.ports.output.OutputRemoteApiEmployeeService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.infra.adapters.output.mappers.Mapper;
import com.placide.k8skafkaavroaepccleanarchibsmicrosproject.infra.adapters.output.models.ProjectDto;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class UseCase implements InputProjectService, InputRemoteApiEmployeeService, InputRemoteApiCompanyService {
    private final OutputKafkaProducerProjectService kafkaProducerService;
    private final OutputProjectService outputProjectService;
    private final OutputRemoteApiEmployeeService outputEmployeeAPIService;
    private final OutputRemoteApiCompanyService outputCompanyAPIService;
    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    public UseCase(OutputKafkaProducerProjectService kafkaProducerService, OutputProjectService outputProjectService,
                   OutputRemoteApiEmployeeService outputEmployeeAPIService, OutputRemoteApiCompanyService outputCompanyAPIService) {
        this.kafkaProducerService = kafkaProducerService;
        this.outputProjectService = outputProjectService;
        this.outputEmployeeAPIService = outputEmployeeAPIService;
        this.outputCompanyAPIService = outputCompanyAPIService;
    }

    private void checkProjectValidity(String name, String desc, int priority, String state, String employeeId, String companyId) throws
            ProjectFieldsEmptyException, ProjectPriorityInvalidException, ProjectStateInvalidException, RemoteEmployeeApiException,
            RemoteCompanyApiException, RemoteEmployeeStateUnauthorizedException {
        if (!Validator.isValidProject(name, desc, employeeId, companyId)) {
            throw new ProjectFieldsEmptyException();
        } else if (!Validator.isValidProject(priority)) {
            throw new ProjectPriorityInvalidException();
        } else if (!Validator.isValidProject(state)) {
            throw new ProjectStateInvalidException();
        }
        Employee employee = getRemoteEmployeeAPI(employeeId);
        if (Validator.remoteEmployeeApiUnreachable(employee.getEmployeeId())) {
            throw new RemoteEmployeeApiException(employee.toString());
        } else if (employee.getState().equals("archive")) {
            throw new RemoteEmployeeStateUnauthorizedException();
        }
        Company company = getRemoteApiCompany(companyId);
        if (Validator.remoteCompanyApiUnreachable(company.getCompanyId())) {
            throw new RemoteCompanyApiException(company.toString());
        }
    }

    private void checkProjectAlreadyExists(String name, String desc, String state, String employeeId, String companyId) throws
            ProjectAlreadyExistsException {
        if (!loadProjectByInfo(name, desc, state, employeeId, companyId).isEmpty()) {
            throw new ProjectAlreadyExistsException();
        }
    }

    private void setProjectDependencies(Project project, String employeeId, String companyId) throws RemoteEmployeeApiException,
            RemoteCompanyApiException {
        Employee employee = getRemoteEmployeeAPI(employeeId);
        project.setEmployeeId(employeeId);
        project.setEmployee(employee);
        Company company = getRemoteApiCompany(companyId);
        project.setCompanyId(companyId);
        project.setCompany(company);
    }

    @Override
    public Project produceKafkaEventProjectCreate(ProjectDto projectDto) throws ProjectAlreadyExistsException,
            ProjectPriorityInvalidException, ProjectStateInvalidException, RemoteEmployeeApiException, RemoteCompanyApiException,
            ProjectFieldsEmptyException, RemoteEmployeeStateUnauthorizedException {

        Validator.format(projectDto);
        checkProjectValidity(projectDto.getName(), projectDto.getDescription(), projectDto.getPriority(), projectDto.getState(),
                projectDto.getEmployeeId(), projectDto.getCompanyId());
        checkProjectAlreadyExists(projectDto.getName(), projectDto.getDescription(), projectDto.getState(), projectDto.getEmployeeId(),
                projectDto.getCompanyId());
        Project project = Mapper.fromTo(projectDto);
        project.setProjectId(UUID.randomUUID().toString());
        project.setCreatedDate(Timestamp.from(Instant.now()).toString());
        setProjectDependencies(project,projectDto.getEmployeeId(), projectDto.getCompanyId());
        ProjectAvro projectAvro = Mapper.fromBeanToAvro(project);
        return Mapper.fromAvroToBean(kafkaProducerService.produceKafkaEventProjectCreate(projectAvro));
    }

    @Override
    public Project createProject(Project project) throws RemoteCompanyApiException, RemoteEmployeeApiException {
        Project saved= outputProjectService.saveProject(project);
        setProjectDependencies(saved, saved.getEmployeeId(), saved.getCompanyId());
        return saved;
    }

    @Override
    public Optional<Project> getProject(String projectId) throws ProjectNotFoundException {
        return Optional.of(outputProjectService.getProject(projectId).orElseThrow(ProjectNotFoundException::new));
    }

    @Override
    public List<Project> loadProjectByInfo(String name, String desc, String state, String employeeId, String companyId) {
        return outputProjectService.loadProjectByInfo(name, desc, state, employeeId, companyId);
    }

    @Override
    public Project produceKafkaEventProjectDelete(String projectId) throws ProjectNotFoundException, RemoteEmployeeApiException,
            RemoteCompanyApiException, ProjectAssignedRemoteEmployeeApiException, ProjectAssignedRemoteCompanyApiException {
        Project project = getProject(projectId).orElseThrow(ProjectNotFoundException::new);
        Employee employee = getRemoteEmployeeAPI(project.getEmployeeId());
        Company company = getRemoteApiCompany(project.getCompanyId());
        if(!employee.getEmployeeId().equals(ExceptionMsg.REMOTE_EMPLOYEE_API_EXCEPTION.getMessage())){
            throw new ProjectAssignedRemoteEmployeeApiException(ExceptionMsg.PROJECT_ASSIGNED_TO_EMPLOYEE_EXCEPTION.getMessage()+employee);
        }else if (!company.getCompanyId().equals(ExceptionMsg.REMOTE_COMPANY_API_EXCEPTION.getMessage())){
            throw new ProjectAssignedRemoteCompanyApiException(ExceptionMsg.PROJECT_ASSIGNED_TO_COMPANY_EXCEPTION.getMessage()+company);
        }
        setProjectDependencies(project, project.getEmployeeId(), project.getCompanyId());
        ProjectAvro projectAvro = Mapper.fromBeanToAvro(project);
        return Mapper.fromAvroToBean(kafkaProducerService.produceKafkaEventProjectDelete(projectAvro));
    }

    @Override
    public String deleteProject(String projectId) throws ProjectNotFoundException {
        Project project = getProject(projectId).orElseThrow(ProjectNotFoundException::new);
        outputProjectService.deleteProject(project.getProjectId());
        return "Project" + project + "successfully deleted";
    }

    @Override
    public Project produceKafkaEventProjectUpdate(ProjectDto projectDto, String projectId) throws ProjectNotFoundException,
            ProjectPriorityInvalidException, ProjectStateInvalidException, RemoteEmployeeApiException, RemoteCompanyApiException,
            ProjectFieldsEmptyException, RemoteEmployeeStateUnauthorizedException {

        Validator.format(projectDto);
        checkProjectValidity(projectDto.getName(), projectDto.getDescription(), projectDto.getPriority(), projectDto.getState(),
                projectDto.getEmployeeId(), projectDto.getCompanyId());

        Project project = getProject(projectId).orElseThrow(ProjectNotFoundException::new);
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        project.setPriority(project.getPriority());
        project.setState(projectDto.getState());
        setProjectDependencies(project, projectDto.getEmployeeId(), projectDto.getCompanyId());
        ProjectAvro projectAvro = Mapper.fromBeanToAvro(project);
        return Mapper.fromAvroToBean(kafkaProducerService.produceKafkaEventProjectEdit(projectAvro));
    }

    @Override
    public Project updateProject(Project payload) throws RemoteCompanyApiException, RemoteEmployeeApiException {
        setProjectDependencies(payload,payload.getEmployeeId(), payload.getCompanyId());
        Project updated = outputProjectService.updateProject(payload);
        setProjectDependencies(updated, updated.getEmployeeId(), updated.getCompanyId());
        return updated;
    }

    @Override
    public List<Project> loadProjectsAssignedToEmployee(String employeeId) throws RemoteEmployeeApiException {
        Employee employee = getRemoteEmployeeAPI(employeeId);
        if(Validator.remoteEmployeeApiUnreachable(employee.getEmployeeId())){
            throw new RemoteEmployeeApiException(employee.toString());
        }
        return loadProjectsOfEmployeeByEmployeeId(employee.getEmployeeId());
    }

    @Override
    public List<Project> loadProjectsOfCompany(String companyId) throws RemoteCompanyApiException {
        Company company = getRemoteApiCompany(companyId);
        if(Validator.remoteCompanyApiUnreachable(company.getCompanyId())){
            throw new RemoteCompanyApiException(company.toString());
        }
        return loadProjectsOfCompanyByCompanyId(company.getCompanyId());
    }

    @Override
    public List<Project> getAllProjects() {
        List<Project> projects = outputProjectService.getAllProjects();
        projects.forEach(project -> {
            try {
                setProjectDependencies(project, project.getEmployeeId(), project.getCompanyId());
            } catch (RemoteEmployeeApiException | RemoteCompanyApiException e) {
                logger.info(String.format(e.getMessage()));
            }
        });

        return projects;
    }

    @Override
    public Company getRemoteApiCompany(String companyId) throws RemoteCompanyApiException {
       return outputCompanyAPIService.getRemoteCompanyAPI(companyId);
    }

    @Override
    public Employee getRemoteEmployeeAPI(String employeeId) throws RemoteEmployeeApiException {
     return outputEmployeeAPIService.getRemoteEmployeeAPI(employeeId);
    }

    @Override
    public List<Employee> getRemoteEmployeesApiByLastname(String lastname) {
        return outputEmployeeAPIService.getRemoteEmployeesApiByLastname(lastname);
    }

    @Override
    public List<Company> getRemoteCompaniesByName(String companyName) {
        return outputCompanyAPIService.getRemoteCompaniesByName(companyName);
    }

    @Override
    public List<Project> loadProjectsOfCompanyName(String companyName) throws RemoteCompanyApiException {
        List<Company> companies = getRemoteCompaniesByName(companyName);
        if(companies.isEmpty()){
            throw new RemoteCompanyApiException(ExceptionMsg.REMOTE_COMPANY_API_NAME_NOT_FOUND_EXCEPTION.getMessage());
        }
        List<Project> projects = new ArrayList<>();
        for (Company c: companies){
            List<Project> subList = loadProjectsOfCompanyByCompanyId(c.getCompanyId());
            projects.addAll(subList);
        }
        return projects;
    }
    @Override
    public List<Company> getRemoteCompaniesByAgency(String agency) {
       return outputCompanyAPIService.getCompaniesByAgency(agency);
    }
    @Override
    public List<Project> getProjectsByCompanyAgency(String agency) throws RemoteCompanyApiException {
        List<Company> companies = getRemoteCompaniesByAgency(agency);
        if(companies.isEmpty()){
            throw new RemoteCompanyApiException(ExceptionMsg.REMOTE_COMPANY_API_AGENCY_NOT_FOUND_EXCEPTION.getMessage());
        }
        List<Project> projects = new ArrayList<>();
        for(Company c: companies){
            List<Project> subList = loadProjectsOfCompanyByCompanyId(c.getCompanyId());
            projects.addAll(subList);
        }
        return projects;
    }

    @Override
    public List<Project> loadProjectsAssignedToEmployeeLastname(String employeeLastname) throws RemoteEmployeeApiException{
        List<Employee> employees = getRemoteEmployeesApiByLastname(employeeLastname);
        if(employees.isEmpty()){
            throw new RemoteEmployeeApiException(ExceptionMsg.REMOTE_EMPLOYEE_API_EXCEPTION.getMessage());
        }
        List<Project> projects = new ArrayList<>();
        for(Employee e:employees){
            List<Project> subList = loadProjectsOfEmployeeByEmployeeId(e.getEmployeeId());
            projects.addAll(subList);
        }
        return projects;
    }

    private List<Project> loadProjectsOfCompanyByCompanyId(String companyId){
        List<Project> projects = outputProjectService.loadProjectsOfCompanyById(companyId);
        projects.forEach(project -> {
            try {
                setProjectDependencies(project, project.getEmployeeId(), project.getCompanyId());
            } catch (RemoteEmployeeApiException | RemoteCompanyApiException e) {
                logger.info(String.format(e.getMessage()));
            }
        });

        return projects;
    }

    private List<Project> loadProjectsOfEmployeeByEmployeeId(String employeeId){
        List<Project> projects = outputProjectService.loadProjectsAssignedToEmployee(employeeId);
        projects.forEach(project -> {
            try {
                setProjectDependencies(project,project.getEmployeeId(), project.getCompanyId());
            } catch (RemoteEmployeeApiException | RemoteCompanyApiException e) {
                logger.info(String.format(e.getMessage()));
            }
        });
        return projects;
    }

}
