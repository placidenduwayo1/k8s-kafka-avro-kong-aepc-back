package com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.exceptions_handler;

import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.domain.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class EmployeeBusinessExceptionsHandler {
    @ExceptionHandler(value = EmployeeAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmployeeAlreadyExistsException() {
        return new ResponseEntity<>(ExceptionsMsg.EMPLOYEE_ALREADY_EXISTS_EXCEPTION
                .getMessage(),
                HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = EmployeeNotFoundException.class)
    public ResponseEntity<Object> handleEmployeeNotFoundException() {
        return new ResponseEntity<>(ExceptionsMsg.EMPLOYEE_NOT_FOUND_EXCEPTION
                .getMessage(),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = EmployeeEmptyFieldsException.class)
    public ResponseEntity<Object> handleEmployeeEmptyFieldsException() {
        return new ResponseEntity<>(ExceptionsMsg.EMPLOYEE_FIELDS_EMPTY_EXCEPTION
                .getMessage(),
                HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = RemoteApiAddressNotLoadedException.class)
    public ResponseEntity<Object> handleRemoteApiAddressNotLoadedException(RemoteApiAddressNotLoadedException exception) {
        return new ResponseEntity<>(exception.getMessage(),
                HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = EmployeeStateInvalidException.class)
    public ResponseEntity<Object> handleEmployeeStateInvalidException() {
        return new ResponseEntity<>(ExceptionsMsg.EMPLOYEE_UNKNOWN_STATE_EXCEPTION
                .getMessage(),
                HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = EmployeeTypeInvalidException.class)
    public ResponseEntity<Object> handleEmployeeTypeInvalidException() {
        return new ResponseEntity<>(ExceptionsMsg.EMPLOYEE_UNKNOWN_TYPE_EXCEPTION
                .getMessage(),
                HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = EmployeeAlreadyAssignedProjectException.class)
    public ResponseEntity<String> handleEmployeeAlreadyAssignedProjectException(EmployeeAlreadyAssignedProjectException e){
        return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_ACCEPTABLE);
    }
}
