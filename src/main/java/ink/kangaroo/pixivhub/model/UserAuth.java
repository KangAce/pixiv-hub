package ink.kangaroo.pixivhub.model;

/**
 * @author Kang
 * @date 2020/8/12 17:59
 */

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

/**
 * Attachment entity
 *
 * @author ryanwang
 * @date 2019-03-12
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "user_auths")
public class UserAuth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "ink.kangaroo.pixivhub.model.support.CustomIdGenerator")
    private Long id;

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id")
    private User user;
    /**
     * OAuth Name.
     * 微信小程序_appid 表明是那个平台的授权
     * 微信公众号_appid
     */
    @Column(name = "oauth_name", nullable = false)
    private String oauthName;
    /**
     * OAuth ID. 用户在开放平台的唯一标识符 有可能为null 据说是如果没有绑定开放平台就会过去不到，但是我绑定了还是获取不到，可能是延迟，暂时忽略，在此情景下，无论如何都要允许为空
     */
    @Column(name = "union_id")
    private String unionId;
    /**
     * OAuth ID. 唯一标识 openId
     */
    @Column(name = "oauth_id", nullable = false)
    private String oauthId;
    /**
     * OAuth Access Token. session_key
     * 动态令牌
     */
    @Column(name = "oauth_access_token", nullable = false)
    private String OAuthAccessToken;
    /**
     * OAuth Access Token. session_key
     * 动态令牌
     */
    @Column(name = "oauth_refresh_token", nullable = false)
    private String OAuthRefreshToken;

    /**
     * OAuth Expires.
     */
    @Column(name = "oauth_expires", nullable = false)
    private String OAuthExpires;

    @Override
    public void prePersist() {
        super.prePersist();


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserAuth userAuth = (UserAuth) o;
        return id != null && Objects.equals(id, userAuth.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
