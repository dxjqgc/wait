package com.wait.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wait.server.dto.TagRequirementViewVO;
import com.wait.server.entity.TagRequirementConfirmationEntity;
import com.wait.server.entity.UserProfileEntity;
import com.wait.server.mapper.TagRequirementConfirmationMapper;
import com.wait.server.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TagRequirementService extends ServiceImpl<TagRequirementConfirmationMapper, TagRequirementConfirmationEntity> {

    private final UserProfileService userProfileService;

    /** 拉取 owner 的标签要求 + 当前 confirmer 是否已确认过 */
    public TagRequirementViewVO view(Long confirmerId, Long ownerId, String direction) {
        UserProfileEntity p = userProfileService.findOrCreateByUserId(ownerId);
        Map<String, Object> preset = JsonUtil.toMap(p.getRequirementPreset());
        List<Map<String, String>> custom = JsonUtil.toMapList(p.getRequirementCustom());

        List<TagRequirementConfirmationEntity> exists = list(
                new LambdaQueryWrapper<TagRequirementConfirmationEntity>()
                        .eq(TagRequirementConfirmationEntity::getConfirmerId, confirmerId)
                        .eq(TagRequirementConfirmationEntity::getRequirementOwnerId, ownerId)
                        .eq(TagRequirementConfirmationEntity::getDirection, direction));

        List<TagRequirementViewVO.ConfirmationItem> items = new ArrayList<>();

        // preset 项目
        for (Map.Entry<String, Object> e : preset.entrySet()) {
            TagRequirementViewVO.ConfirmationItem item = new TagRequirementViewVO.ConfirmationItem();
            item.setKey(e.getKey());
            item.setValue(e.getValue() == null ? null : String.valueOf(e.getValue()));
            item.setSatisfied(true); // preset 检验交给前端/业务规则，默认满足
            item.setConfirmed(lookup(exists, e.getKey(), direction));
            items.add(item);
        }
        // custom 项目
        for (Map<String, String> c : custom) {
            String k = c.get("key");
            String v = c.get("value");
            TagRequirementViewVO.ConfirmationItem item = new TagRequirementViewVO.ConfirmationItem();
            item.setKey(k);
            item.setValue(v);
            item.setSatisfied(true);
            item.setConfirmed(lookup(exists, k, direction));
            items.add(item);
        }

        TagRequirementViewVO vo = new TagRequirementViewVO();
        vo.setOwnerId(ownerId);
        vo.setPreset(preset);
        vo.setCustom(custom);
        vo.setItems(items);
        return vo;
    }

    private boolean lookup(List<TagRequirementConfirmationEntity> list, String key, String direction) {
        return list.stream().anyMatch(x -> key.equals(x.getRequirementKey()));
    }

    /** 写入确认记录（幂等：UNIQUE 约束兜底） */
    public void recordConfirmation(Long confirmerId, Long ownerId, String direction,
                                   String key, String value) {
        TagRequirementConfirmationEntity entity = new TagRequirementConfirmationEntity();
        entity.setConfirmerId(confirmerId);
        entity.setRequirementOwnerId(ownerId);
        entity.setDirection(direction);
        entity.setRequirementKey(key);
        entity.setRequirementValue(value);
        entity.setConfirmed(1);
        try {
            save(entity);
        } catch (org.springframework.dao.DuplicateKeyException ignored) {
            // 已确认过，幂等
        }
    }

    /** confirmer 是否已对 owner 的所有要求确认过 */
    public boolean allConfirmed(Long confirmerId, Long ownerId, String direction) {
        UserProfileEntity p = userProfileService.findOrCreateByUserId(ownerId);
        Map<String, Object> preset = JsonUtil.toMap(p.getRequirementPreset());
        List<Map<String, String>> custom = JsonUtil.toMapList(p.getRequirementCustom());
        int expected = preset.size() + custom.size();
        if (expected == 0) return true;
        long actual = count(new LambdaQueryWrapper<TagRequirementConfirmationEntity>()
                .eq(TagRequirementConfirmationEntity::getConfirmerId, confirmerId)
                .eq(TagRequirementConfirmationEntity::getRequirementOwnerId, ownerId)
                .eq(TagRequirementConfirmationEntity::getDirection, direction));
        return actual >= expected;
    }
}
