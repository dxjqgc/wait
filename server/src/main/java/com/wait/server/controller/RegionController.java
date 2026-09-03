package com.wait.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wait.server.dto.R;
import com.wait.server.dto.RegionVO;
import com.wait.server.entity.RegionEntity;
import com.wait.server.mapper.RegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionMapper regionMapper;

    /** 不带 parentCode → 返回所有省；带 parentCode → 返回该父级下的子区域 */
    @GetMapping
    public R<List<RegionVO>> list(@RequestParam(required = false) String parentCode) {
        LambdaQueryWrapper<RegionEntity> w = new LambdaQueryWrapper<RegionEntity>()
                .orderByAsc(RegionEntity::getCode);
        if (parentCode == null || parentCode.isBlank()) {
            w.eq(RegionEntity::getLevel, 1);
        } else {
            w.eq(RegionEntity::getParentCode, parentCode);
        }
        List<RegionEntity> list = regionMapper.selectList(w);
        return R.ok(list.stream().map(e -> {
            RegionVO vo = new RegionVO();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).toList());
    }
}
