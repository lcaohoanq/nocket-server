package com.lcaohoanq.nocket.domain.user

import com.fasterxml.jackson.annotation.*
import com.lcaohoanq.nocket.base.entity.BaseEntity
import com.lcaohoanq.nocket.domain.avatar.Avatar
import com.lcaohoanq.nocket.domain.chat.ChatRoom
import com.lcaohoanq.nocket.domain.friendship.Friendship
import com.lcaohoanq.nocket.domain.reaction.PostReaction
import com.lcaohoanq.nocket.domain.wallet.Wallet
import com.lcaohoanq.nocket.enums.Gender
import com.lcaohoanq.nocket.enums.UserRole
import com.lcaohoanq.nocket.enums.UserStatus
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.experimental.SuperBuilder
import org.checkerframework.common.aliasing.qual.Unique
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User : BaseEntity(), UserDetails {
    @Column(name = "email", nullable = false, length = 100)
    @Email
    var email: String? = null

    @Column(name = "password", length = 200)
    @JsonProperty("password")
    var hashedPassword: String? = null

    var name: String? = null

    @Enumerated(EnumType.STRING)
    var gender: Gender? = null

    @Column(name = "is_active", columnDefinition = "boolean default true")
    @JsonProperty("is_active")
    var isActive: Boolean = false

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: UserStatus? = null

    @Column(name = "date_of_birth")
    var dateOfBirth: String? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnore
    var avatars: List<Avatar> = mutableListOf()

    @Column(name = "phone_number", nullable = false, length = 100)
    @JsonProperty("phone_number")
    var phoneNumber: String? = null

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL])
    @JsonManagedReference //to prevent infinite loop
    @JsonIgnore
    var wallet: Wallet? = null

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "role")
    var role: UserRole? = null

    @Column(name = "preferred_language")
    var preferredLanguage: String? = null

    @Column(name = "preferred_currency")
    var preferredCurrency: String? = null

    @Column(name = "last_login_timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    var lastLoginTimestamp: LocalDateTime? = null

    @OneToMany(mappedBy = "user1")
    @JsonIgnore
    var initiatedChats: MutableList<ChatRoom> = mutableListOf()

    @OneToMany(mappedBy = "user2")
    @JsonIgnore
    var receivedChats: MutableList<ChatRoom> = mutableListOf()

    @OneToMany(mappedBy = "user1")
    @JsonIgnore
    var initiatedFriendships: MutableList<Friendship> = mutableListOf()

    @OneToMany(mappedBy = "user2")
    @JsonIgnore
    var receivedFriendships: MutableList<Friendship> = mutableListOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonBackReference(value = "user-reactions")
    @JsonIgnore
    var reactions: MutableList<PostReaction> = mutableListOf()

    //Spring Security
    override fun getAuthorities(): Collection<GrantedAuthority> {
        val authorityList = mutableListOf<SimpleGrantedAuthority>()
        role?.let {
            authorityList.add(SimpleGrantedAuthority("ROLE_${it.name}"))
        }
        return authorityList
    }

    override fun getPassword(): String {
        return password ?: ""
    }

    override fun getUsername(): String {
        return email ?: ""
    }

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

    companion object {
        fun builder(): User = User()
    }
}