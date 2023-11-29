package com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.usecase;

import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.avrobeans.CompanyAvro;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.beans.address.Address;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.beans.project.Project;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.exceptions.*;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.ports.input.InputCompanyService;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.ports.input.InputRemoteAddressService;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.ports.output.OutputCompanyService;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.ports.output.OutputKafkaProducerService;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.beans.company.Company;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.ports.output.OutputRemoteAddressService;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.ports.output.OutputRemoteProjectService;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.output.mapper.CompanyMapper;
import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.infra.adapters.output.models.CompanyDto;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.placide.k8skafkaavroaepccleanarchibsmicroscompany.domain.exceptions.ExceptionMsg;

public class CompanyUseCase implements InputCompanyService, InputRemoteAddressService {
    private final OutputKafkaProducerService kafkaProducerService;
    private final OutputCompanyService outputCompanyService;
    private final OutputRemoteAddressService outputRemoteAddressService;
    private final OutputRemoteProjectService outputRemoteProjectService;
    public CompanyUseCase(OutputKafkaProducerService kafkaProducerService, OutputCompanyService outputCompanyService,
                          OutputRemoteAddressService outputRemoteAddressService, OutputRemoteProjectService outputRemoteProjectService) {
        this.kafkaProducerService = kafkaProducerService;
        this.outputCompanyService = outputCompanyService;
        this.outputRemoteAddressService = outputRemoteAddressService;
        this.outputRemoteProjectService = outputRemoteProjectService;
    }
    private void checkPayloadValidity(CompanyDto dto) throws CompanyEmptyFieldsException, CompanyTypeInvalidException,
            RemoteApiAddressNotLoadedException {
        if (!Validator.areValidCompanyFields(dto.getName(), dto.getAgency(), dto.getType(), dto.getAddressId())) {
            throw new CompanyEmptyFieldsException();
        } else if (!Validator.checkTypeExists(dto.getType())) {
            throw new CompanyTypeInvalidException();
        }
        Address address = getRemoteAddressById(dto.getAddressId());
        if (Validator.remoteAddressApiUnreachable(address.getAddressId())) {
            throw new RemoteApiAddressNotLoadedException(address.toString());
        }
    }
    private void checkCompanyAlreadyExists(CompanyDto dto) throws CompanyAlreadyExistsException {
        if (!loadCompanyByInfo(dto.getName(), dto.getAgency(), dto.getType()).isEmpty()) {
            throw new CompanyAlreadyExistsException();
        }
    }

    private void setCompanyDependency(Company company, String addressId) throws RemoteApiAddressNotLoadedException {
        Address address = getRemoteAddressById(addressId);
        company.setAddressId(addressId);
        company.setAddress(address);
    }

    @Override
    public Company produceKafkaEventCompanyCreate(CompanyDto dto) throws CompanyTypeInvalidException, CompanyEmptyFieldsException,
            CompanyAlreadyExistsException, RemoteApiAddressNotLoadedException{
        Validator.format(dto);
        checkPayloadValidity(dto);
        checkCompanyAlreadyExists(dto);
        Company bean = CompanyMapper.fromDtoToBean(dto);
        bean.setCompanyId(UUID.randomUUID().toString());
        bean.setConnectedDate(Timestamp.from(Instant.now()).toString());
        setCompanyDependency(bean, dto.getAddressId());
        CompanyAvro companyAvro = CompanyMapper.fromBeanToAvro(bean);
        return CompanyMapper.fromAvroToBean(kafkaProducerService.produceKafkaEventCompanyCreate(companyAvro));
    }

    @Override
    public Company createCompany(Company company) throws RemoteApiAddressNotLoadedException {
        Company saved = outputCompanyService.saveCompany(company);
        setCompanyDependency(saved, saved.getAddressId());
        return saved;
    }

    @Override
    public Optional<Company> getCompanyById(String id) throws CompanyNotFoundException, RemoteApiAddressNotLoadedException {
        Company company = outputCompanyService.getCompanyById(id).orElseThrow(CompanyNotFoundException::new);
        setCompanyDependency(company, company.getAddressId());
        return Optional.of(company);
    }

    @Override
    public List<Company> loadCompanyByInfo(String name, String agency, String type) {
        List<Company> companies = outputCompanyService.loadCompanyByInfo(name, agency, type);
        companies.forEach(company -> {
            try {
                setCompanyDependency(company, company.getAddressId());
            } catch (RemoteApiAddressNotLoadedException e) {
                e.getMessage();
            }
        });
        return companies;
    }

    @Override
    public List<Company> loadAllCompanies() {
        List<Company> companies = outputCompanyService.loadAllCompanies();
        companies.forEach(company -> {
            try {
                setCompanyDependency(company, company.getAddressId());
            } catch (RemoteApiAddressNotLoadedException e) {
                e.getMessage();
            }
        });

        return companies;
    }

