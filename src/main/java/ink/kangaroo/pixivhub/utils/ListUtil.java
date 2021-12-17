package ink.kangaroo.pixivhub.utils;

import cn.hutool.db.PageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class ListUtil {
    /**
     * list  去重
     *
     * @param keyExtra
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtra) {
        ConcurrentHashMap<Object, Boolean> map = new ConcurrentHashMap<>();
        //只有一个map,匿名内部类构造的时候，会将map传入。所以只有一个map。
        return t -> map.putIfAbsent(keyExtra.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 切割数组
     *
     * @param paramList
     * @param length
     * @return
     */
    public static <T> List<List<T>> divideList(List<T> paramList, Integer length) {
        List<List<T>> list = new ArrayList<>();
        int size = paramList.size();
        int toIndex = length;
        if (size <= length) {
            list.add(paramList);
            return list;
        }
        for (int i = 0; i < size; i += length) {
            if (i + length > size) {
                toIndex = size - i;
            }
            List<T> newList = paramList.subList(i, i + toIndex);
            list.add(newList);
        }
        return list;
    }


    public static <T> List<T> arrayToList(List<T> sourceArray, int startIndex, int length) {
        List<T> targetList = new ArrayList<>();
        if (startIndex >= sourceArray.size()) {
            return targetList;
        }
        int tem;
        int copyLength;
        copyLength = (tem = startIndex + length) > sourceArray.size() ? sourceArray.size() : tem;
        targetList.addAll(sourceArray.subList(startIndex, copyLength));
        return targetList;
    }

    /**
     * List分页
     */
    public static <T> PageResult<T> pagination(List<T> srcList, int pageNum, int pageSize) {
        List<T> list = new ArrayList<>();
        PageResult<T> pageResult = new PageResult<>();
        for (int i = (pageNum - 1) * pageSize; i < pageNum * pageSize && i < srcList.size(); i++) {
            list.add(srcList.get(i));
        }
        pageResult.addAll(list);
        pageResult.setTotal(srcList.size());
        return pageResult;
    }
}
