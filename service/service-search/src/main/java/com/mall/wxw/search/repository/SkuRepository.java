package com.mall.wxw.search.repository;

import com.mall.wxw.model.search.SkuEs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: wxw24633
 * @Time: 2023/10/16  13:21
 */
@Repository
public interface SkuRepository extends ElasticsearchRepository<SkuEs, Long> {

    Page<SkuEs> findByOrderByHotScoreDesc(Pageable pageable);
}
