package com.jpsoftware.farmapp.user.entity;

import com.jpsoftware.farmapp.billing.model.BillingSubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean emailConfirmed = true;

    @Column
    private String emailConfirmationTokenHash;

    @Column
    private Instant emailConfirmationTokenExpiresAt;

    @Column(columnDefinition = "TEXT")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserPlan plan = UserPlan.defaultPlan();

    @Column
    private String stripeCustomerId;

    @Column
    private String stripeSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column
    private BillingSubscriptionStatus billingSubscriptionStatus;

    @Column(nullable = false)
    private boolean stripeCancelAtPeriodEnd = false;

    @Column
    private Instant stripeCurrentPeriodEnd;

    public UserEntity() {
    }

    public UserEntity(UUID id, String name, String email, String role) {
        this(id, name, email, role, "", true);
    }

    public UserEntity(UUID id, String name, String email, String role, String password) {
        this(id, name, email, role, password, true);
    }

    public UserEntity(UUID id, String name, String email, String role, String password, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
        this.active = active;
        this.plan = UserPlan.defaultPlan();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public String getEmailConfirmationTokenHash() {
        return emailConfirmationTokenHash;
    }

    public void setEmailConfirmationTokenHash(String emailConfirmationTokenHash) {
        this.emailConfirmationTokenHash = emailConfirmationTokenHash;
    }

    public Instant getEmailConfirmationTokenExpiresAt() {
        return emailConfirmationTokenExpiresAt;
    }

    public void setEmailConfirmationTokenExpiresAt(Instant emailConfirmationTokenExpiresAt) {
        this.emailConfirmationTokenExpiresAt = emailConfirmationTokenExpiresAt;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UserPlan getPlan() {
        return plan;
    }

    public void setPlan(UserPlan plan) {
        this.plan = plan == null ? UserPlan.defaultPlan() : plan;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }

    public BillingSubscriptionStatus getBillingSubscriptionStatus() {
        return billingSubscriptionStatus;
    }

    public void setBillingSubscriptionStatus(BillingSubscriptionStatus billingSubscriptionStatus) {
        this.billingSubscriptionStatus = billingSubscriptionStatus;
    }

    public boolean isStripeCancelAtPeriodEnd() {
        return stripeCancelAtPeriodEnd;
    }

    public void setStripeCancelAtPeriodEnd(boolean stripeCancelAtPeriodEnd) {
        this.stripeCancelAtPeriodEnd = stripeCancelAtPeriodEnd;
    }

    public Instant getStripeCurrentPeriodEnd() {
        return stripeCurrentPeriodEnd;
    }

    public void setStripeCurrentPeriodEnd(Instant stripeCurrentPeriodEnd) {
        this.stripeCurrentPeriodEnd = stripeCurrentPeriodEnd;
    }
}
