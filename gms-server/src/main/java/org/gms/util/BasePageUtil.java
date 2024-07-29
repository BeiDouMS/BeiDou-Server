package org.gms.util;

import com.mybatisflex.core.paginate.Page;
import org.gms.model.dto.BasePageDTO;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 对于数据库的查询分页，直接用mybatis-flex
 * 这里仅针对特殊情况，在已经有数据的情况下分页
 * 不需要过滤，不需要转换的情况下，直接 BasePageUtil.create(list, request).page();
 */
public class BasePageUtil<T> {
    private Stream<T> data;
    private final BasePageDTO basePageDTO;

    private BasePageUtil(Collection<T> data) {
        this(data, null);
    }

    private BasePageUtil(Collection<T> data, BasePageDTO basePageDTO) {
        if (data == null || data.isEmpty()) {
            this.data = Stream.of();
        } else {
            this.data = data.stream();
        }
        if (basePageDTO == null) {
            basePageDTO = new BasePageDTO();
        }
        if (basePageDTO.getPageNo() == null) {
            // 默认页码
            basePageDTO.setPageNo(1);
        }
        if (basePageDTO.getPageSize() == null) {
            // 默认每页条数
            basePageDTO.setPageSize(20);
        }
        this.basePageDTO = basePageDTO;
    }

    /**
     * 起始必须调create创建分页对象
     *
     * @param data 列表数据
     * @param <T>  类型
     * @return PageUtil对象
     */
    public static <T> BasePageUtil<T> create(Collection<T> data) {
        return new BasePageUtil<>(data);
    }

    public static <T> BasePageUtil<T> create(Collection<T> data, BasePageDTO basePageDTO) {
        return new BasePageUtil<>(data, basePageDTO);
    }

    public static <T> BasePageUtil<T> create(Collection<T> data, Integer pageNo, Integer pageSize) {
        return new BasePageUtil<>(data, BasePageDTO.builder().pageNo(pageNo).pageSize(pageSize).build());
    }

    public static <T> BasePageUtil<T> create(Collection<T> data, boolean onlyTotal, boolean notPage) {
        return new BasePageUtil<>(data, BasePageDTO.builder().onlyTotal(onlyTotal).notPage(notPage).build());
    }

    /**
     * 如有过滤数据的需要，可以调用这个方法
     *
     * @param predicate 过滤条件
     * @return PageUtil对象
     */
    public BasePageUtil<T> filter(Predicate<T> predicate) {
        this.data = this.data.filter(predicate);
        return this;
    }

    public BasePageUtil<T> sorted(Comparator<? super T> comparator) {
        this.data = this.data.sorted(comparator);
        return this;
    }

    /**
     * 构建分页对象
     *
     * @return 分页对象
     */
    public Page<T> page() {
        if (this.basePageDTO.isNotPage()) {
            List<T> list = this.data.toList();
            // 不分页
            return new Page<>(list, 1, list.size(), list.size());
        } else if (this.basePageDTO.isOnlyTotal()) {
            // 只查询数量
            Page<T> page = new Page<>();
            page.setTotalRow(this.data.toList().size());
            return page;
        } else {
            List<T> totalList = this.data.toList();
            List<T> list = totalList.stream()
                    .skip((long) (this.basePageDTO.getPageNo() - 1) * this.basePageDTO.getPageSize())
                    .limit(this.basePageDTO.getPageSize())
                    .toList();
            return new Page<>(list, this.basePageDTO.getPageNo(), this.basePageDTO.getPageSize(), totalList.size());
        }
    }

    /**
     * 如果不以原对象返回，需要构建新对象，用这个
     *
     * @return 分页对象
     */
    public <R> Page<R> page(Function<T, R> mapper) {
        if (this.basePageDTO.isNotPage()) {
            List<R> list = this.data.map(mapper).toList();
            // 不分页
            return new Page<>(list, 1, list.size(), list.size());
        } else if (this.basePageDTO.isOnlyTotal()) {
            // 只查询数量
            Page<R> page = new Page<>();
            page.setTotalRow(this.data.toList().size());
            return page;
        } else {
            List<T> totalList = this.data.toList();
            List<R> list = totalList.stream()
                    .skip((long) (this.basePageDTO.getPageNo() - 1) * this.basePageDTO.getPageSize())
                    .limit(this.basePageDTO.getPageSize())
                    .map(mapper)
                    .toList();
            return new Page<>(list, this.basePageDTO.getPageNo(), this.basePageDTO.getPageSize(), totalList.size());
        }
    }
}
