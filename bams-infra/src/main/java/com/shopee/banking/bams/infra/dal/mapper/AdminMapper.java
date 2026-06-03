package com.shopee.banking.bams.infra.dal.mapper;

import com.shopee.banking.bams.infra.dal.dataObject.AdminDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {
    AdminDO selectById(@Param("id") Long id);

    AdminDO selectByIdForUpdate(@Param("id") Long id);

    AdminDO selectByUsername(@Param("username") String username);

    int updateProfile(@Param("id") Long id,
                      @Param("nickname") String nickname,
                      @Param("profilePictureUrl") String profilePictureUrl);
}
