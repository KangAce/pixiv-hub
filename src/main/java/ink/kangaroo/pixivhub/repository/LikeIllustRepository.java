package ink.kangaroo.pixivhub.repository;

import ink.kangaroo.pixivhub.model.LikeIllust;
import ink.kangaroo.pixivhub.model.User;

import java.util.List;
import java.util.Optional;

/**
 * User repository.
 *
 * @author kang
 */
public interface LikeIllustRepository extends BaseRepository<LikeIllust, Long> {

    public List<LikeIllust> findByUser(User user);
    public Optional<LikeIllust> getByUserAndAndIllustId(User user,String illustId);

    public Integer deleteByUserAndIllustId(User user,String illustId);
}
