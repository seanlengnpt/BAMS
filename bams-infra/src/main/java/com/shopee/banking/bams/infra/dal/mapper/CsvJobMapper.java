package com.shopee.banking.bams.infra.dal.mapper;

import com.shopee.banking.bams.infra.dal.dataObject.CsvJobDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CsvJobMapper {
    int insertJob(@Param("job") CsvJobDO job);
    int updateJobSuccess(@Param("jobId") Long jobId, @Param("modifiedCount") long modifiedCount, @Param("status") String status);
    int updateJobFail(@Param("jobId") Long jobId,
                      @Param("status") String status,
                      @Param("errorCode") int errorCode,
                      @Param("errorMessage") String errorMessage);
}
