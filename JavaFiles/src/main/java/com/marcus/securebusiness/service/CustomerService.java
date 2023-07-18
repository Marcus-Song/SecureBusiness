package com.marcus.securebusiness.service;

import com.marcus.securebusiness.model.Customer;
import com.marcus.securebusiness.model.Invoice;
import com.marcus.securebusiness.model.Stats;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface CustomerService {
    // Customer Functions
    Customer createCustomer(Customer customer);
    Customer updateCustomer(Customer customer);
    Page<Customer> getCustomers(int page, int size);
    Iterable<Customer> getCustomers();
    Customer getCustomerById(Long id);
    Page<Customer> searchCustomers(String name, int page, int size);
    // Invoice Functions
    Invoice createInvoice(Invoice invoice);
    Page<Invoice> getInvoices(int page, int size);
    void addInvoiceToCustomer(Long id, Invoice invoice);

    Invoice getinvoiceById(Long id);

    Stats getStats();
    Double getCustomerTotal(Long customerId);

    void sendInvoiceEmail(Long invoiceId, MultipartFile pdf);

    void updateImage(Customer customer, MultipartFile image);
}
