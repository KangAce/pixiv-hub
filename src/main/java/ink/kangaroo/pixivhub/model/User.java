package ink.kangaroo.pixivhub.model;

import ink.kangaroo.pixivhub.model.enums.Enable;
import ink.kangaroo.pixivhub.model.enums.MFAType;
import ink.kangaroo.pixivhub.model.enums.Sex;
import ink.kangaroo.pixivhub.utils.DateUtils;
import lombok.Data;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * User entity
 *
 * @date 2019-03-12
 */
@Data
@Entity
@Table(name = "users")
@ToString(callSuper = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "ink.kangaroo.pixivhub.model.support.CustomIdGenerator")
    private Long id;


    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<UserAuth> userAuths;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<LikeIllust> likeIllust;
    /**
     * User ID.
     */
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;
    /**
     * User name.
     */
    @Column(name = "username", length = 50, nullable = false)
    private String username;

    /**
     * User nick name,used to display on page.
     */
    @Column(name = "nickname", nullable = false)
    private String nickname;

    /**
     * Password.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User email.
     */
    @Column(name = "email", length = 127)
    private String email;
    /**
     * User email.
     */
    @Column(name = "phone", length = 20)
    private String phone;


    /**
     * 用户状态是否启用
     */
    @Column(name = "user_status", nullable = false)
    @ColumnDefault("0")
    private Enable userStatus = Enable.DISABLE;

    /**
     * 生日
     */
    @Column(name = "birthday", nullable = false)
    private Date birthday = new Date(0);
    /**
     * 用户状态是否启用
     */
    @Column(name = "sex", nullable = false)
    @ColumnDefault("-1")
    private Sex sex = Sex.UNKNOW;
    /**
     * User avatar.
     */
    @Column(name = "avatar", length = 1023)
    private String avatar;

    /**
     * User description.
     */
    @Column(name = "description", length = 1023)
    private String description;

    /**
     * Expire time.
     */
    @Column(name = "expire_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expireTime;

    /**
     * mfa type (current: tfa)
     */
    @Column(name = "mfa_type", nullable = false)
    @ColumnDefault("0")
    private MFAType mfaType;

    /**
     * two factor auth key
     */
    @Column(name = "mfa_key", length = 64)
    private String mfaKey;

    @Override
    public void prePersist() {
        super.prePersist();

        if (email == null) {
            email = "";
        }

        if (avatar == null) {
            avatar = "";
        }

        if (description == null) {
            description = "";
        }

        if (expireTime == null) {
            expireTime = DateUtils.getNowDate();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
