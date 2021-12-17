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
import java.util.Objects;

/**
 * User entity
 *
 * @date 2019-03-12
 */
@Data
@Entity
@Table(name = "like_illust")
@ToString(callSuper = true)
public class LikeIllust extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "ink.kangaroo.pixivhub.model.support.CustomIdGenerator")
    private Long id;


    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id")
    private User user;
    /**
     * User ID.
     */
    @Column(name = "illust_id", length = 50, nullable = false)
    private String illustId;


    @Override
    public void prePersist() {
        super.prePersist();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        LikeIllust user = (LikeIllust) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
