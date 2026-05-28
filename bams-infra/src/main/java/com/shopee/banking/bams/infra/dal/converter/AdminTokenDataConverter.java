package com.shopee.banking.bams.infra.dal.converter;

import com.shopee.banking.bams.domain.JwtToken;
import com.shopee.banking.bams.domain.valueObject.AdminId;
import com.shopee.banking.bams.infra.dal.dataObject.AdminTokenDO;
import org.springframework.stereotype.Component;

@Component
public class AdminTokenDataConverter implements BaseDataConverter<JwtToken, AdminTokenDO> {
    @Override
    public JwtToken toEntity(AdminTokenDO adminTokenDO) {
        if (adminTokenDO == null) {
            return null;
        }
        return new JwtToken(
                adminTokenDO.getId(),
                new AdminId(adminTokenDO.getAdminId()),
                adminTokenDO.getRefreshToken(),
                null,
                adminTokenDO.getExpiresAt(),
                Boolean.TRUE.equals(adminTokenDO.getRevoked())
        );
    }

    @Override
    public AdminTokenDO toDataObject(JwtToken jwtToken) {
        if (jwtToken == null) {
            return null;
        }
        AdminTokenDO adminTokenDO = new AdminTokenDO();
        adminTokenDO.setId(jwtToken.getId());
        adminTokenDO.setAdminId(jwtToken.getAdminId().getId());
        adminTokenDO.setRefreshToken(jwtToken.getToken());
        adminTokenDO.setRevoked(jwtToken.isRevoked());
        adminTokenDO.setCreatedAt(java.time.LocalDateTime.now());
        adminTokenDO.setExpiresAt(jwtToken.getExpiresAt());
        return adminTokenDO;
    }
}
