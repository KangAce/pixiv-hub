package ink.kangaroo.pixivhub.cache;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Cache wrapper.
 *
 * @author kang
 */
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
class CacheWrapper<V> implements Serializable {
    //TODO
    /**
     * Cache data
     */
    private V data;

    /**
     * Expired time.
     */
    private Date expireAt;

    /**
     * Create time.
     */
    private Date createAt;
}
