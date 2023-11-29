package com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.infra.excetions_handler;

import com.placide.k8skafkaavroaepccleanarchibsmicrosaddress.domain.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsHandler {
    @ExceptionHandler(value = AddressAlreadyExistsException.class)
    public ResponseEntity<String> handleAddressAlreadyExistsException(){
        return new ResponseEntity<>(ExceptionMgs
                .ADDRESS_ALREADY_EXISTS_EXCEPTION
                .getMsg(),
                HttpStatus.NOT_ACCEPTABLE);
    }
    @ExceptionHandler(value = AddressFieldsInvalidException.class)
    public ResponseEntity<String> handleAddressFieldsInvalidException(){
        return new ResponseEntity<>(ExceptionMgs
                .ADDRESS_FIELDS_INVALID_EXCEPTION
                .getMsg(),
                HttpStatus.NOT_ACCEPTABLE);

    }
    @ExceptionHandler(value = AddressNotFoundException.class)
    public ResponseEntity<String> handleAddressNotFoundException(){
        return new ResponseEntity<>(ExceptionMgs
                .ADDRESS_NOT_FOUND_EXCEPTION
                .getMsg(),
                HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(value = AddressCityNotFoundException.class)
    public ResponseEntity<String> handleAddressUnknownCityException(){
        return new ResponseEntity<>(ExceptionMgs
                .ADDRESS_CITY_NOT_EXCEPTION
                .getMsg(),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = AddressAlreadyAssignedCompanyException.class)
    public ResponseEntity<String> handleAddressAlreadyAssignedCompany(AddressAlreadyAssignedCompanyException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
    }
    @ExceptionHandler(value = AddressAlreadyAssignedEmployeeException.class)
    public ResponseEntity<String> handleAddressAlreadyAssignedEmployeeException(AddressAlreadyAssignedEmployeeException e){
        return new ResponseEntity<>(e.getMessage(),
                HttpStatus.NOT_ACCEPTABLE);
    }
}
