package com.shopee.banking.bams.app.service.impl;

import com.shopee.banking.bams.app.service.ICustomerService;
import com.shopee.banking.bams.app.service.dto.CreateCustomerByCsvResult;
import com.shopee.banking.bams.common.exception.BizException;
import com.shopee.banking.bams.common.exception.DependencyException;
import com.shopee.banking.bams.common.exception.enums.BizErrorCode;
import com.shopee.banking.bams.common.exception.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.enums.Gender;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.domain.aggregateRoot.Customer;
import com.shopee.banking.bams.infra.dal.repository.impl.CustomerRepositoryImpl;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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
    private static final int MAX_EXPORT_ROWS = 10000;
    private static final int BATCH_SIZE = 500;
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("\\d{10}");
    private static final String INVALID_HEADER_ERROR = "Invalid CSV header.";
    private static final String INVALID_ACCOUNT_NUMBER_ERROR = "Invalid account number.";
    private static final String INVALID_NAME_ERROR = "Invalid name.";
    private static final String INVALID_GENDER_ERROR = "Invalid gender.";
    private static final String DUPLICATE_ACCOUNT_NUMBER_ERROR = "Duplicate account number.";

    @Autowired
    private CustomerRepositoryImpl customerRepository;

    @Override
    @Transactional
    public CreateCustomerByCsvResult createCustomerByCSV(String csvFilePath){
        Asserter.assertNotNull(csvFilePath, ParamErrorCode.NULL_PARAM);
        Asserter.assertTrue(!csvFilePath.isBlank(), ParamErrorCode.INVALID_PARAM, "csvFilePath");
        Path csvPath = validateCsvFileExists(csvFilePath);

        List<CsvValidationError> errors = validateCsvEntries(csvPath);
        Asserter.assertTrue(errors.isEmpty(), BizErrorCode.INVALID_CSV_FILE, errors);

        int modifiedCount = 0;
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setIgnoreEmptyLines(false)
                     .build()
                     .parse(reader)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator.hasNext()) {
                iterator.next();
            }

            while (iterator.hasNext()) {
                List<Customer> customers = readNextCustomerBatch(iterator, BATCH_SIZE);
                if (customers.isEmpty()){
                    break;
                }
                modifiedCount += customerRepository.batchUpsert(customers);
            }
        } catch (IOException e) {
            throw new BizException(BizErrorCode.INVALID_CSV_FILEPATH);
        }
        return new CreateCustomerByCsvResult(modifiedCount);
    }

    @Override
    public Customer viewCustomerProfile(String accNo) {
        Asserter.assertNotNull(accNo, ParamErrorCode.NULL_PARAM, "AccNo");
        Asserter.assertTrue(ACCOUNT_NUMBER_PATTERN.matcher(accNo).matches(), ParamErrorCode.INVALID_PARAM, "accNo");
        Customer customer =  customerRepository.getCustomerByAccNo(accNo);
        Asserter.assertNotNull(customer, BizErrorCode.CUSTOMER_NOT_FOUND_MAPPING, accNo);
        return customer;
    }

    @Override
    public String exportCustomersByDates(LocalDateTime startDate, LocalDateTime endDate) {
        Asserter.assertNotNull(startDate, ParamErrorCode.NULL_PARAM, "startDate");
        Asserter.assertNotNull(endDate, ParamErrorCode.NULL_PARAM, "endDate");
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
            DateTimeFormatter csvDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            writer.write("account_number,name,gender,created_at,modified_at");
            writer.newLine();

            Consumer<Customer> consumer = customer -> {
                try {
                    writer.write(String.join(",",
                            customer.getAccountNumber(),
                            customer.getName(),
                            customer.getGender().name(),
                            customer.getCreatedAt().format(csvDateFormatter),
                            customer.getModifiedAt().format(csvDateFormatter)
                    ));
                    writer.newLine();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };

            customerRepository.selectCustomersByDates(startDate, endDate, MAX_EXPORT_ROWS, consumer);
        } catch (IOException | UncheckedIOException ex) {
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

    private List<Customer> readNextCustomerBatch(Iterator<CSVRecord> iterator, int batchSize) {
        List<Customer> customers = new ArrayList<>(batchSize);
        while (iterator.hasNext() && customers.size() < batchSize) {
            CSVRecord record = iterator.next();
            String accountNumber = getValue(record, ACCOUNT_NUMBER_COLUMN).trim();
            String name = getValue(record, NAME_COLUMN).trim();
            String gender = getValue(record, GENDER_COLUMN).trim();
            customers.add(Customer.builder()
                    .accountNumber(accountNumber)
                    .gender(Gender.valueOf(gender))
                    .name(name)
                    .build()
            );
        }
        return customers;
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
                return errors;
            }

            CSVRecord headerRecord = iterator.next();
            if (!isExpectedHeader(headerRecord)) {
                errors.add(new CsvValidationError(1, INVALID_HEADER_ERROR));
            }

            while (iterator.hasNext() && rowCount < MAX_CSV_ROWS) {
                CSVRecord record = iterator.next();
                int lineNumber = Math.toIntExact(record.getRecordNumber());
                String accountNumber = getTrimmedValue(record, ACCOUNT_NUMBER_COLUMN);
                String name = getTrimmedValue(record, NAME_COLUMN);
                String gender = getTrimmedValue(record, GENDER_COLUMN);
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
            errors.sort(null);
            return errors;
        } catch (IOException e) {
            throw new BizException(BizErrorCode.INVALID_CSV_FILEPATH);
        }
    }

    private boolean isExpectedHeader(CSVRecord headerRecord) {
        return headerRecord.size() == 3
                && ACCOUNT_NUMBER_HEADER.equals(getValue(headerRecord, ACCOUNT_NUMBER_COLUMN))
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
        for (Gender customerGender : Gender.values()) {
            if (customerGender.name().equals(gender)) {
                return true;
            }
        }
        return false;
    }

    private String getValue(CSVRecord record, int index) {
        return record.size() > index ? record.get(index) : null;
    }

    private String getTrimmedValue(CSVRecord record, int index) {
        String value = getValue(record, index);
        return value == null ? null : value.trim();
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
