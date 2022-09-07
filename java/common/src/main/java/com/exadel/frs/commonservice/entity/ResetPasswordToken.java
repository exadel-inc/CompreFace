package com.exadel.frs.commonservice.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reset_password_token", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordToken {

    @Id
    @GeneratedValue
    private UUID token;

    @Column(name = "expires_in")
    private LocalDateTime expiresIn;

    @OneToOne
    @JoinColumn(name = "user_email", referencedColumnName = "email")
    private User user;

    public ResetPasswordToken(final LocalDateTime expiresIn, final User user) {
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
