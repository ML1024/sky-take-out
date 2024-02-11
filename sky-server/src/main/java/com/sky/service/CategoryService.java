package com.sky.service;

import com.sky.dto.CategoryDTO;
import org.springframework.stereotype.Service;

public interface CategoryService {

    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);
}
