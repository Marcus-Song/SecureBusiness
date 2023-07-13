package com.marcus.securebusiness.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.marcus.securebusiness.enumeration.InvoiceStatus;
import com.marcus.securebusiness.util.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Date;

import static jakarta.persistence.GenerationType.*;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String invoiceNumber;
    @Convert(converter = StringListConverter.class)
    private List<String> services;
    @Convert(converter = StringListConverter.class)
    private List<String> price;
    private Date date;
    private InvoiceStatus status;
    private double total;
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore // Ignore the loop in customer and invoice class
    private Customer customer;
}
