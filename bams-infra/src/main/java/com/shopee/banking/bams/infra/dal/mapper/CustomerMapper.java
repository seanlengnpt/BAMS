package com.shopee.banking.bams.infra.dal.mapper;

import com.shopee.banking.bams.infra.dal.dataObject.CustomerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface CustomerMapper {
    List<String> selectExistingAccountNumbers(@Param("accountNumbers") Collection<String> accountNumbers);

    int batchInsert(@Param("customers") List<CustomerDO> customers);
    int batchUpdate(@Param("customers") List<CustomerDO> customers);
    int batchUpsert(@Param("tableName") String tableName, @Param("customers") List<CustomerDO> customers);
    CustomerDO selectCustomerByAccNo(@Param("accNo") String accNo, @Param("tableName") String tableName);
    List<CustomerDO> selectCustomersByDates(@Param("tableName") String tableName, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("offset") int offset);
}
