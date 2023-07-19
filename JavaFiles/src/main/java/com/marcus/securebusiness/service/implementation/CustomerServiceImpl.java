package com.marcus.securebusiness.service.implementation;

import com.marcus.securebusiness.dto.UserDTO;
import com.marcus.securebusiness.enumeration.VerificationType;
import com.marcus.securebusiness.exception.ApiException;
import com.marcus.securebusiness.model.Customer;
import com.marcus.securebusiness.model.Invoice;
import com.marcus.securebusiness.model.Stats;
import com.marcus.securebusiness.repository.CustomerRepository;
import com.marcus.securebusiness.repository.InvoiceRepository;
import com.marcus.securebusiness.rowMapper.StatsRowMapper;
import com.marcus.securebusiness.service.CustomerService;
import com.marcus.securebusiness.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.marcus.securebusiness.query.StatsQuery.*;
import static com.marcus.securebusiness.query.UserQuery.UPDATE_USER_IMAGE_QUERY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final NamedParameterJdbcTemplate jdbc;
    private final EmailService emailService;
    @Override
    public Customer createCustomer(Customer customer, UserDTO userDTO) {
        customer.setCreateAt(new Date());
        createCustomerHelper(customer, userDTO);
        return customerRepository.save(customer);
    }

    private void createCustomerHelper(Customer customer, UserDTO userDTO) {
        try {
                List<String> belongsTo = new ArrayList<>(findCustomerByEmail(customer.getEmail()).getBelongsTo());
                Customer oldCustomer = findCustomerByEmail(customer.getEmail());
                if (oldCustomer != null)
                    customer = oldCustomer;
                if (belongsTo.contains(userDTO.getEmail())) {
                    log.error("Email: {} already exists in customer with id: {}", userDTO.getEmail(), customer.getId());
                    throw new ApiException("You have already created the same customer");
                }
                if (belongsTo.contains(""))
                    belongsTo = new ArrayList<>();
                belongsTo.add(userDTO.getEmail());
                customer.setBelongsTo(belongsTo);
        } catch (Exception exception) {throw new ApiException("Error occur when create customer with message: "+exception.getMessage());}
    }

    private Customer findCustomerByEmail(String email) {
        try {
            List<Customer> customers = customerRepository.findAll();
            List<Customer> output = new ArrayList<>();
            for (Customer customer: customers) {
                if (customer.getEmail().equals(email))
                    output.add(customer);
            }
            if (output.size() == 0)
                return null;
            if (output.size() > 1)
                log.warn("Find multiple customers: {} with email: {}", output.toString(), email);
            return output.get(0);
        } catch (Exception exception) {throw new ApiException("Error occur when finding customer by email"+exception.getMessage());}
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

    public Page<Customer> searchCustomerForUser(String name, int page, int size, UserDTO userDTO) {
        Iterable<Customer> allCustomers = getCustomers();
        Iterator<Customer> customerIterator = allCustomers.iterator();
        return null;
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

    @Override
    public void sendInvoiceEmail(Long invoiceId, MultipartFile pdf) {
        //String attachment = setCustomerPdfUrl(invoiceId.toString());
        String attachment = System.getProperty("user.home") + "/Download/invoices/" + invoiceId + ".pdf";
        savePdf(invoiceId.toString(), pdf);
        try {
            log.info("customer service - sendInvoiceEmail with attachment: {}", attachment);
            String name = invoiceRepository.findById(invoiceId).get().getCustomer().getName();
            String email = invoiceRepository.findById(invoiceId).get().getCustomer().getEmail();
            emailService.sendEmailWithAttachment(name, email, attachment, VerificationType.ATTACHMENT);
        } catch (Exception exception) {
            throw exception;
        }
    }

    private void savePdf(String invoiceId, MultipartFile pdf) {
        Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/Download/invoices/").toAbsolutePath().normalize();
        if (!Files.exists(fileStorageLocation)) {
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new ApiException("Unable to create directories to save pdf");
            }
            log.info("Created directories at: {}", fileStorageLocation);
        }
        try {
            Files.copy(pdf.getInputStream(), fileStorageLocation.resolve(invoiceId + ".pdf"), REPLACE_EXISTING);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException(exception.getMessage());
        }
        log.info("File saved in: {} Folder", fileStorageLocation);
    }

    @Override
    public void updateImage(Customer customer, MultipartFile image) {
        String imageUrl = setCustomerImageUrl(customer.getEmail());
        saveImage(customer.getEmail(), image);
        customer.setImageUrl(imageUrl);
        customerRepository.save(customer);
    }

    private String setCustomerImageUrl(String email) {
        return fromCurrentContextPath().path("/user/image/" + email + ".png").toUriString();
    }

    private void saveImage(String email, MultipartFile image) {
        Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/Download/images/").toAbsolutePath().normalize();
        if (!Files.exists(fileStorageLocation)) {
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new ApiException("Unable to create directories to save image");
            }
            log.info("Created directories at: {}", fileStorageLocation);
        }
        try {
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(email + ".png"), REPLACE_EXISTING);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException(exception.getMessage());
        }
        log.info("File saved in: {} Folder", fileStorageLocation);
    }


}
