package com.shopee.banking.bams.domain.aggregateRoot;

import com.shopee.banking.bams.domain.valueObject.AdminId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class JwtToken extends BaseAggregateRoot {
    private AdminId adminId;
    private String token;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private boolean revoked;

    protected JwtToken(){

    }

    public JwtToken(String token){
        this.token = token;
    }

    public JwtToken(Long id,
                    AdminId adminId,
                    String token,
                    String tokenHash,
                    LocalDateTime expiresAt,
                    boolean revoked){
        super(id);
        this.adminId = adminId;
        this.token = token;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public boolean isValid(){
        return !revoked && expiresAt.isAfter(LocalDateTime.now());
    }
}
