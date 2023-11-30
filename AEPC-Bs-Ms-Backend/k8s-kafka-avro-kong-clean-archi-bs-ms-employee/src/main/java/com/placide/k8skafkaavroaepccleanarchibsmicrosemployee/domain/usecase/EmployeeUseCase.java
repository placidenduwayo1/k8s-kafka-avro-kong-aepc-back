package com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.usecase;

import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.avrobeans.EmployeeAvro;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.beans.address.Address;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.beans.employee.Employee;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.beans.project.Project;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.exceptions.*;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.ports.input.InputEmployeeService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.ports.input.RemoteInputAddressService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.ports.output.OutputEmployeeService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.ports.output.OutputKafkaProducerEmployeeService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.ports.output.RemoteOutputAddressService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.ports.output.RemoteOutputProjectService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.adapters.output.mapper.EmployeeMapper;
import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.adapters.output.models.EmployeeDto;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmployeeUseCase implements InputEmployeeService, RemoteInputAddressService {
    private final OutputKafkaProducerEmployeeService outputKafkaProducerEmployeeService;
    private final OutputEmployeeService outputEmployeeService;
    private final RemoteOutputAddressService remoteOutputAddressService;
    private final RemoteOutputProjectService remoteOutputProjectService;


    public EmployeeUseCase(OutputKafkaProducerEmployeeService outputKafkaProducerEmployeeService,
                           OutputEmployeeService outputEmployeeService,
                           RemoteOutputAddressService remoteOutputAddressService, RemoteOutputProjectService remoteOutputProjectService) {
        this.outputKafkaProducerEmployeeService = outputKafkaProducerEmployeeService;
        this.outputEmployeeService = outputEmployeeService;
        this.remoteOutputAddressService = remoteOutputAddressService;
        this.remoteOutputProjectService = remoteOutputProjectService;
    }

    private void checkEmployeeValidity(EmployeeDto employeeDto) throws
            EmployeeEmptyFieldsException, EmployeeStateInvalidException,
            EmployeeTypeInvalidException, RemoteApiAddressNotLoadedException {
        if (!Validator.isValidEmployee(
                employeeDto.getFirstname(), employeeDto.getLastname(),
                employeeDto.getState(), employeeDto.getType(),
                employeeDto.getAddressId())) {
            throw new EmployeeEmptyFieldsException();
        } else if (!Validator.checkStateValidity(employeeDto.getState())) {
            throw new EmployeeStateInvalidException();
        } else if (!Validator.checkTypeValidity(employeeDto.getType())) {
            throw new EmployeeTypeInvalidException();
        }
        Address address = getRemoteAddressById(employeeDto.getAddressId());
        if (Validator.remoteAddressApiUnreachable(address.getAddressId())) {
            throw new RemoteApiAddressNotLoadedException(address.toString());
        }
    }

    private void checkEmployeeAlreadyExist(EmployeeDto dto) throws EmployeeAlreadyExistsException {
        if (!loadEmployeeByInfo(dto.getFirstname(), dto.getLastname(), dto.getState(), dto.getType(),
                dto.getAddressId()).isEmpty()) {
            throw new EmployeeAlreadyExistsException();
        }
    }

    private void setEmployeeDependency(Employee employee, String addressId) throws RemoteApiAddressNotLoadedException {
        Address address = getRemoteAddressById(addressId);
        employee.setAddressId(addressId);
        employee.setAddress(address);
    }

    //kafka producer employee create, edit and delete events
    @Override
    public Employee produceKafkaEventEmployeeCreate(EmployeeDto employeeDto) throws
            EmployeeTypeInvalidException, EmployeeEmptyFieldsException,
            EmployeeStateInvalidException, RemoteApiAddressNotLoadedException,
            EmployeeAlreadyExistsException {

        Validator.formatter(employeeDto);
        checkEmployeeValidity(employeeDto);
        checkEmployeeAlreadyExist(employeeDto);
        Employee employee = EmployeeMapper.fromDto(employeeDto);
        employee.setEmployeeId(UUID.randomUUID().toString());
        employee.setHireDate(Timestamp.from(Instant.now()).toString());
        employee.setEmail(Validator.setEmail(employee.getFirstname(), employee.getLastname()));
        setEmployeeDependency(employee,employeeDto.getAddressId());
        EmployeeAvro employeeAvro = EmployeeMapper.fromBeanToAvro(employee);
        return EmployeeMapper.fromAvroToBean(outputKafkaProducerEmployeeService.produceKafkaEventEmployeeCreate(employeeAvro));
    }

    @Override
    public Employee createEmployee(Employee employee) throws RemoteApiAddressNotLoadedException {
        Employee saved= outputEmployeeService.saveEmployee(employee);
        setEmployeeDependency(saved, saved.getAddressId());
        return saved;
    }

    @Override
    public Employee produceKafkaEventEmployeeDelete(String employeeId) throws EmployeeNotFoundException, RemoteApiAddressNotLoadedException,
            EmployeeAlreadyAssignedProjectException {
        Employee employee = getEmployeeById(employeeId).orElseThrow(EmployeeNotFoundException::new);
        List<Project> projects = remoteOutputProjectService.loadRemoteProjectsAssignedToEmployee(employeeId);
        for(Project prj: projects){
            if(!prj.getProjectId().equals(ExceptionsMsg.EMPLOYEE_ASSIGNED_REMOTE_PROJECT_API_EXCEPTION.getMessage())){
                throw new EmployeeAlreadyAssignedProjectException(ExceptionsMsg.EMPLOYEE_ASSIGNED_REMOTE_PROJECT_API_EXCEPTION.getMessage() + projects);
            }
        }
        setEmployeeDependency(employee,employee.getAddressId());
        EmployeeAvro employeeAvro = EmployeeMapper.fromBeanToAvro(employee);
        return EmployeeMapper.fromAvroToBean(outputKafkaProducerEmployeeService.produceKafkaEventEmployeeDelete(employeeAvro));
    }

    @Override
    public String deleteEmployee(String employeeId) throws EmployeeNotFoundException, RemoteApiAddressNotLoadedException {
        Employee employee = getEmployeeById(employeeId).orElseThrow(EmployeeNotFoundException::new);
        outputEmployeeService.deleteEmployee(employee.getEmployeeId());
        return "Employee" + employee + "successfully deleted";
    }

    @Override
    public Employee produceKafkaEventEmployeeEdit(EmployeeDto employeeDto, String employeeId) throws
            RemoteApiAddressNotLoadedException, EmployeeNotFoundException, EmployeeTypeInvalidException, EmployeeEmptyFieldsException,
            EmployeeStateInvalidException {
        Validator.formatter(employeeDto);
        checkEmployeeValidity(employeeDto);

        Employee employee = getEmployeeById(employeeId).orElseThrow(EmployeeNotFoundException::new);
        employee.setFirstname(employeeDto.getFirstname());
        employee.setLastname(employeeDto.getLastname());
        employee.setEmail(Validator.setEmail(employeeDto.getFirstname(), employeeDto.getLastname()));
        employee.setState(employeeDto.getState());
        employee.setType(employeeDto.getType());
        setEmployeeDependency(employee,employeeDto.getAddressId());
        EmployeeAvro employeeAvro = EmployeeMapper.fromBeanToAvro(employee);
        return EmployeeMapper.fromAvroToBean(outputKafkaProducerEmployeeService.produceKafkaEventEmployeeEdit(employeeAvro));
    }

    @Override
    public Employee editEmployee(Employee payload) throws RemoteApiAddressNotLoadedException {
        Employee updated = outputEmployeeService.editEmployee(payload);
        setEmployeeDependency(updated, updated.getAddressId());
        return updated;
    }

    @Override
    public List<Employee> loadEmployeesByRemoteAddress(String addressId) throws RemoteApiAddressNotLoadedException {
        Address address = remoteOutputAddressService.getRemoteAddressById(addressId);
        if(Validator.remoteAddressApiUnreachable(addressId)){
            throw new RemoteApiAddressNotLoadedException(address.toString());
        }
        List<Employee> employees = outputEmployeeService.loadEmployeesByRemoteAddress(address.getAddressId());
        employees.forEach(employee -> {
            try {
                setEmployeeDependency(employee, addressId);
            } catch (RemoteApiAddressNotLoadedException e) {
                e.getMessage();
            }
        });
        return employees;
    }

    @Override
    public List<Employee> getEmployeesByLastname(String employeeLastname) {
        List<Employee> employees = outputEmployeeService.getEmployeesByLastname(employeeLastname);
        employees.forEach(employee -> {
            try {
                setEmployeeDependency(employee,employee.getAddressId());
            } catch (RemoteApiAddressNotLoadedException e) {
                e.getMessage();
            }
        });
        return employees;
    }

    @Override
    public List<Employee> loadEmployeesByRemoteAddressCity(String city) throws RemoteApiAddressNotLoadedException {
        List<Address> addresses = remoteOutputAddressService.loadRemoteAddressesByCity(city);
        for(Address address: addresses){
            if(address.getAddressId().equals(ExceptionsMsg.REMOTE_ADDRESS_API_EXCEPTION.getMessage())){
                throw new RemoteApiAddressNotLoadedException(address.toString());
            }
        }
        List<Employee> employees = new ArrayList<>();
        for(Address address: addresses){
            List<Employee> subList = outputEmployeeService.loadEmployeesByRemoteAddress(address.getAddressId());
            employees.addAll(subList);
        }
        employees.forEach(employee -> {
            try {
                setEmployeeDependency(employee, employee.getAddressId());
            } catch (RemoteApiAddressNotLoadedException e) {
                e.getMessage();
            }
        });
        return employees;
    }

    @Override
    public Optional<Employee> getEmployeeById(String employeeId) throws EmployeeNotFoundException, RemoteApiAddressNotLoadedException {
       Employee employee = outputEmployeeService.getEmployeeById(employeeId).orElseThrow(
                EmployeeNotFoundException::new);
       setEmployeeDependency(employee, employee.getAddressId());
       return Optional.of(employee);
    }

    @Override
    public List<Employee> loadEmployeeByInfo(String firstname, String lastname, String state, String type, String addressId) {
        List<Employee> employees = outputEmployeeService.loadEmployeeByInfo(firstname, lastname, state, type, addressId);
        employees.forEach(employee -> {
            try {
                setEmployeeDependency(employee, employee.getAddressId());
            } catch (RemoteApiAddressNotLoadedException e) {
                e.getMessage();
            }
        });

        return employees;
    }

    @Override
    public List<Employee> loadAllEmployees() {
        List<Employee> employees = outputEmployeeService.loadAllEmployees();
        employees.forEach(employee -> {
            try {
                setEmployeeDependency(employee, employee.getAddressId());
            } catch (RemoteApiAddressNotLoadedException e) {
                e.getMessage();
            }
        });
        return employees;
    }

    @Override
    public Address getRemoteAddressById(String addressId) throws RemoteApiAddressNotLoadedException {
        return remoteOutputAddressService.getRemoteAddressById(addressId);
    }

    @Override
    public List<Address> loadRemoteAllAddresses() {
        return remoteOutputAddressService.loadAllRemoteAddresses();
    }
}
