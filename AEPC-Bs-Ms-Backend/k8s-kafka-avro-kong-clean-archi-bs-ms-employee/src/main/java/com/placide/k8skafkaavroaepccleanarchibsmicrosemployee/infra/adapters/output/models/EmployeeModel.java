package com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.adapters.output.models;

import com.placide.k8skafkaavroaepccleanarchibsmicrosemployee.infra.adapters.input.feignclients.models.AddressModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employees")
public class EmployeeModel {
    @Id
    @GenericGenerator(name = "uuid")
    private String employeeId;
    private String firstname;
    private String lastname;
    @Column(unique = true)
    private String email;
    private String hireDate;
    private String state;
    private String type;
    private String addressId;
    @Transient
    private AddressModel address;
}
