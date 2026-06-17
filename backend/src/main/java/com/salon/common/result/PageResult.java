package com.salon.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页查询结果
 * <p>
 * 封装 MyBatis Plus 分页信息，统一返回格式
 * </p>
 *
 * @param <T> 数据类型
 */
@Data
@Schema(description = "分页查询结果")
public class PageResult<T> {

    /** 数据列表 */
    @Schema(description = "数据列表")
    private List<T> list;

    /** 总记录数 */
    @Schema(description = "总记录数", example = "100")
    private long total;

    /** 当前页码 */
    @Schema(description = "当前页码", example = "1")
    private long page;

    /** 每页条数 */
    @Schema(description = "每页条数", example = "10")
    private long pageSize;

    /** 总页数 */
    @Schema(description = "总页数", example = "10")
    private long pages;

    /**
     * 从 MyBatis Plus 分页对象构建
     *
     * @param page MyBatis Plus 分页对象
     * @param <T>  数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setList(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    /**
     * 从 MyBatis Plus 分页对象构建，支持类型转换（Entity → VO）
     *
     * @param page      MyBatis Plus 分页对象
     * @param converter 类型转换函数（如 MemberVO::from）
     * @param <T>       源类型
     * @param <R>       目标类型
     * @return 分页结果
     */
    public static <T, R> PageResult<R> of(IPage<T> page, Function<T, R> converter) {
        PageResult<R> result = new PageResult<>();
        result.setList(page.getRecords().stream().map(converter).collect(Collectors.toList()));
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }
}
