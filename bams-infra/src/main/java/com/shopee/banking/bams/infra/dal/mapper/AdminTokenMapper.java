package com.shopee.banking.bams.infra.dal.mapper;

import com.shopee.banking.bams.infra.dal.dataObject.AdminTokenDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminTokenMapper {
    int insert(AdminTokenDO adminTokenDO);

    AdminTokenDO selectByRefreshToken(@Param("refreshToken") String refreshToken);
}
