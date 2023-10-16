package com.mall.wxw.search.repository;

import com.mall.wxw.model.search.SkuEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author: wxw24633
 * @Time: 2023/10/16  13:21
 */
public interface SkuRepository extends ElasticsearchRepository<SkuEs, Long> {
}