    @Override
    public Company produceKafkaEventCompanyDelete(String id) throws CompanyNotFoundException, CompanyAlreadyAssignedRemoteProjectsException,
            RemoteApiAddressNotLoadedException {
        Company company = getCompanyById(id).orElseThrow(CompanyNotFoundException::new);
        List<Project> projects = outputRemoteProjectService.getRemoteProjectsOfCompany(id);
        for(Project project: projects){
            if(!project.getProjectId().equals(ExceptionMsg.COMPANY_ASSIGNED_PROJECT_EXCEPTION.getMessage())){
                throw new CompanyAlreadyAssignedRemoteProjectsException(ExceptionMsg.COMPANY_ASSIGNED_PROJECT_EXCEPTION.getMessage() + projects);
            }
        }
        setCompanyDependency(company, company.getAddressId());
        CompanyAvro companyAvro = CompanyMapper.fromBeanToAvro(company);
        return CompanyMapper.fromAvroToBean(kafkaProducerService.produceKafkaEventCompanyDelete(companyAvro));
    }

    @Override
    public String deleteCompany(String id) throws CompanyNotFoundException, RemoteApiAddressNotLoadedException {
        Company company = getCompanyById(id).orElseThrow(CompanyNotFoundException::new);
        outputCompanyService.deleteCompany(company.getCompanyId());
        return "Company <" + company + "> successfully deleted";
    }

    @Override
    public Company produceKafkaEventCompanyEdit(CompanyDto payload, String id) throws CompanyNotFoundException,
            CompanyTypeInvalidException, CompanyEmptyFieldsException, RemoteApiAddressNotLoadedException {
        Validator.format(payload);
        checkPayloadValidity(payload);
        Company company = getCompanyById(id).orElseThrow(CompanyNotFoundException::new);
        company.setName(payload.getName());
        company.setAgency(payload.getAgency());
        company.setType(payload.getType());
        setCompanyDependency(company, payload.getAddressId());
        CompanyAvro companyAvro = CompanyMapper.fromBeanToAvro(company);
        return CompanyMapper.fromAvroToBean(kafkaProducerService.produceKafkaEventCompanyEdit(companyAvro));
    }

    @Override
    public Company editCompany(Company payload) throws RemoteApiAddressNotLoadedException {
        Company company = outputCompanyService.editCompany(payload);
        setCompanyDependency(company, company.getAddressId());
        return company;
    }

    @Override
    public List<Company> getCompaniesByName(String companyName) {
        List<Company> companies = outputCompanyService.loadCompaniesByName(companyName);
        companies.forEach(company -> {
            try {
                setCompanyDependency(company, company.getAddressId());
            } catch (RemoteApiAddressNotLoadedException e) {
                e.getMessage();
            }
        });
        return companies;
    }

    @Override
    public List<Company> getCompanyByAgency(String agency) {
        List<Company> companies = outputCompanyService.getCompanyByAgency(agency);
        companies.forEach(company -> {
            try {
                setCompanyDependency(company, company.getAddressId());
            } catch (RemoteApiAddressNotLoadedException e) {
                e.getMessage();
            }
        });
        return companies;
    }

    @Override
    public Company getCompanyOfGivenAddressId(String addressId) throws RemoteApiAddressNotLoadedException, CompanyNotFoundException {
       Company company = outputCompanyService.getCompanyOfGivenAddressId(addressId);
       setCompanyDependency(company, company.getAddressId());
       return company;
    }

    @Override
    public List<Company> getCompaniesOfGivenAddressCity(String city) throws RemoteApiAddressNotLoadedException, CompanyNotFoundException {
        List<Address> addresses = getRemoteAddressesByCity(city);
        for(Address a: addresses){
            if (a.getAddressId().equals(ExceptionMsg.REMOTE_ADDRESS_API_EXCEPTION.getMessage())) {
                throw new RemoteApiAddressNotLoadedException(a.toString());
            }
        }

        List<Company> companies = new ArrayList<>();
        for (Address a : addresses) {
            companies.add(getCompanyOfGivenAddressId(a.getAddressId()));
        }
        companies.forEach(company -> {
            try {
                setCompanyDependency(company, company.getAddressId());
            } catch (RemoteApiAddressNotLoadedException e) {
                e.getMessage();
            }
        });
        return companies;
    }

    @Override
    public Address getRemoteAddressById(String addressId) throws RemoteApiAddressNotLoadedException{
        return outputRemoteAddressService.getRemoteAddressById(addressId);
    }

    @Override
    public List<Address> getRemoteAddressesByCity(String city) {
        return outputRemoteAddressService.getRemoteAddressesByCity(city);
    }
}
