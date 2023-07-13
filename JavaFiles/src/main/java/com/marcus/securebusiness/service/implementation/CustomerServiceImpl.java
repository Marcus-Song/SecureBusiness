package com.marcus.securebusiness.service.implementation;

import com.marcus.securebusiness.model.Customer;
import com.marcus.securebusiness.model.Invoice;
import com.marcus.securebusiness.model.Stats;
import com.marcus.securebusiness.repository.CustomerRepository;
import com.marcus.securebusiness.repository.InvoiceRepository;
import com.marcus.securebusiness.rowMapper.StatsRowMapper;
import com.marcus.securebusiness.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static com.marcus.securebusiness.query.StatsQuery.*;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.springframework.data.domain.PageRequest.of;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final NamedParameterJdbcTemplate jdbc;
    @Override
    public Customer createCustomer(Customer customer) {
        customer.setCreateAt(new Date());
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Page<Customer> getCustomers(int page, int size) {
        return customerRepository.findAll(of(page, size));
    }

    @Override
    public Iterable<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).get();
    }

    @Override
    public Page<Customer> searchCustomers(String name, int page, int size) {
        return customerRepository.findByNameContaining(name, of(page,  size));
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        invoice.setInvoiceNumber(randomAlphanumeric(8).toUpperCase());
        return invoiceRepository.save(invoice);
    }

    @Override
    public Page<Invoice> getInvoices(int page, int size) {
        return invoiceRepository.findAll(of(page, size));
    }

    @Override
    public void addInvoiceToCustomer(Long id, Invoice invoice) {
        invoice.setInvoiceNumber(randomAlphanumeric(8).toUpperCase());
        Customer customer = customerRepository.findById(id).get();
        invoice.setCustomer(customer);
        log.info(invoice.getServices().toString(), invoice.getPrice().toString());
        invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getinvoiceById(Long id) {
        //log.info(String.valueOf(invoiceRepository.findById(id).stream().toList().toString()));
        return invoiceRepository.findById(id).get();
    }

    @Override
    public Stats getStats() {
        return jdbc.queryForObject(STATS_QUERY, Map.of(), new StatsRowMapper());
    }

    @Override
    public Double getCustomerTotal(Long customerId) {
        try {
            Double total = jdbc.queryForObject(GET_CUSTOMER_TOTAL_QUERY, Map.of("id", customerId), Double.class);
            if (total == null) total = 0.0;
            return total;
        } catch (Exception exception) {
            throw exception;
        }
    }
}
