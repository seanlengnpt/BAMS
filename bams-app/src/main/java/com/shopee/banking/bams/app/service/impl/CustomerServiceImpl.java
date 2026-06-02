package com.shopee.banking.bams.app.service.impl;

import com.shopee.banking.bams.app.service.ICustomerService;
import com.shopee.banking.bams.app.service.dto.CreateCustomerByCsvResult;
import com.shopee.banking.bams.common.BizException;
import com.shopee.banking.bams.common.DependencyException;
import com.shopee.banking.bams.common.enums.BizErrorCode;
import com.shopee.banking.bams.common.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.enums.Gender;
import com.shopee.banking.bams.common.enums.ParamErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.domain.enums.CustomerGender;
import com.shopee.banking.bams.domain.repository.ICustomerRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class CustomerServiceImpl implements ICustomerService {

    private static final String ACCOUNT_NUMBER_HEADER = "account_number";
    private static final String NAME_HEADER = "name";
    private static final String GENDER_HEADER = "gender";
    private static final int ACCOUNT_NUMBER_COLUMN = 0;
    private static final int NAME_COLUMN = 1;
    private static final int GENDER_COLUMN = 2;
    private static final int ACCOUNT_NUMBER_LENGTH = 10;
    private static final int MAX_NAME_LENGTH = 250;
    private static final int MAX_CSV_ROWS = 10000;
    private static final int CHUNK_SIZE = 500;
    private static final String INVALID_HEADER_ERROR = "Invalid CSV header.";
    private static final String INVALID_ACCOUNT_NUMBER_ERROR = "Invalid account number.";
    private static final String INVALID_NAME_ERROR = "Invalid name.";
    private static final String INVALID_GENDER_ERROR = "Invalid gender.";
    private static final String DUPLICATE_ACCOUNT_NUMBER_ERROR = "Duplicate account number.";

    @Autowired
    private ICustomerRepository customerRepository;

    @Override
    @Transactional
    public CreateCustomerByCsvResult createCustomerByCSV(String csvFilePath){
        Asserter.assertNotNull(csvFilePath, ParamErrorCode.NULL_PARAM);
        Path csvPath = validateCsvFileExists(csvFilePath);

        List<CsvValidationError> errors = validateCsvEntries(csvPath);
        Asserter.assertTrue(errors.isEmpty(), BizErrorCode.INVALID_CSV_FILE, errors);

        int offset = 0;
        while (true) {
            List<Customer> customers = getCustomersByChunk(csvPath, offset);
            if (customers.isEmpty()){
                break;
            }
            customerRepository.batchUpsert(customers);
            offset += customers.size();
        }
        return new CreateCustomerByCsvResult();

    }


//    @Override
//    @Transactional
//    public CreateCustomerByCsvResult createCustomerByCSV(String csvFilePath) {
//        Asserter.assertNotNull(csvFilePath, ParamErrorCode.NULL_PARAM);
//        Path csvPath = validateCsvFileExists(csvFilePath);
//
//        ParsedCustomerCsv parsedCustomerCsv = parseAndValidateCsv(csvPath);
//        Asserter.assertTrue(parsedCustomerCsv.errors().isEmpty(), BizErrorCode.INVALID_CSV_FILE, parsedCustomerCsv.errors());
//
//        List<String> existingAccountNumbers = customerRepository.findExistingAccountNumbers(parsedCustomerCsv.customers()
//                .stream()
//                .map(Customer::getAccountNumber)
//                .toList());
//
//        List<Customer> existingCustomers =
//                parsedCustomerCsv.customers()
//                        .stream()
//                        .filter(customer ->
//                                existingAccountNumbers.contains(
//                                        customer.getAccountNumber()
//                                )
//                        )
//                        .toList();
//
//        List<Customer> newCustomers =
//                parsedCustomerCsv.customers()
//                        .stream()
//                        .filter(customer ->
//                                !existingAccountNumbers.contains(
//                                        customer.getAccountNumber()
//                                )
//                        )
//                        .toList();
//
//        int createdCount = customerRepository.batchInsert(newCustomers);
//        int modifiedCount = customerRepository.batchUpdate(existingCustomers);
//        return new CreateCustomerByCsvResult(createdCount, modifiedCount, List.of());
//    }

    @Override
    public Customer viewCustomerProfile(String accNo) {
        Asserter.assertNotNull(accNo, ParamErrorCode.NULL_PARAM, "AccNo");
        Customer customer =  customerRepository.getCustomerByAccNo(accNo);
        Asserter.assertNotNull(customer, BizErrorCode.CUSTOMER_NOT_FOUND_MAPPING, accNo);
        return customer;
    }

    @Override
    public String exportCustomersByDates(LocalDateTime startDate, LocalDateTime endDate) {
        Asserter.assertNotNull(startDate, ParamErrorCode.NULL_PARAM, "startDate");
        Asserter.assertNotNull(endDate, ParamErrorCode.NULL_PARAM, "endDate");

        List<List<Customer>> shardCustomerLists = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String tableName = "customers_" + i;

            List<Customer> shardCustomers =
                    customerRepository.selectCustomersByDates(
                            tableName,
                            startDate,
                            endDate
                    );

            shardCustomerLists.add(shardCustomers);
        }

        List<Customer> customers = mergeSortedCustomerLists(shardCustomerLists);

        return writeCustomersToCsv(customers);
    }

    private List<Customer> mergeSortedCustomerLists(List<List<Customer>> shardCustomerLists) {
        List<Customer> result = new ArrayList<>();

        PriorityQueue<CustomerCursor> minHeap = new PriorityQueue<>(
                Comparator.comparing(cursor -> cursor.customer().getCreatedAt())
        );

        for (int shardIndex = 0; shardIndex < shardCustomerLists.size(); shardIndex++) {
            List<Customer> shardCustomers = shardCustomerLists.get(shardIndex);

            if (!shardCustomers.isEmpty()) {
                minHeap.offer(new CustomerCursor(
                        shardCustomers.get(0),
                        shardIndex,
                        0
                ));
            }
        }

        while (!minHeap.isEmpty()) {
            CustomerCursor earliest = minHeap.poll();
            result.add(earliest.customer());

            int nextCustomerIndex = earliest.customerIndex() + 1;
            List<Customer> sameShardCustomers = shardCustomerLists.get(earliest.shardIndex());

            if (nextCustomerIndex < sameShardCustomers.size()) {
                minHeap.offer(new CustomerCursor(
                        sameShardCustomers.get(nextCustomerIndex),
                        earliest.shardIndex(),
                        nextCustomerIndex
                ));
            }
        }

        return result;
    }

    private record CustomerCursor(
            Customer customer,
            int shardIndex,
            int customerIndex
    ) {
    }

    private String writeCustomersToCsv(List<Customer> customers) {
        String directoryPath = "file-storage/exports";
        String fileName = "customers_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        Path filePath = Paths.get(directoryPath, fileName);

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new DependencyException(
                    DependencyErrorCode.EXPORT_CSV_FAILED,
                    "Unknown I/O Exception."
            );
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("account_number,name,gender,created_at,modified_at");
            writer.newLine();

            for (Customer customer : customers) {
                writer.write(String.join(",",
                        customer.getAccountNumber(),
                        customer.getName(),
                        customer.getGender().name(),
                        customer.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        customer.getModifiedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                ));
                writer.newLine();
            }
        } catch (IOException ex) {
            throw new DependencyException(
                    DependencyErrorCode.EXPORT_CSV_FAILED,
                    "Unknown I/O Exception."
            );
        }

        return filePath.toString();
    }

    private Path validateCsvFileExists(String csvFilePath) {
        try {
            Path path = Path.of(csvFilePath);
            if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
                throw new BizException(BizErrorCode.INVALID_CSV_FILEPATH);
            }
            return path;
        } catch (InvalidPathException | NullPointerException e) {
            throw new BizException(BizErrorCode.INVALID_CSV_FILEPATH);
        }
    }


    private List<CsvValidationError> validateCsvEntries(Path csvPath) {
        List<CsvValidationError> errors = new ArrayList<>();
        int rowCount = 0;
        Map<String, List<Integer>> linesByAccountNumber = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setIgnoreEmptyLines(false)
                     .build()
                     .parse(reader)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (!iterator.hasNext()) {
                errors.add(new CsvValidationError(1, INVALID_HEADER_ERROR));
            }

            CSVRecord headerRecord = iterator.next();
            if (!isExpectedHeader(headerRecord)) {
                errors.add(new CsvValidationError(1, INVALID_HEADER_ERROR));
            }

            while (iterator.hasNext() && rowCount < MAX_CSV_ROWS) {
                CSVRecord record = iterator.next();
                int lineNumber = Math.toIntExact(record.getRecordNumber());
                String accountNumber = getValue(record, ACCOUNT_NUMBER_COLUMN).trim();
                String name = getValue(record, NAME_COLUMN).trim();
                String gender = getValue(record, GENDER_COLUMN).trim();
                boolean valid = validateRow(lineNumber, accountNumber, name, gender, errors);
                if (valid){
                    linesByAccountNumber.computeIfAbsent(accountNumber, ignored -> new ArrayList<>()).add(lineNumber);
                }
                rowCount+=1;
            }
            addDuplicateAccountNumberErrors(linesByAccountNumber, errors);
            if (iterator.hasNext()) {
                errors.add(new CsvValidationError(MAX_CSV_ROWS + 2, "CSV exceeds maximum allowed rows."));
            }
            return errors;
        } catch (IOException e) {
            throw new BizException(BizErrorCode.INVALID_CSV_FILEPATH);
        }
    }
    /*
    private ParsedCustomerCsv parseAndValidateCsv(Path csvPath) {
        List<CsvValidationError> errors = new ArrayList<>();
        List<Customer> customers = new ArrayList<>();
        Map<String, List<Integer>> linesByAccountNumber = new LinkedHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setIgnoreEmptyLines(false)
                     .build()
                     .parse(reader)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (!iterator.hasNext()) {
                errors.add(new CsvValidationError(1, INVALID_HEADER_ERROR));
            }

            CSVRecord headerRecord = iterator.next();
            if (!isExpectedHeader(headerRecord)) {
                errors.add(new CsvValidationError(1, INVALID_HEADER_ERROR));
            }

            while (iterator.hasNext() && customers.size() < MAX_CSV_ROWS) {
                CSVRecord record = iterator.next();
                int lineNumber = Math.toIntExact(record.getRecordNumber());
                String accountNumber = getValue(record, ACCOUNT_NUMBER_COLUMN).trim();
                String name = getValue(record, NAME_COLUMN).trim();
                String gender = getValue(record, GENDER_COLUMN).trim();

                boolean valid = validateRow(lineNumber, accountNumber, name, gender, errors);
                if (valid) {
                    customers.add(Customer.builder()
                            .accountNumber(accountNumber)
                            .gender(Gender.valueOf(gender))
                            .name(name)
                            .build()
                    );
                    linesByAccountNumber.computeIfAbsent(accountNumber, ignored -> new ArrayList<>()).add(lineNumber);
                }
            }
            addDuplicateAccountNumberErrors(linesByAccountNumber, errors);
            return new ParsedCustomerCsv(customers, errors);
        } catch (IOException e) {
            throw new BizException(BizErrorCode.INVALID_CSV_FILEPATH);
        }
    }*/

    private boolean isExpectedHeader(CSVRecord headerRecord) {
        return ACCOUNT_NUMBER_HEADER.equals(getValue(headerRecord, ACCOUNT_NUMBER_COLUMN))
                && NAME_HEADER.equals(getValue(headerRecord, NAME_COLUMN))
                && GENDER_HEADER.equals(getValue(headerRecord, GENDER_COLUMN));
    }

    private boolean validateRow(int lineNumber,
                                String accountNumber,
                                String name,
                                String gender,
                                List<CsvValidationError> errors) {
        boolean valid = true;
        if (!(accountNumber != null
                && !accountNumber.isBlank()
                && accountNumber.trim().length() == ACCOUNT_NUMBER_LENGTH
                && accountNumber.trim().chars().allMatch(Character::isDigit))) {
            errors.add(new CsvValidationError(lineNumber, INVALID_ACCOUNT_NUMBER_ERROR));
            valid = false;
        }
        if (name == null || name.isBlank() || name.length() > MAX_NAME_LENGTH) {
            errors.add(new CsvValidationError(lineNumber, INVALID_NAME_ERROR));
            valid = false;
        }
        if (gender == null || !isValidGender(gender.trim())) {
            errors.add(new CsvValidationError(lineNumber, INVALID_GENDER_ERROR));
            valid = false;
        }
        return valid;
    }

    private boolean isValidGender(String gender) {
        for (CustomerGender customerGender : CustomerGender.values()) {
            if (customerGender.name().equals(gender)) {
                return true;
            }
        }
        return false;
    }

    private String getValue(CSVRecord record, int index) {
        return record.size() > index ? record.get(index) : null;
    }

    private void addDuplicateAccountNumberErrors(Map<String, List<Integer>> linesByAccountNumber,
                                                 List<CsvValidationError> errors) {
        for (List<Integer> lineNumbers : linesByAccountNumber.values()) {
            if (lineNumbers.size() > 1) {
                for (Integer lineNumber : lineNumbers) {
                    errors.add(new CsvValidationError(lineNumber, DUPLICATE_ACCOUNT_NUMBER_ERROR));
                }
            }
        }
    }


    private record ParsedCustomerCsv(List<Customer> customers,
                                     List<CsvValidationError> errors) {
    }

    private record CsvValidationError(int lineNumber, String message) implements Comparable<CsvValidationError> {
        @Override
        public int compareTo(CsvValidationError other) {
            int lineComparison = Integer.compare(lineNumber, other.lineNumber);
            if (lineComparison != 0) {
                return lineComparison;
            }
            return message.compareTo(other.message);
        }

        @Override
        public String toString() {
            return "Line " + lineNumber +  ": " + message;
        }
    }
}
