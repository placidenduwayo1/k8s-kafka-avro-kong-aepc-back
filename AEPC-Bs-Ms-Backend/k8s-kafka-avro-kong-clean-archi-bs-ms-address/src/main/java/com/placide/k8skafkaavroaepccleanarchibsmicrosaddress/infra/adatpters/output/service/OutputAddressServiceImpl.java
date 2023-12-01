package com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.output.service;

import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.beans.Address;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.beans.Company;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.beans.Employee;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.exceptions.AddressNotFoundException;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.ports.output.OutputAddressService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.ports.output.OutputRemoteCompanyService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.ports.output.OutputRemoteEmployeeService;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.models.EmployeeModel;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.proxy.CompanyServiceProxy;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.input.feignclients.proxy.EmployeeServiceProxy;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.output.mapper.AddressMapper;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.output.mapper.CompanyMapper;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.output.mapper.EmployeeMapper;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.output.models.AddressModel;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.output.repository.AddressRepository;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.adatpters.output.models.AddressDto;
import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.avrobean.AddressAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


@Service
@Slf4j
public class OutputAddressServiceImpl implements OutputAddressService, OutputRemoteCompanyService, OutputRemoteEmployeeService {

    private final AddressRepository addressRepository;
    private final  CompanyServiceProxy companyServiceProxy;
    private final  EmployeeServiceProxy employeeServiceProxy;

    public OutputAddressServiceImpl(AddressRepository addressRepository,
                                    @Qualifier(value = "company-service-proxy") CompanyServiceProxy companyServiceProxy,
                                    @Qualifier(value = "employee-service-proxy") EmployeeServiceProxy employeeServiceProxy) {
        this.addressRepository = addressRepository;
        this.companyServiceProxy = companyServiceProxy;
        this.employeeServiceProxy = employeeServiceProxy;
    }

    @Override
    @KafkaListener(topics = "avro-addresses-created", groupId = "address-group-id")
    public Address consumeAddressAdd(@Payload AddressAvro addressAvro,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Address bean = AddressMapper.mapAvroToBean(addressAvro);
        log.info("address: {} to add consumed from topic: {}", bean,topic);
       return bean;
    }

    @Override
    public Address saveInDbConsumedAddress(Address address) {
        AddressAvro addressAvro = AddressMapper.mapBeanToAvro(address);
        Address consumed = consumeAddressAdd(addressAvro,"avro-addresses-created");
        AddressModel addressModel = AddressMapper.mapBeanToModel(consumed);
        AddressModel savedAddress = addressRepository.save(addressModel);
        return AddressMapper.mapModelToBean(savedAddress);
    }

    @Override
    public List<Address> findAddressByInfo(AddressDto addressDto) {
        List<AddressModel> addressModels = addressRepository.findByNumAndStreetAndPoBoxAndCityAndCountry(
                addressDto.getNum(),
                addressDto.getStreet(),
                addressDto.getPoBox(),
                addressDto.getCity(),
                addressDto.getCountry()
        );

        return innerUtilityMethod(addressModels);

    }

    @Override
    public List<Address> getAllAddresses() {
        List<AddressModel> addressModels = addressRepository.findAll();
        return innerUtilityMethod(addressModels);
    }

    @Override
    public Optional<Address> getAddress(String addressID) throws AddressNotFoundException {
        AddressModel addressModel = addressRepository.findById(addressID).orElseThrow(
                AddressNotFoundException::new
        );
        return Optional.of(AddressMapper.mapModelToBean(addressModel));
    }

    @Override
    public List<Address> getAddressesOfGivenCity(String city) {
        List<AddressModel> addressModels = addressRepository.findByCity(city);

      return innerUtilityMethod(addressModels);
    }

    @Override
    @KafkaListener(topics = "avro-addresses-deleted", groupId = "address-group-id")
    public Address consumeAddressDelete(@Payload AddressAvro addressAvro,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Address address = AddressMapper.mapAvroToBean(addressAvro);
        log.info("address: {} to delete consumed from topic: {}", address,topic);
        return address;
    }

    @Override
    public String deleteAddress(String addressId) throws AddressNotFoundException {
        Address address = getAddress(addressId).orElseThrow(
                AddressNotFoundException::new);
        AddressAvro addressAvro = AddressMapper.mapBeanToAvro(address);
        Address consumed = consumeAddressDelete(addressAvro,"avro-addresses-deleted");
        addressRepository.deleteById(consumed.getAddressId());
        return "Address"+ address +"successfully deleted";
    }
    @Override
    @KafkaListener(topics = "avro-addresses-edited", groupId = "address-group-id")
    public Address consumeAddressEdit(@Payload AddressAvro addressAvro,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Address address = AddressMapper.mapAvroToBean(addressAvro);
        log.info("address: {} to edit consumed from topic: {}", address,topic);
        return address;
    }
    @Override
    public Address updateAddress(Address address) {
        AddressAvro addressAvro = AddressMapper.mapBeanToAvro(address);
        Address consumedAddress = consumeAddressEdit(addressAvro,"avro-addresses-edited");
        AddressModel addressModel = AddressMapper.mapBeanToModel(consumedAddress);
        return AddressMapper.mapModelToBean(addressRepository.save(addressModel));
    }

    private List<Address> innerUtilityMethod(List<AddressModel> addressModels) {
        Function<AddressModel, Address> mapper = AddressMapper::mapModelToBean;
        return addressModels
                .stream()
                .map(mapper)
                .toList();
    }

    @Override
    public Company getRemoteCompanyOnGivenAddress(String addressId) throws AddressNotFoundException {
        Address address = getAddress(addressId).orElseThrow(AddressNotFoundException::new);
       return CompanyMapper.toBean(companyServiceProxy.getRemoteCompanyAtAddress(address.getAddressId()));
    }

    @Override
    public List<Employee> getRemoteEmployeesLivingAtAddress(String addressId) throws AddressNotFoundException {
        Address address = getAddress(addressId).orElseThrow(AddressNotFoundException::new);
        List<EmployeeModel> models = employeeServiceProxy.getRemoteEmployeesLivingAtAddress(address.getAddressId());
        return models.stream()
                .map(EmployeeMapper::toBean).toList();
    }
}